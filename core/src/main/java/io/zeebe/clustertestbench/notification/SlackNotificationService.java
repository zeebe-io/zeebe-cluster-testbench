package io.zeebe.clustertestbench.notification;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

public class SlackNotificationService implements NotificationService {

  private final MethodsClient slackClient;
  private final String slackChannel;

  public SlackNotificationService(String token, String slackChannel) {

    Slack slack = Slack.getInstance();

    slackClient = slack.methods(token);
    this.slackChannel = slackChannel;
  }

  @Override
  public void sendNotification(final String message) throws Exception {
    ChatPostMessageRequest request = ChatPostMessageRequest.builder().channel(slackChannel)
        .text(message).build();


    ChatPostMessageResponse response = slackClient.chatPostMessage(request);
    if (response.getError() != null) {
      final var errorMessage = String
          .format("Expected to send message %s to channel %s, but failed with %s.", message, slackChannel, response.getError());
      throw new Exception(errorMessage);
    }
  }
}
