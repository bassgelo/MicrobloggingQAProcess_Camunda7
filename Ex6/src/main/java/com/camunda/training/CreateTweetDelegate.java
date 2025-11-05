package com.camunda.training;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import okhttp3.OkHttpClient;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

//this is a Spring bean
@Component("ClassOnStereoids")
public class CreateTweetDelegate implements JavaDelegate {

    private final Logger LOGGER = LoggerFactory.getLogger(CreateTweetDelegate.class.getName());

    public void execute(DelegateExecution delegateExecution) throws Exception {

        String accessToken = "l-ttg3BmJL3qSlS_VGg_2wrR5rWmxEDgFRIUbdN7qTQ";
        String instanceName = "mastodon.social";
        MastodonClient client =
                new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson())
                        .accessToken(accessToken).build();

        String status = "Hello world from Camunda Test!";
        Long inReplyToId = null;
        List<Long> mediaIds = null;
        boolean sensitive = false;
        String spoilerText = null;

        // Connect to the Mastodon API's statuses endpoint
        Statuses statusesEndpoint = new Statuses(client);

        // Post a status
        statusesEndpoint
                .postStatus(status, inReplyToId, mediaIds, sensitive, spoilerText)
                .execute();

    }
}
