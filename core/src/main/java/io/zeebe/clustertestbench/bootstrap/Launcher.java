package io.zeebe.clustertestbench.bootstrap;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance;
import static java.lang.Runtime.getRuntime;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClientFactory;
import io.zeebe.clustertestbench.handler.AggregateTestResultHandler;
import io.zeebe.clustertestbench.handler.CheckGenerationUsageHandler;
import io.zeebe.clustertestbench.handler.CreateClusterInCamundaCloudHandler;
import io.zeebe.clustertestbench.handler.CreateGenerationInCamundaCloudHandler;
import io.zeebe.clustertestbench.handler.DeleteClusterInCamundaCloudHandler;
import io.zeebe.clustertestbench.handler.DeleteGenerationInCamundaCloudHandler;
import io.zeebe.clustertestbench.handler.GatherInformationAboutClusterInCamundaCloudHandler;
import io.zeebe.clustertestbench.handler.MapNamesToUUIDsHandler;
import io.zeebe.clustertestbench.handler.NotifyEngineersHandler;
import io.zeebe.clustertestbench.handler.QueryClusterStateInCamundaCloudHandler;
import io.zeebe.clustertestbench.handler.RecordTestResultHandler;
import io.zeebe.clustertestbench.handler.SequentialTestHandler;
import io.zeebe.clustertestbench.handler.TriggerMessageStartEventHandler;
import io.zeebe.clustertestbench.handler.WarmUpClusterHandler;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClientFactory;
import io.zeebe.clustertestbench.notification.SlackNotificationService;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

  private final Map<String, JobWorker> registeredJobWorkers = new HashMap<>();

  private final String testOrchestrationContactPoint;
  private final OAuthServiceAccountAuthenticationDetails testOrchestrationAuthenticatonDetails;

  private final String sheetsApiKeyFileContent;
  private final String reportSheetID;

  private final String slackWebhookUrl;

  private final CloudAPIClient cloudApiClient;
  private final InternalCloudAPIClient internalCloudApiClient;

  public Launcher(
      final String testOrchestrationContactPoint,
      final OAuthServiceAccountAuthenticationDetails testOrchestrationAuthenticatonDetails,
      final String cloudApiUrl,
      final OAuthServiceAccountAuthenticationDetails cloudApiAuthenticationDetails,
      final String internalCloudApiUrl,
      final OAuthUserAccountAuthenticationDetails internalCloudApiAuthenticationDetails,
      final String sheetsApiKeyfileContent,
      final String reportSheetID,
      final String slackWebhookUrl) {
    this.testOrchestrationContactPoint = testOrchestrationContactPoint;
    this.testOrchestrationAuthenticatonDetails = testOrchestrationAuthenticatonDetails;
    this.sheetsApiKeyFileContent = sheetsApiKeyfileContent;
    this.reportSheetID = reportSheetID;
    this.slackWebhookUrl = slackWebhookUrl;

    cloudApiClient = createCloudApiClient(cloudApiUrl, cloudApiAuthenticationDetails);
    internalCloudApiClient =
        createInternalCloudApiClient(internalCloudApiUrl, internalCloudApiAuthenticationDetails);
  }

  public void launch() throws IOException {
    performSelfTest();

    final OAuthCredentialsProvider cred = buildCredentialsProvider();

    try (final ZeebeClient client =
        ZeebeClient.newClientBuilder()
            .numJobWorkerExecutionThreads(50)
            .gatewayAddress(testOrchestrationContactPoint)
            .credentialsProvider(cred)
            .build(); ) {

      try {
        final boolean success =
            new ProcessDeployer(client).deployProcessesInClasspathFolder("processes");

        if (!success) {
          LOGGER.warn("Deployment failed");
        }
      } catch (final IOException e) {
        LOGGER.error("Error while deploying process: " + e.getMessage(), e);
      }

      registerWorkers(client);

      getRuntime()
          .addShutdownHook(
              new Thread("Close thread") {
                @Override
                public void run() {
                  LOGGER.info("Received shutdown signal");

                  registeredJobWorkers.values().forEach(JobWorker::close);
                }
              });

      waitForInterruption();

      LOGGER.info("About to complete normally");
    }
  }

  private void performSelfTest() {
    testConnectionToOrchestrationCluster();
    testConnectionToCloudApi();
    testConnectionToInternalCloudApi();
    testConnectionToSlack();
    testConnectionToGoogleSheets();
  }

  private void testConnectionToOrchestrationCluster() {
    final OAuthCredentialsProvider cred = buildCredentialsProvider();

    try (final ZeebeClient client =
        ZeebeClient.newClientBuilder()
            .numJobWorkerExecutionThreads(50)
            .gatewayAddress(testOrchestrationContactPoint)
            .credentialsProvider(cred)
            .build(); ) {
      client.newTopologyRequest().send().join();

      LOGGER.info("Selftest - Successfully established connection to test orchestration cluster");
    } catch (final Exception e) {
      LOGGER.error("Selftest - Unable to establish connection to test orchestration cluster", e);
    }
  }

  private void testConnectionToCloudApi() {
    try {
      cloudApiClient.getParameters();

      LOGGER.info("Selftest - Successfully established connection to cloud API");
    } catch (final Exception e) {
      LOGGER.error("Selftest - Unable to establish connection to cloud API", e);
    }
  }

  private void testConnectionToInternalCloudApi() {
    try {
      internalCloudApiClient.listGenerationInfos();

      LOGGER.info("Selftest - Successfully established connection to internal cloud API");
    } catch (final Exception e) {
      LOGGER.error("Selftest - Unable to establish connection to internal cloud API", e);
    }
  }

  private CloudAPIClient createCloudApiClient(
      final String cloudApiUrl,
      final OAuthServiceAccountAuthenticationDetails authenticationDetails) {
    final String authenticationServerUrl = authenticationDetails.getServerURL();
    final String audience = authenticationDetails.getAudience();
    final String clientId = authenticationDetails.getClientId();
    final String clientSecret = authenticationDetails.getClientSecret();

    return new CloudAPIClientFactory()
        .createCloudAPIClient(
            cloudApiUrl, authenticationServerUrl, audience, clientId, clientSecret);
  }

  private InternalCloudAPIClient createInternalCloudApiClient(
      final String internalCloudApiUrl,
      final OAuthUserAccountAuthenticationDetails authenticationDetails) {
    final String authenticationServerUrl = authenticationDetails.getServerURL();
    final String audience = authenticationDetails.getAudience();
    final String clientId = authenticationDetails.getClientId();
    final String clientSecret = authenticationDetails.getClientSecret();
    final String username = authenticationDetails.getUsername();
    final String password = authenticationDetails.getPassword();

    return new InternalCloudAPIClientFactory()
        .createCloudAPIClient(
            internalCloudApiUrl,
            authenticationServerUrl,
            audience,
            clientId,
            clientSecret,
            username,
            password);
  }

  private void testConnectionToSlack() {
    final Slack slack = Slack.getInstance();

    try {
      final WebhookResponse response = slack.send(slackWebhookUrl, Payload.builder().build());
      if ("invalid_payload".equals(response.getBody())) {
        LOGGER.info("Selftest - Successfully established connection to Slack");
      } else {
        LOGGER.error(
            "Selftest - Wrong response when establishing connection to Slack: " + response);
      }
    } catch (final Exception e) {
      LOGGER.error("Selftest - Unable to establish connection to Slack", e);
    }
  }

  private void testConnectionToGoogleSheets() {
    try (final var inputStream = new StringBufferInputStream(sheetsApiKeyFileContent)) {
      final GoogleCredential credential =
          GoogleCredential.fromStream(inputStream)
              .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

      final Sheets service =
          new Sheets.Builder(newTrustedTransport(), getDefaultInstance(), credential)
              .setApplicationName("Zeebe Cluster Testbench - Publish Test Results Worker")
              .build();

      final Sheets.Spreadsheets.Values.Get request =
          service.spreadsheets().values().get(reportSheetID, "Sheet1!A1:B1");

      request.execute();
      LOGGER.info("Selftest - Successfully established connection to Google Sheets");
    } catch (final Exception e) {
      LOGGER.error("Selftest - Unable to establish connection to Google Sheets", e);
    }
  }

  private void registerWorkers(final ZeebeClient client) {
    registerWorker(
        client,
        "map-names-to-uuids-job",
        new MapNamesToUUIDsHandler(cloudApiClient),
        Duration.ofSeconds(10));
    registerWorker(
        client,
        "create-zeebe-cluster-in-camunda-cloud-job",
        new CreateClusterInCamundaCloudHandler(cloudApiClient),
        Duration.ofMinutes(1));
    registerWorker(
        client,
        "query-zeebe-cluster-state-in-camunda-cloud-job",
        new QueryClusterStateInCamundaCloudHandler(cloudApiClient),
        Duration.ofSeconds(10));
    registerWorker(
        client,
        "gather-information-about-cluster-in-camunda-cloud-job",
        new GatherInformationAboutClusterInCamundaCloudHandler(cloudApiClient),
        Duration.ofSeconds(10));
    registerWorker(
        client, "warm-up-cluster-job", new WarmUpClusterHandler(), Duration.ofMinutes(3));

    registerWorker(
        client, "run-sequential-test-job", new SequentialTestHandler(), Duration.ofMinutes(30));

    try {
      registerWorker(
          client,
          "record-test-result-job",
          new RecordTestResultHandler(sheetsApiKeyFileContent, reportSheetID),
          Duration.ofSeconds(10));
    } catch (final IOException | GeneralSecurityException e) {
      LOGGER.error(
          "Exception while creating and registering worker for 'record-test-result-job'", e);
    }

    final var slackNotificationService = new SlackNotificationService(slackWebhookUrl);
    registerWorker(
        client,
        "notify-engineers-job",
        new NotifyEngineersHandler(slackNotificationService),
        Duration.ofSeconds(10));
    registerWorker(
        client,
        "destroy-zeebe-cluster-in-camunda-cloud-job",
        new DeleteClusterInCamundaCloudHandler(cloudApiClient),
        Duration.ofSeconds(10));

    registerWorker(
        client,
        "create-generation-in-camunda-cloud-job",
        new CreateGenerationInCamundaCloudHandler(internalCloudApiClient),
        Duration.ofSeconds(10));
    registerWorker(
        client,
        "delete-generation-in-camunda-cloud-job",
        new DeleteGenerationInCamundaCloudHandler(internalCloudApiClient),
        Duration.ofSeconds(10));
    registerWorker(
        client,
        "aggregate-test-results-job",
        new AggregateTestResultHandler(),
        Duration.ofSeconds(10));
    registerWorker(
        client,
        "trigger-message-start-event-job",
        new TriggerMessageStartEventHandler(client),
        Duration.ofSeconds(10));
    registerWorker(
        client,
        "check-generation-usage-job",
        new CheckGenerationUsageHandler(cloudApiClient),
        Duration.ofSeconds(10));
  }

  private void registerWorker(
      final ZeebeClient client,
      final String jobType,
      final JobHandler jobHandler,
      final Duration timeout) {
    LOGGER.info(
        "Registering job worker " + jobHandler.getClass().getSimpleName() + " for: " + jobType);

    final JobWorker workerRegistration =
        client.newWorker().jobType(jobType).handler(jobHandler).timeout(timeout).open();

    registeredJobWorkers.put(jobType, workerRegistration);

    LOGGER.info("Job worker opened and receiving jobs.");
  }

  private OAuthCredentialsProvider buildCredentialsProvider() {
    final OAuthServiceAccountAuthenticationDetails authenticationDetails =
        testOrchestrationAuthenticatonDetails;

    if (authenticationDetails.getServerURL() == null) {
      // use default server
      return new OAuthCredentialsProviderBuilder()
          .audience(authenticationDetails.getAudience())
          .clientId(authenticationDetails.getClientId())
          .clientSecret(authenticationDetails.getClientSecret())
          .build();
    } else {
      return new OAuthCredentialsProviderBuilder()
          .authorizationServerUrl(authenticationDetails.getServerURL())
          .audience(authenticationDetails.getAudience())
          .clientId(authenticationDetails.getClientId())
          .clientSecret(authenticationDetails.getClientSecret())
          .build();
    }
  }

  private static void waitForInterruption() {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    try {
      countDownLatch.await();
    } catch (final InterruptedException e) {
      LOGGER.info(e.getMessage(), e);
    }
  }
}
