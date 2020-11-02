package io.zeebe.clustertestbench.notification;

public interface NotificationService {

  /**
   * Sends a message over the notification service to the given target.
   *
   * @param target the target where the message should be sent to
   * @param message the message which should be sent
   * @throws Exception is thrown when an error happens during sending the given message
   */
  void sendNotification(String target, String message) throws Exception ;
}
