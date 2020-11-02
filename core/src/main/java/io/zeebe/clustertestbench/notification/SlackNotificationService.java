package io.zeebe.clustertestbench.notification;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

public class SlackNotificationService implements NotificationService {

  private final MethodsClient slackClient;

  public SlackNotificationService(String token) {

    Slack slack = Slack.getInstance();

    slackClient = slack.methods(token);
  }

  @Override
  public void sendNotification(final String target, final String message) throws Exception {
    ChatPostMessageRequest request = ChatPostMessageRequest.builder().channel(target)
        .text(message).build();


    ChatPostMessageResponse response = slackClient.chatPostMessage(request);
    if (response.getError() != null) {
      final var errorMessage = String
          .format("Expected to send message %s to channel %s, but failed with %s.", message, target, response.getError());
      throw new Exception(errorMessage);
    }
  }
}
