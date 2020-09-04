package io.zeebe.clustertestbench.bootstrap;

import static java.lang.Runtime.getRuntime;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;
import io.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import io.zeebe.clustertestbench.bootstrap.mock.MockBootstrapper;
import io.zeebe.clustertestbench.testdriver.sequential.SequentialTestParameters;
import io.zeebe.clustertestbench.worker.CreateClusterInCamundaCloudWorker;
import io.zeebe.clustertestbench.worker.DeleteClusterInCamundaCloudWorker;
import io.zeebe.clustertestbench.worker.MapNamesToUUIDsWorker;
import io.zeebe.clustertestbench.worker.NotifyEngineersWorker;
import io.zeebe.clustertestbench.worker.RecordTestResultWorker;
import io.zeebe.clustertestbench.worker.SequentialTestLauncher;
import io.zeebe.model.bpmn.BpmnModelInstance;
import io.zeebe.workflow.generator.builder.SequenceWorkflowBuilder;

public class Launcher {

	private static final Logger logger = Logger.getLogger(Launcher.class.getPackageName());

	// jobs to replace with mocks (during development)
	private static final List<String> jobsToMock = Arrays.asList();

	private final Map<String, JobWorker> registeredJobWorkers = new HashMap<>();

	private final String testOrchestrationContactPoint;
	private final OAuthAuthenticationDetails testOrchestrationAuthenticatonDetails;

	private final String cloudApiUrl;
	private final OAuthAuthenticationDetails cloudApiAuthenticationDetails;

	private final String reportSheetID;
	private final String slackToken;

	public Launcher(String testOrchestrationContactPoint,
			OAuthAuthenticationDetails testOrchestrationAuthenticatonDetails, String cloudApiUrl,
			OAuthAuthenticationDetails cloudApiAuthenticationDetails, String reportSheetID, String slackToken) {
		this.testOrchestrationContactPoint = testOrchestrationContactPoint;
		this.testOrchestrationAuthenticatonDetails = testOrchestrationAuthenticatonDetails;
		this.cloudApiUrl = cloudApiUrl;
		this.cloudApiAuthenticationDetails = cloudApiAuthenticationDetails;
		this.reportSheetID = reportSheetID;
		this.slackToken = slackToken;
	}

	public void launch() throws IOException {
		final OAuthCredentialsProvider cred = buildCredentialsProvider();

		try (final ZeebeClient client = ZeebeClient.newClientBuilder().numJobWorkerExecutionThreads(50)
				.brokerContactPoint(testOrchestrationContactPoint).credentialsProvider(cred).build();) {
			client.newTopologyRequest().send().join();

			logger.log(Level.INFO, "Connection to cluster established");

			boolean success = new WorkflowDeployer(client).deployWorkflowsInClasspathFolder("workflows");

			if (!success) {
				throw new IllegalStateException("Deployment failed");
			}

			registerWorkers(client);

			MockBootstrapper mockBootstrapper = new MockBootstrapper(client, jobsToMock);
			mockBootstrapper.registerMockWorkers();

			getRuntime().addShutdownHook(new Thread("Gateway close thread") {
				@Override
				public void run() {
					mockBootstrapper.stop();
					registeredJobWorkers.values().forEach(JobWorker::close);
				}
			});

			Map<String, Object> variables = new HashMap<>();
			variables.put("clusterPlans",
					Arrays.asList("Development", "Production - S", "Production - M", "Production - L"));
			variables.put("generation", "Zeebe 0.24.2");
			variables.put("channel", "Internal Dev");
			variables.put("region", "Europe West 1D");
			variables.put("sequentialTestParams", SequentialTestParameters.defaultParams());

			logger.log(Level.INFO,
					"Starting workflow instance of 'run-all-tests-in-camunda-cloud-per-cluster-plan-process'");
			client.newCreateInstanceCommand().bpmnProcessId("run-all-tests-in-camunda-cloud-per-cluster-plan-process")
					.latestVersion().variables(variables).send().join();

			waitUntilSystemInput("exit");
		}
	}

	private void registerWorkers(final ZeebeClient client) {
		final OAuthAuthenticationDetails authenticationDetails = cloudApiAuthenticationDetails;

		final String cloudApiAuthenticationServerUrl = authenticationDetails.getServerURL();
		final String cloudApiAudience = authenticationDetails.getAudience();
		final String cloudApiClientId = authenticationDetails.getClientId();
		final String cloudApiClientSecret = authenticationDetails.getClientSecret();

		registerWorker(
				client, "map-names-to-uuids-job", new MapNamesToUUIDsWorker(cloudApiUrl,
						cloudApiAuthenticationServerUrl, cloudApiAudience, cloudApiClientId, cloudApiClientSecret),
				Duration.ofSeconds(10));
		registerWorker(
				client, "create-zeebe-cluster-in-camunda-cloud-job", new CreateClusterInCamundaCloudWorker(cloudApiUrl,
						cloudApiAuthenticationServerUrl, cloudApiAudience, cloudApiClientId, cloudApiClientSecret),
				Duration.ofMinutes(18));
		registerWorker(client, "run-sequential-test-job", new SequentialTestLauncher(), Duration.ofMinutes(30));
		registerWorker(client, "record-test-result-job", new RecordTestResultWorker(reportSheetID),
				Duration.ofSeconds(10));
		registerWorker(client, "notify-engineers-job", new NotifyEngineersWorker(slackToken), Duration.ofSeconds(10));
		registerWorker(
				client, "destroy-zeebe-cluster-in-camunda-cloud-job", new DeleteClusterInCamundaCloudWorker(cloudApiUrl,
						cloudApiAuthenticationServerUrl, cloudApiAudience, cloudApiClientId, cloudApiClientSecret),
				Duration.ofSeconds(10));
	}

	private void registerWorker(ZeebeClient client, String jobType, JobHandler jobHandler, Duration timeout) {
		logger.log(Level.INFO, "Registering job worker " + jobHandler.getClass().getSimpleName() + " for: " + jobType);

		final JobWorker workerRegistration = client.newWorker().jobType(jobType).handler(jobHandler).timeout(timeout)
				.open();

		registeredJobWorkers.put(jobType, workerRegistration);

		String workflowId = "execute-job-worker-" + jobType + "-in-isolation";
		BpmnModelInstance workflow = new SequenceWorkflowBuilder(Optional.of(1), Optional.of(jobType))
				.buildWorkflow(workflowId);

		client.newDeployCommand().addWorkflowModel(workflow, workflowId + ".bpmn").send().join();

		logger.log(Level.INFO, "Job worker opened and receiving jobs.");
	}

	private OAuthCredentialsProvider buildCredentialsProvider() {
		final OAuthAuthenticationDetails authenticationDetails = testOrchestrationAuthenticatonDetails;

		if (authenticationDetails.getServerURL() == null) {
			// use default server
			return new OAuthCredentialsProviderBuilder().audience(authenticationDetails.getAudience())
					.clientId(authenticationDetails.getClientId()).clientSecret(authenticationDetails.getClientSecret())
					.build();
		} else {
			return new OAuthCredentialsProviderBuilder().authorizationServerUrl(authenticationDetails.getServerURL())
					.audience(authenticationDetails.getAudience()).clientId(authenticationDetails.getClientId())
					.clientSecret(authenticationDetails.getClientSecret()).build();
		}
	}

	private static void waitUntilSystemInput(final String exitCode) {
		try (final Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				final String nextLine = scanner.nextLine();
				if (nextLine.contains(exitCode)) {
					return;
				}
			}
		}
	}

}
