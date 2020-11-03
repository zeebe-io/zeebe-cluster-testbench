package io.zeebe.clustertestbench.notification;

public interface NotificationService {

  /**
   * Sends a message over the notification service.
   *
   * @param message the message which should be sent
   * @throws Exception is thrown when an error happens during sending the given message
   */
  void sendNotification(String message) throws Exception ;
}
