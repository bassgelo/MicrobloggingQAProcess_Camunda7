package com.camunda.training;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import okhttp3.OkHttpClient;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class CreateTweetDelegate implements JavaDelegate {
    private final Logger LOGGER = LoggerFactory.getLogger(CreateTweetDelegate.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        boolean incident = execution.getVariable("incident")!=null?(Boolean) execution.getVariable("incident"):false;

        //Imagine here the error is due to a bad quality connection, so it is purely on the network side
        if (incident) {
            throw new RuntimeException("simulated network error");
        }

        String content = (String) execution.getVariable("content");
        LOGGER.info("Publishing tweet: " + content);

        String accessToken = "l-ttg3BmJL3qSlS_VGg_2wrR5rWmxEDgFRIUbdN7qTQ";
        String instanceName = "mastodon.social";
        MastodonClient client =
                new MastodonClient.Builder(instanceName, new OkHttpClient.Builder(), new Gson())
                        .accessToken(accessToken).build();

        String status;
        status = content;
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
