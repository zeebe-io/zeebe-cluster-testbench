package io.zeebe.clustertestbench.mockhandler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.handler.MapNamesToUUIDsHandler.InputOutput;

public class MockMapNamesToUUIDsHandler implements JobHandler {

  public static final String UUID_SUFFIX = "-uuid";

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final InputOutput inputOutput = job.getVariablesAsType(InputOutput.class);

    inputOutput.setChannelUUID(inputOutput.getChannel() + UUID_SUFFIX);
    inputOutput.setClusterPlanUUID(inputOutput.getClusterPlan() + UUID_SUFFIX);
    inputOutput.setGenerationUUID(inputOutput.getGeneration() + UUID_SUFFIX);
    inputOutput.setRegionUUID(inputOutput.getRegion() + UUID_SUFFIX);

    client.newCompleteCommand(job.getKey()).variables(inputOutput).send();
  }
}
