package io.zeebe.clustertestbench.worker;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient;

public final class DeleteGenerationInCamundaCloudWorker implements JobHandler {

	private final InternalCloudAPIClient internalApiClient;

	public DeleteGenerationInCamundaCloudWorker(InternalCloudAPIClient internalApiClient) {
		this.internalApiClient = internalApiClient;
	}

	@Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		Input input = job.getVariablesAsType(Input.class);
				
		internalApiClient.deleteGeneration(input.getGenerationUUID());
		
		client.newCompleteCommand(job.getKey()).send().join();
	}
	
	protected static final class Input {
		private String generationUUID;

		public String getGenerationUUID() {
			return generationUUID;
		}

		public void setGenerationUUID(String generationUUID) {
			this.generationUUID = generationUUID;
		}
		
	}

}
