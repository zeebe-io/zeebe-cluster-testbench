package io.zeebe.clustertestbench.notification;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import java.io.IOException;

public class SlackNotificationService implements NotificationService {

  private final Slack client;
  private final String webhookUrl;

  public SlackNotificationService(final String webhookUrl) {
    client = Slack.getInstance();
    this.webhookUrl = webhookUrl;
  }

  @Override
  public void sendNotification(final String message) throws Exception {
    final Payload payload = Payload.builder().text(message).build();
    final WebhookResponse response = client.send(webhookUrl, payload);

    if (response.getCode() >= 400) {
      final var errorMessage =
          String.format(
              "Expected to send message %s to Slack, but failed with %s", message, response);
      throw new IOException(errorMessage);
    }
  }
}
