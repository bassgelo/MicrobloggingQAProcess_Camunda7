package com.camunda.training;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
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

import static java.util.Map.entry;
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
  @Deployment(resources = {"javadevstwitter_12.bpmn", "tweetApproval.dmn"})
  public void testHappyPath() {

    //Adding a random number to avoid Twitter error with status
    Random random = new Random(); //instance of random class
    int upperbound = 9999;



    // Start process with Java API and variables
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("Process_simpleTQA_exercise12");

    //Query the tasks that do not have any assignee i.e write Tweet
    List<Task> taskListNoAssignee = taskService()
            .createTaskQuery()
            .processInstanceId(processInstance.getId())
            .list();

    //Get the task
    Task taskNoAssignee = taskListNoAssignee.get(0);

    // Create a HashMap to put in variables for the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    String text = "Test best tweet No. "+random.nextInt(upperbound);
    variables.put("content", text);
    variables.put("email", "gerardo@camunda.org");
    taskService().complete(taskNoAssignee.getId(), variables);

    //Our process has a task marked as Asynch before, it means it will be executed by the job executor
    //In the tests we do not have a job executor, so we have to 'manually' move the process to the next step
    List<Job> jobList = jobQuery()
            .processInstanceId(processInstance.getId())
            .list();
    org.assertj.core.api.Assertions.assertThat(jobList).hasSize(1);
    Job job = jobList.get(0);
    execute(job);

    //Check in your BPMN for more asynch before/after, for each of them you will have to do move manually the process to the next step

    // Make assertions on the process instance
    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"javadevstwitter_12.bpmn", "tweetApproval.dmn"})
  // this test does not need the dmn file but without it, Test coverage complains:
  // java.lang.AssertionError: Class coverage can only be calculated if all tests deploy the same BPMN resources.
  public void testRejectTweet() {
    // start the process
    Map<String, Object> varMap = new HashMap<>();
    varMap.put("approved", false);
    varMap.put("content", "This is my unhappy path JUnit tweet!! "
            + System.currentTimeMillis());
    ProcessInstance processInstance = runtimeService()
            .createProcessInstanceByKey("Process_simpleTQA_exercise12")
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
  @Deployment(resources = {"javadevstwitter_12.bpmn", "tweetApproval.dmn"})
  public void testSuperUserMessage() {
    // start the process
    ProcessInstance processInstance = runtimeService()
            .createMessageCorrelation("superUserTweetExercise12")
            .setVariable("content", "My Exercise 11 Tweet GM- " + System.currentTimeMillis())
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
  @Deployment(resources = {"javadevstwitter_12.bpmn", "tweetApproval.dmn"})
  public void testTweetWithdrawn() {
    Map<String, Object> varMap = new HashMap<>();
    varMap.put("content", "Test tweetWithdrawn message");

    ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_simpleTQA_exercise12", varMap);

    assertThat(processInstance).isStarted().isWaitingAt(findId("Write Tweet"));

    runtimeService()
            .createMessageCorrelation("tweetWithdrawExercise12")
            .processInstanceVariableEquals("content", "Test tweetWithdrawn message")
            .correlateWithResult();

    assertThat(processInstance).isEnded();

  }

  @Test
  //@Deployment(resources = "tweetApproval.dmn")
  @Deployment(resources = {"javadevstwitter_12.bpmn", "tweetApproval.dmn"})
  public void testTweetFromGerardo() {

    Map<String, Object> variables = withVariables("email", "jakob.freund@camunda.com", "content", "this should be published");
    DmnDecisionTableResult decisionResult = decisionService().evaluateDecisionTableByKey("tweetApproval", variables);

    org.assertj.core.api.Assertions.assertThat(decisionResult.getFirstResult()).isNotNull();
    org.assertj.core.api.Assertions.assertThat(decisionResult.getFirstResult()).containsKey("approved");
    org.assertj.core.api.Assertions.assertThat(decisionResult.getFirstResult()).containsEntry("approved", false);
    org.assertj.core.api.Assertions.assertThat(decisionResult.getFirstResult()).contains(entry("approved", false));
  }

}
