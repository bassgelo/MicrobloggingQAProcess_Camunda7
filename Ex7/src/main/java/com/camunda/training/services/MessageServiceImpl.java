package com.camunda.training.services;
import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Override
    public void sendMessage(String content) throws Exception {

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
