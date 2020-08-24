package io.zeebe.clustertestbench.testdriver.sequential;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;
import io.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import io.zeebe.clustertestbench.testdriver.api.CamundaCLoudAuthenticationDetails;
import io.zeebe.model.bpmn.BpmnModelInstance;
import io.zeebe.workflow.generator.builder.SequenceWorkflowBuilder;

public class SequentialTestDriver {

	private static final Logger logger = Logger.getLogger("io.zeebe.clustertestbench.testdriver.simple");

	private static final String JOB_TYPE = "test-job";
	private static final String WORKFLOW_ID = "simple-test-sequence-with-one-step";

	private ZeebeClient client;

	public SequentialTestDriver(CamundaCLoudAuthenticationDetails authenticationDetails) {
		logger.log(Level.INFO, "Creating Simple Test Driver");
		final OAuthCredentialsProvider cred = buildCredentialsProvider(Objects.requireNonNull(authenticationDetails));

		client = ZeebeClient.newClientBuilder().brokerContactPoint(authenticationDetails.getContactPoint())
				.credentialsProvider(cred).build();

		SequenceWorkflowBuilder builder = new SequenceWorkflowBuilder(Optional.of(1), Optional.of(JOB_TYPE));

		BpmnModelInstance workflow = builder.buildWorkflow(WORKFLOW_ID);

		logger.log(Level.INFO, "Deploying test workflow:" + WORKFLOW_ID);
		client.newDeployCommand().addWorkflowModel(workflow, WORKFLOW_ID + ".bpmn").send().join();
	}

	public boolean runTest(int iterations) {
		boolean pass = true;
		logger.log(Level.INFO, "Starting Simple Test Driver");
		try {
			JobWorker workerRegistration = client.newWorker().jobType(JOB_TYPE).handler(new MoveAlongJobHandler())
					.timeout(Duration.ofSeconds(10)).open();
			
			for (int i = 0; i < iterations ; i++) {
				try {
					client
					.newCreateInstanceCommand()
					.bpmnProcessId(WORKFLOW_ID)
					.latestVersion()
					.withResult()
					.requestTimeout(Duration.ofSeconds(10))
					.send().get();
				} catch (Throwable t) {
			        final Throwable cause = t.getCause();
			        if (cause instanceof StatusRuntimeException) {
			          final StatusRuntimeException statusRuntimeException = (StatusRuntimeException) cause;
			          if (statusRuntimeException.getStatus().getCode() != Code.RESOURCE_EXHAUSTED) {
			        	  pass = false;
			          }
			        } else {
			        	pass = false;
			        }
				}				
			}

			workerRegistration.close();
		} finally {
			client.close();
		}
		
		return pass;
	}

	private OAuthCredentialsProvider buildCredentialsProvider(CamundaCLoudAuthenticationDetails authenticationDetails) {
		if (authenticationDetails.getAuthorizationURL() == null) {
			return new OAuthCredentialsProviderBuilder().audience(authenticationDetails.getAudience())
					.clientId(authenticationDetails.getClientId()).clientSecret(authenticationDetails.getClientSecret())
					.build();
		} else {
			return new OAuthCredentialsProviderBuilder()
					.authorizationServerUrl(authenticationDetails.getAuthorizationURL())
					.audience(authenticationDetails.getAudience()).clientId(authenticationDetails.getClientId())
					.clientSecret(authenticationDetails.getClientSecret()).build();
		}
	}

	private static class MoveAlongJobHandler implements JobHandler {
		@Override
		public void handle(final JobClient client, final ActivatedJob job) {
			logger.log(Level.INFO, job.toString());
			client.newCompleteCommand(job.getKey()).send().join();
		}
	}
}
