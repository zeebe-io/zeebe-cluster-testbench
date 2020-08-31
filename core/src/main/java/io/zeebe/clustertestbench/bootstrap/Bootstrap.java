package io.zeebe.clustertestbench.bootstrap;

import static java.lang.Runtime.getRuntime;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;
import io.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import io.zeebe.clustertestbench.bootstrap.mock.MockBootstrapper;
import io.zeebe.clustertestbench.cloud.request.CreateClusterRequest;
import io.zeebe.clustertestbench.cloud.response.CreateClusterResponse;
import io.zeebe.clustertestbench.testdriver.sequential.SequentialTestParameters;
import io.zeebe.clustertestbench.worker.CreateClusterInCamundaCloudWorker;
import io.zeebe.clustertestbench.worker.DeleteClusterInCamundaCloudWorker;
import io.zeebe.clustertestbench.worker.NotifyEngineersWorker;
import io.zeebe.clustertestbench.worker.RecordTestResultWorker;
import io.zeebe.clustertestbench.worker.SequentialTestLauncher;
import io.zeebe.model.bpmn.BpmnModelInstance;
import io.zeebe.workflow.generator.builder.SequenceWorkflowBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "bootstrap", mixinStandardHelpOptions = true, description = "Deploys workflows to the test orchestration Zeebe cluster", exitCodeListHeading = "Exit Codes:%n", exitCodeList = {
		" 0: Successful program execution", "-1: Unsuccessful program execution" })
public class Bootstrap implements Callable<Integer> {

	private static final Logger logger = Logger.getLogger("io.zeebe.clustertestbench.bootstrap");

	private static final List<String> jobsToMock = Arrays.asList();

	@Option(names = { "-r",
			"--report-sheet-id" }, description = "ID of the Google Sheet into which the test reports will be written", required = true)
	private String reportSheetID;

	@Option(names = { "-t", "--slack-token" }, description = "Token to access slack API", required = true)
	private String slackToken;

	// details to talk to the Zeebe cluster that orchestrates the test processes
	@Option(names = { "-c", "--contact-point" }, description = "Contact point for the Zeebe cluster", required = true)
	private String contactPoint;

	@Option(names = { "-a",
			"--audience" }, description = "(Optional) Zeebe token audience. If omitted it will be derived from the contact point")
	private String audience;

	@Option(names = { "-s", "--clinet-secret" }, description = "Client secret for authentication", required = true)
	private String clientSecret;

	@Option(names = { "-i", "--clinet-id" }, description = "Client id for authentication", required = true)
	private String clientId;

	@Option(names = { "-u", "--authentication-server-url" }, description = "URL for the authentication server")
	private String authenticationServerUrl;

	// details to talk to Camunda Cloud
	@Option(names = { "--cloud-api-url" }, description = "Contact point for the Camunda cloud API", required = true)
	private String cloudApiUrl;

	@Option(names = { "-ca", "--cloud-audience" }, description = "Camunda cloud token audience", required = true)
	private String cloudApiAudience;

	@Option(names = { "-cs",
			"--cloud-client-secret" }, description = "Client secret for Caunda cloud authentication", required = true)
	private String cloudApiClientSecret;

	@Option(names = { "-ci",
			"--cloud-client-id" }, description = "Client id for Camunda cloud authentication", required = true)
	private String cloudApiClientId;

	@Option(names = { "-cu",
			"--cloud-authentication-server-url" }, description = "URL for the Camunda cloud authentication server")
	private String cloudApiAuthenticationServerUrl;

	private final Map<String, JobWorker> registeredJobWorkers = new HashMap<>();

	public static void main(String[] args) {
		int exitCode = new CommandLine(new Bootstrap()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		logger.log(Level.INFO, "Bootstrapper starting");

		deriveMissingOptions();

		logger.log(Level.INFO, "Testbench cluster - contactPoint: " + contactPoint);
		logger.log(Level.INFO, "Testbench cluster - audience: " + audience);
		logger.log(Level.INFO, "Testbench cluster - clientId: " + clientId);

		if (authenticationServerUrl != null) {
			logger.log(Level.INFO, "Testbench cluster - authorizationServerUrl:" + authenticationServerUrl);
		}

		logger.log(Level.INFO, "Camunda cloud - API URL: " + cloudApiUrl);
		logger.log(Level.INFO, "Camunda cloud - audience: " + cloudApiAudience);
		logger.log(Level.INFO, "Camunda cloud - clientId: " + cloudApiClientId);
		logger.log(Level.INFO, "Camunda cloud - authorizationServerUrl:" + cloudApiAuthenticationServerUrl);

		final OAuthCredentialsProvider cred = buildCredentialsProvider();

		try (final ZeebeClient client = ZeebeClient.newClientBuilder().brokerContactPoint(contactPoint)
				.credentialsProvider(cred).build();) {
			client.newTopologyRequest().send().join();

			logger.log(Level.INFO, "Connection to cluster established");

			boolean success = new WorkflowDeployer(client).deployWorkflowsInClasspathFolder("workflows");

			if (!success) {
				return -1;
			}

			registerWorker(client, "create-zeebe-cluster-in-camunda-cloud-job",
					new CreateClusterInCamundaCloudWorker(cloudApiUrl, cloudApiAuthenticationServerUrl,
							cloudApiAudience, cloudApiClientId, cloudApiClientSecret),
					Duration.ofMinutes(18));
			registerWorker(client, "run-sequential-test-job", new SequentialTestLauncher(), Duration.ofHours(2));
			registerWorker(client, "record-test-result-job", new RecordTestResultWorker(reportSheetID),
					Duration.ofSeconds(10));
			registerWorker(client, "notify-engineers-job", new NotifyEngineersWorker(slackToken),
					Duration.ofSeconds(10));
			registerWorker(client, "destroy-zeebe-cluster-in-camunda-cloud-job",
					new DeleteClusterInCamundaCloudWorker(cloudApiUrl, cloudApiAuthenticationServerUrl,
							cloudApiAudience, cloudApiClientId, cloudApiClientSecret),
					Duration.ofSeconds(10));

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
			variables.put("clusterPlans", Arrays.asList("736d9100-0155-4af5-be14-b09c42de8417"));
			variables.put("dockerImage", "fcb8c48e-e320-4ae7-8299-3fb95e340feb");
			variables.put("channelId", "cf355219-cc2d-4266-83e1-2430998ddf30");
			variables.put("regionId", "deadbeef-eaea-4bd3-972a-70203f150d88");
			variables.put("sequentialTestParams", SequentialTestParameters.defaultParams());
			

			logger.log(Level.INFO, "Starting workflow instance of 'run-all-tests'");
			client.newCreateInstanceCommand().bpmnProcessId("run-all-tests").latestVersion().variables(variables).send()
					.join();

			waitUntilSystemInput("exit");
		}

		return 0;
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
		if (authenticationServerUrl == null) {
			return new OAuthCredentialsProviderBuilder().audience(audience).clientId(clientId)
					.clientSecret(clientSecret).build();
		} else {
			return new OAuthCredentialsProviderBuilder().authorizationServerUrl(authenticationServerUrl)
					.audience(audience).clientId(clientId).clientSecret(clientSecret).build();
		}
	}

	private void deriveMissingOptions() {
		if (audience == null) {
			audience = contactPoint.substring(0, contactPoint.lastIndexOf(":"));
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
