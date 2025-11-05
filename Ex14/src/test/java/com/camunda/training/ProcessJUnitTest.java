package com.camunda.training;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.process_test_coverage.junit5.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessJUnitTest {

  /*
  @Rule
  @ClassRule
  public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  @Autowired
  private GenericApplicationContext context;
   */

  @Test
  @Deployment(resources = "javadevstwitter_11.bpmn")
  public void testHappyPath() {

    //Adding a random number to avoid Twitter error with status
    Random random = new Random(); //instance of random class
    int upperbound = 9999;

    // Start process with Java API and variables
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("Process_simpleTQA_exercise11");

    //Query the tasks that do not have any assignee
    List<Task> taskListNoAssignee = taskService()
            .createTaskQuery()
            .processInstanceId(processInstance.getId())
            .list();

    //Get the task
    Task taskNoAssignee = taskListNoAssignee.get(0);

    // Create a HashMap to put in variables for the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    String text = "Exercise 11: Test tweet No. "+random.nextInt(upperbound);
    variables.put("content", text);
    taskService().complete(taskNoAssignee.getId(), variables);


    //Query the tasks that belong to the management group
    List<Task> taskList = taskService()
            .createTaskQuery()
            .taskCandidateGroup("management")
            .processInstanceId(processInstance.getId())
            .list();

    org.assertj.core.api.Assertions.assertThat(taskList).isNotNull();
    org.assertj.core.api.Assertions.assertThat(taskList).hasSize(1);

    //Get the task
    Task task = taskList.get(0);

    //complete the task and pass the variables
    Map<String, Object> approvedMap = new HashMap<String, Object>();
    approvedMap.put("approved", true);
    taskService().complete(task.getId(), approvedMap);

    //Our process has a task marked as Asynch before, it means it will be executed by the job executor
    //In the tests we do not have a job executor, so we have to 'manually' move the process to the next step
    List<Job> jobList = jobQuery()
            .processInstanceId(processInstance.getId())
            .list();
    org.assertj.core.api.Assertions.assertThat(jobList).hasSize(1);
    Job job = jobList.get(0);
    execute(job);

    // Make assertions on the process instance
    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources= "javadevstwitter_11.bpmn")
  public void testRejectTweet() {
    // start the process
    Map<String, Object> varMap = new HashMap<>();
    varMap.put("approved", false);
    varMap.put("content", "This is my unhappy path JUnit tweet!! "
            + System.currentTimeMillis());
    ProcessInstance processInstance = runtimeService()
            .createProcessInstanceByKey("Process_simpleTQA_exercise11")
            .setVariables(varMap)
            .startAfterActivity(findId("Approve Tweet"))
            .execute();


    //Assert that the process instance is waiting at the external task and that the task has the expected topic.
    //Then complete the task to continue the process execution.
    assertThat(processInstance)
            .isWaitingAt(findId("Notify Tweet Rejection"))
            .externalTask()
            .hasTopicName("TweetRejectionNotification");
    complete(externalTask());
  }

  @Test
  @Deployment(resources= "javadevstwitter_11.bpmn")
  public void testSuperUserMessage() {
    // start the process
    ProcessInstance processInstance = runtimeService()
            .createMessageCorrelation("superUserTweet")
            .setVariable("content", "Mozambique: My Exercise 11 Tweet GM- " + System.currentTimeMillis())
            .correlateWithResult()
            .getProcessInstance();

    assertThat(processInstance).isStarted();

    //The following correlation is a mismatching correlation
    /*
    runtimeService()
            .createMessageCorrelation("TweetWithdraw")
            .processInstanceId(processInstance.getProcessInstanceId())
            .correlateWithResult();
     */

    // get the job
    List<Job> jobList = jobQuery()
            .processInstanceId(processInstance.getId())
            .list();

    // execute the job
    org.assertj.core.api.Assertions.assertThat(jobList).hasSize(1);
    Job job = jobList.get(0);
    execute(job);
    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources= "javadevstwitter_11.bpmn")
  public void testTweetWithdrawn() {
    Map<String, Object> varMap = new HashMap<>();
    varMap.put("content", "Test tweetWithdrawn message");

    ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_simpleTQA_exercise11", varMap);

    assertThat(processInstance).isStarted().isWaitingAt(findId("Write Tweet"));

    runtimeService()
            .createMessageCorrelation("tweetWithdraw")
            .processInstanceVariableEquals("content", "Test tweetWithdrawn message")
            .correlateWithResult();

    assertThat(processInstance).isEnded();

  }

}
