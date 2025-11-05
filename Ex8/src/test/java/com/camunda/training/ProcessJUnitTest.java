package com.camunda.training;

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

  @Test
  @Deployment(resources = "javadevstwitter_5.bpmn")
  public void testHappyPath() {

    //Adding a random number to make message unique
    Random random = new Random(); //instance of random class
    int upperbound = 9999;

    // Create a HashMap to put in variables for the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    String text = "Exercise 5, GM Message No. "+random.nextInt(upperbound);
    variables.put("content", text);

    // Start process with Java API and variables
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("Process_simpleTQA_exercise5", variables);

    //assert and complete for "write tweet", which is the current task after the process started
    assertThat(processInstance).task().isNotAssigned();
    complete(task());

    //query, assert and complete for "approve tweet"
    //Query the tasks that belong to the management group
    List<Task> taskList = taskService()
            .createTaskQuery()
            //.taskCandidateGroup("management")
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

    // Make assertions on the process instance
    assertThat(processInstance).isEnded();
  }

}
