package io.zeebe.clustertestbench.mockhandler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

public class NoOpHandler implements JobHandler {

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    client.newCompleteCommand(job).send().join();
  }
}
