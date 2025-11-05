package com.camunda.training;

import com.camunda.training.services.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Slf4j
@Component("createTwitterDelegate") //creates a named bean
public class CreateTweetDelegate implements JavaDelegate {

    private final Logger LOGGER = LoggerFactory.getLogger(CreateTweetDelegate.class.getName());

    private final MessageService messageService;

    @Inject
    public CreateTweetDelegate(MessageService messageService ){
        this.messageService = messageService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        //input mapping
        String content = (String) execution.getVariable("content");
        //service bean call
        messageService.sendMessage(content);
        //output mapping

    }
}
