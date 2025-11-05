package com.camunda.training;

import com.camunda.training.services.MessageServiceImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit5.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import java.util.HashMap;
import java.util.Map;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;


@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessJUnitTest {

  @Test
  @Deployment(resources = "javadevstwitter_4.bpmn")
  public void testHappyPath() {
    //I have 2 options
    //option1: create a real bean of the class that publishes the tweet
    //CreateTweetDelegate localMock = new CreateTweetDelegate(new MessageServiceImpl());

    //option2: mock a bean (mocking is creating a dummy object of a class)
    //We mock our named bean, so that we do not call the business logic in the test
    CreateTweetDelegate localMock = Mockito.mock(CreateTweetDelegate.class); //instruction from Mockito

    //we always use this instruction to pass a loaded bean or mock to the test context
    Mocks.register("createTwitterDelegate", localMock); //instruction from Camunda

    // Create a HashMap to put in variables for the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    String text = "Test tweet GM WAS HERE !";
    variables.put("content", text);
    variables.put("approved", true);

    // Start process with Java API and variables
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("Process_simpleTQA_exercise4", variables);

    assertThat(processInstance).task().isNotAssigned();
    complete(task());

    assertThat(processInstance).isEnded();
  }

}
