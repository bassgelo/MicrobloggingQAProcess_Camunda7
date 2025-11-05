import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TweetRejectionWorker {
    public static void main(String[] args) {
        // bootstrap the client
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest") //connecting to the engine
                .asyncResponseTimeout(20000)
                .lockDuration(10000)
                .maxTasks(1)
                .build();

        // subscribe to the topic
        TopicSubscriptionBuilder subscriptionBuilder = client
                .subscribe("TweetRejectionNotification"); //suscribing to the task, also, i.e. TOPIC

        // handle job
        subscriptionBuilder.handler((externalTask, externalTaskService) -> {
            //logic that you want to execute
            String content = externalTask.getVariable("content");
            System.out.println("Sorry, your tweet has been rejected: " + content);
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("notficationTimestamp", new Date());
            externalTaskService.complete(externalTask, variables); //make sure you always have a complete instruction
        });

        subscriptionBuilder.open();
    }
}
