package io.zeebe.clustertestbench.handler;

import static java.util.Objects.requireNonNull;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * This handler is a work around for the missing message throw event element. When called, it will
 * send a message start event. The message name is configured in the header of the service task. The
 * content of the message are all variables passed to the service task.
 *
 * <p>It is not a full-fledged replacement for the missing message throw event. In particular, it
 * doesn't set a value for the correlation key. This is sufficient for message start events, but
 * insufficient for the general case
 */
public class TriggerMessageStartEventHandler implements JobHandler {

  protected static final String KEY_MESSAGE_NAME = "messageName";

  private final ZeebeClient zeebeClient;

  public TriggerMessageStartEventHandler(final ZeebeClient zeebeClient) {
    this.zeebeClient = requireNonNull(zeebeClient);
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final var headers = job.getCustomHeaders();

    if (!headers.containsKey(KEY_MESSAGE_NAME)) {
      throw new IllegalArgumentException("Header value '" + KEY_MESSAGE_NAME + "' is not defined");
    }

    final var messageName = headers.get(KEY_MESSAGE_NAME);

    if (StringUtils.isEmpty(messageName)) {
      throw new IllegalArgumentException("Message name is null or empyty");
    }

    final var variables = job.getVariables();

    zeebeClient
        .newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey("correlationKey")
        .variables(variables)
        .send()
        .join();

    client.newCompleteCommand(job.getKey()).send().join();
  }
}
