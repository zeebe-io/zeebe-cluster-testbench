package io.zeebe.clustertestbench.bootstrap;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Bootstrapper that reads its values from environment variables */
public class BootstrapFromEnvVars {

  private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapFromEnvVars.class);

  private static final String PREFIX = "ZCTB_"; // "Zeebe Cluster Test Bench"

  public static void main(final String[] args) {
    LOGGER.info("Bootstrapper starting");

    try {
      final String authenticationServerUrl =
          getEnvironmentvariable("AUTHENTICATION_SERVER_URL", true);
      String audience = getEnvironmentvariable("AUDIENCE", true);

      // test orchestration Zeebe cluster
      final String contactPoint = getEnvironmentvariable("CONTACT_POINT", false);

      if (audience == null) {
        audience = contactPoint.substring(0, contactPoint.lastIndexOf(":"));
      }

      final String clientId = getEnvironmentvariable("CLIENT_ID", false);
      final String clientSecret = getEnvironmentvariable("CLIENT_SECRET", false);

      final OAuthServiceAccountAuthenticationDetails testOrchestrationAuthenticatonDetails =
          new OAuthServiceAccountAuthenticationDetails(
              authenticationServerUrl, audience, clientId, clientSecret);

      // cloud API
      final String cloudApiUrl = getEnvironmentvariable("CLOUD_API_URL", false);
      final String cloudApiAuthenticationServerUrl =
          getEnvironmentvariable("CLOUD_AUTHENTICATION_SERVER_URL", false);

      final String cloudApiAudience = getEnvironmentvariable("CLOUD_AUDIENCE", false);
      final String cloudApiClientId = getEnvironmentvariable("CLOUD_CLIENT_ID", false);
      final String cloudApiClientSecret = getEnvironmentvariable("CLOUD_CLIENT_SECRET", false);

      final OAuthServiceAccountAuthenticationDetails cloudApiAuthenticationDetails =
          new OAuthServiceAccountAuthenticationDetails(
              cloudApiAuthenticationServerUrl,
              cloudApiAudience,
              cloudApiClientId,
              cloudApiClientSecret);

      // internal cloud APi
      final String internalCloudApiAuthenticationServerUrl =
          getEnvironmentvariable("INTERNAL_CLOUD_AUTHENTICATION_SERVER_URL", false);
      final String inernalCloudApiUrl = getEnvironmentvariable("INTERNAL_CLOUD_API_URL", false);
      final String internalCloudApiAudience =
          getEnvironmentvariable("INTERNAL_CLOUD_AUDIENCE", false);
      final String internalCloudApiClientId =
          getEnvironmentvariable("INTERNAL_CLOUD_CLIENT_ID", false);
      final String internalCloudApiClientSecret =
          getEnvironmentvariable("INTERNAL_CLOUD_CLIENT_SECRET", false);
      final String internalCloudApiUsername =
          getEnvironmentvariable("INTERNAL_CLOUD_USERNAME", false);
      final String internalCloudApiPassword =
          getEnvironmentvariable("INTERNAL_CLOUD_PASSWORD", false);

      final OAuthUserAccountAuthenticationDetails internalCloudApiAuthenticationDetails =
          new OAuthUserAccountAuthenticationDetails(
              internalCloudApiAuthenticationServerUrl,
              internalCloudApiAudience,
              internalCloudApiClientId,
              internalCloudApiClientSecret,
              internalCloudApiUsername,
              internalCloudApiPassword);

      final String slackWebhookUrl = getEnvironmentvariable("SLACK_WEBHOOK_URL", false);

      final String testbenchRestAddress = getEnvironmentvariable("TESTBENCH_REST_ADDRESS", false);

      new Launcher(
              contactPoint,
              testOrchestrationAuthenticatonDetails,
              cloudApiUrl,
              cloudApiAuthenticationDetails,
              inernalCloudApiUrl,
              internalCloudApiAuthenticationDetails,
              slackWebhookUrl,
              testbenchRestAddress)
          .launch();
    } catch (final Exception e) {
      LOGGER.error(e.getMessage(), e);
      System.exit(-1);
    }
  }

  private static String getEnvironmentvariable(final String suffix, final boolean optional) {
    final Map<String, String> envVars = System.getenv();

    final String key = PREFIX + suffix;

    if (!optional && !envVars.containsKey(key)) {
      throw new IllegalStateException("Unable to find mandatory environment variable " + key);
    }

    return envVars.get(key);
  }
}
