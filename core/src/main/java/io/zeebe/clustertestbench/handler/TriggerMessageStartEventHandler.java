package io.zeebe.clustertestbench.handler;

import static java.util.Objects.requireNonNull;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import org.apache.commons.lang3.StringUtils;

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
        .correlationKey("")
        .variables(variables)
        .send()
        .join();

    client.newCompleteCommand(job.getKey()).send().join();
  }
}
