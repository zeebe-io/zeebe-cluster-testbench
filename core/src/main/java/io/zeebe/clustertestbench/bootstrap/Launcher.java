package io.zeebe.clustertestbench.bootstrap;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance;
import static java.lang.Runtime.getRuntime;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.api.ApiTestRequest;
import com.slack.api.methods.response.api.ApiTestResponse;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;
import io.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import io.zeebe.clustertestbench.bootstrap.mock.MockBootstrapper;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClientFactory;
import io.zeebe.clustertestbench.testdriver.sequential.SequentialTestParameters;
import io.zeebe.clustertestbench.worker.CreateClusterInCamundaCloudWorker;
import io.zeebe.clustertestbench.worker.DeleteClusterInCamundaCloudWorker;
import io.zeebe.clustertestbench.worker.GatherInformationAboutClusterInCamundaCloudWorker;
import io.zeebe.clustertestbench.worker.MapNamesToUUIDsWorker;
import io.zeebe.clustertestbench.worker.NotifyEngineersWorker;
import io.zeebe.clustertestbench.worker.QueryClusterStateInCamundaCloudWorker;
import io.zeebe.clustertestbench.worker.RecordTestResultWorker;
import io.zeebe.clustertestbench.worker.SequentialTestLauncher;

public class Launcher {

	private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

	// jobs to replace with mocks (during development)
	private static final List<String> jobsToMock = Arrays.asList();

	private final Map<String, JobWorker> registeredJobWorkers = new HashMap<>();

	private final String testOrchestrationContactPoint;
	private final OAuthAuthenticationDetails testOrchestrationAuthenticatonDetails;

	private final String cloudApiUrl;
	private final OAuthAuthenticationDetails cloudApiAuthenticationDetails;

	private String sheetsApiKeyFileContent;
	private final String reportSheetID;

	private final String slackToken;

	public Launcher(String testOrchestrationContactPoint,
			OAuthAuthenticationDetails testOrchestrationAuthenticatonDetails, String cloudApiUrl,
			OAuthAuthenticationDetails cloudApiAuthenticationDetails, String sheetsApiKeyfileContent,
			String reportSheetID, String slackToken) {
		this.testOrchestrationContactPoint = testOrchestrationContactPoint;
		this.testOrchestrationAuthenticatonDetails = testOrchestrationAuthenticatonDetails;
		this.cloudApiUrl = cloudApiUrl;
		this.cloudApiAuthenticationDetails = cloudApiAuthenticationDetails;
		this.sheetsApiKeyFileContent = sheetsApiKeyfileContent;
		this.reportSheetID = reportSheetID;
		this.slackToken = slackToken;
	}

	public void launch() throws IOException {
		performSelfTest();

		final OAuthCredentialsProvider cred = buildCredentialsProvider();

		try (final ZeebeClient client = ZeebeClient.newClientBuilder().numJobWorkerExecutionThreads(50)
				.brokerContactPoint(testOrchestrationContactPoint).credentialsProvider(cred).build();) {

			try {
				boolean success = new WorkflowDeployer(client).deployWorkflowsInClasspathFolder("workflows");

				if (!success) {
					logger.warn("Deployment failed");
				}
			} catch (IOException e) {
				logger.error("Error while deploying workflow: " + e.getMessage(), e);
			}

			registerWorkers(client);

			MockBootstrapper mockBootstrapper = new MockBootstrapper(client, jobsToMock);
			mockBootstrapper.registerMockWorkers();

			getRuntime().addShutdownHook(new Thread("Close thread") {
				@Override
				public void run() {
					logger.info("Received shutdown signal");

					mockBootstrapper.stop();
					registeredJobWorkers.values().forEach(JobWorker::close);
				}
			});

//			Map<String, Object> variables = new HashMap<>();
//			variables.put("clusterPlans", Arrays.asList("Production - S", "Production - M"));
//			variables.put("generation", "Zeebe 0.24.2");
//			variables.put("channel", "Internal Dev");
//			variables.put("region", "Europe West 1D");
//			variables.put("sequentialTestParams", SequentialTestParameters.defaultParams());
//
//			logger.info("Starting workflow instance of 'run-all-tests-in-camunda-cloud-per-cluster-plan-process'");
//			client.newCreateInstanceCommand().bpmnProcessId("run-all-tests-in-camunda-cloud-per-cluster-plan-process")
//					.latestVersion().variables(variables).send().join();

			waitForInterruption();

			logger.info("About to complete normally");
		}
	}

	private void performSelfTest() {
		testConnectionToOrchestrationCluster();
		testConnectionToCloudApi();
		testConnectionToSlack();
		testConnectionToGoogleSheets();
	}

	private void testConnectionToOrchestrationCluster() {
		final OAuthCredentialsProvider cred = buildCredentialsProvider();

		try (final ZeebeClient client = ZeebeClient.newClientBuilder().numJobWorkerExecutionThreads(50)
				.brokerContactPoint(testOrchestrationContactPoint).credentialsProvider(cred).build();) {
			client.newTopologyRequest().send().join();

			logger.info("Selftest - Successfully established connection to test orchestration cluster");
		} catch (Throwable t) {
			logger.error("Selftest - Unable to establish connection to test orchestration cluster", t);
		}
	}

	private void testConnectionToCloudApi() {
		try {
			final OAuthAuthenticationDetails authenticationDetails = cloudApiAuthenticationDetails;

			final String cloudApiAuthenticationServerUrl = authenticationDetails.getServerURL();
			final String cloudApiAudience = authenticationDetails.getAudience();
			final String cloudApiClientId = authenticationDetails.getClientId();
			final String cloudApiClientSecret = authenticationDetails.getClientSecret();

			CloudAPIClient client = new CloudAPIClientFactory().createCloudAPIClient(cloudApiUrl,
					cloudApiAuthenticationServerUrl, cloudApiAudience, cloudApiClientId, cloudApiClientSecret);
			client.getParameters();

			logger.info("Selftest - Successfully established connection to cloud API");
		} catch (Throwable t) {
			logger.error("Selftest - Unable to establish connection to cloud API", t);
		}
	}

	private void testConnectionToSlack() {
		Slack slack = Slack.getInstance();

		MethodsClient slackClient = slack.methods(slackToken);

		try {
			ApiTestResponse response = slackClient.apiTest(ApiTestRequest.builder().foo("test").build());

			String returnedFoo = response.getArgs().getFoo();

			if ("test".equalsIgnoreCase(returnedFoo)) {
				logger.info("Selftest - Successfully established connection to Slack");
			} else {
				logger.error("Selftest - Wrong respponse when establishing connection to Slack: " + returnedFoo);
			}
		} catch (Throwable t) {
			logger.error("Selftest - Unable to establish connection to Slack", t);
		}
	}

	private void testConnectionToGoogleSheets() {
		try {
			GoogleCredential credential = GoogleCredential
					.fromStream(new StringBufferInputStream(sheetsApiKeyFileContent))
					.createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

			Sheets service = new Sheets.Builder(newTrustedTransport(), getDefaultInstance(), credential)
					.setApplicationName("Zeebe Cluster Testbench - Publish Test Results Worker").build();

			Sheets.Spreadsheets.Values.Get request = service.spreadsheets().values().get(reportSheetID, "Sheet1!A1:B1");

			request.execute();
			logger.info("Selftest - Successfully established connection to Google Sheets");
		} catch (Throwable t) {
			logger.error("Selftest - Unable to establish connection to Google Sheets", t);
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
				Duration.ofMinutes(1));
		registerWorker(client, "query-zeebe-cluster-state-in-camunda-cloud-job",
				new QueryClusterStateInCamundaCloudWorker(cloudApiUrl, cloudApiAuthenticationServerUrl,
						cloudApiAudience, cloudApiClientId, cloudApiClientSecret),
				Duration.ofSeconds(10));
		registerWorker(client, "gather-information-about-cluster-in-camunda-cloud-job",
				new GatherInformationAboutClusterInCamundaCloudWorker(cloudApiUrl, cloudApiAuthenticationServerUrl,
						cloudApiAudience, cloudApiClientId, cloudApiClientSecret),
				Duration.ofSeconds(10));
		registerWorker(client, "run-sequential-test-job", new SequentialTestLauncher(), Duration.ofMinutes(30));

		try {
			registerWorker(client, "record-test-result-job",
					new RecordTestResultWorker(sheetsApiKeyFileContent, reportSheetID), Duration.ofSeconds(10));
		} catch (IOException | GeneralSecurityException e) {
			logger.error("Exception while creating and registering worker for 'record-test-result-job'", e);
		}

		registerWorker(client, "notify-engineers-job", new NotifyEngineersWorker(slackToken), Duration.ofSeconds(10));
		registerWorker(
				client, "destroy-zeebe-cluster-in-camunda-cloud-job", new DeleteClusterInCamundaCloudWorker(cloudApiUrl,
						cloudApiAuthenticationServerUrl, cloudApiAudience, cloudApiClientId, cloudApiClientSecret),
				Duration.ofSeconds(10));
	}

	private void registerWorker(ZeebeClient client, String jobType, JobHandler jobHandler, Duration timeout) {
		logger.info("Registering job worker " + jobHandler.getClass().getSimpleName() + " for: " + jobType);

		final JobWorker workerRegistration = client.newWorker().jobType(jobType).handler(jobHandler).timeout(timeout)
				.open();

		registeredJobWorkers.put(jobType, workerRegistration);

//		String workflowId = "execute-job-worker-" + jobType + "-in-isolation";
//		BpmnModelInstance workflow = new SequenceWorkflowBuilder(Optional.of(1), Optional.of(jobType))
//				.buildWorkflow(workflowId);
//
//		client.newDeployCommand().addWorkflowModel(workflow, workflowId + ".bpmn").send().join();

		logger.info("Job worker opened and receiving jobs.");
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

	private static void waitForInterruption() {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			logger.info(e.getMessage(), e);
		}
	}

}
