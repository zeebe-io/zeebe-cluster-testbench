package io.zeebe.clustertestbench.bootstrap;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstrapper that reads its values from environment variables
 */
public class BootstrapFromEnvVars {

	private static final Logger logger = LoggerFactory.getLogger(BootstrapFromEnvVars.class);

	private static final String PREFIX = "ZCTB_"; // "Zeebe Cluster Test Bench"

	public static void main(String[] args) {
		logger.info("Bootstrapper starting");

		try {
			final String authenticationServerUrl = getEnvironmentvariable("AUTHENTICATION_SERVER_URL", true);
			String audience = getEnvironmentvariable("AUDIENCE", true);

			// test orchestration Zeebe cluster
			final String contactPoint = getEnvironmentvariable("CONTACT_POINT", false);

			if (audience == null) {
				audience = contactPoint.substring(0, contactPoint.lastIndexOf(":"));
			}

			final String clientId = getEnvironmentvariable("CLIENT_ID", false);
			final String clientSecret = getEnvironmentvariable("CLIENT_SECRET", false);

			final OAuthServiceAccountAuthenticationDetails testOrchestrationAuthenticatonDetails = new OAuthServiceAccountAuthenticationDetails(
					authenticationServerUrl, audience, clientId, clientSecret);

			// cloud API
			final String cloudApiUrl = getEnvironmentvariable("CLOUD_API_URL", false);
			final String cloudApiAuthenticationServerUrl = getEnvironmentvariable("CLOUD_AUTHENTICATION_SERVER_URL",
					false);

			final String cloudApiAudience = getEnvironmentvariable("CLOUD_AUDIENCE", false);
			final String cloudApiClientId = getEnvironmentvariable("CLOUD_CLIENT_ID", false);
			final String cloudApiClientSecret = getEnvironmentvariable("CLOUD_CLIENT_SECRET", false);

			final OAuthServiceAccountAuthenticationDetails cloudApiAuthenticationDetails = new OAuthServiceAccountAuthenticationDetails(
					cloudApiAuthenticationServerUrl, cloudApiAudience, cloudApiClientId, cloudApiClientSecret);

			// internal cloud APi
			final String internalCloudApiAuthenticationServerUrl = getEnvironmentvariable("INTERNAL_CLOUD_AUTHENTICATION_SERVER_URL",
					false);
			final String inernalCloudApiUrl = getEnvironmentvariable("INTERNAL_CLOUD_API_URL", false);
			final String internalCloudApiAudience = getEnvironmentvariable("INTERNAL_CLOUD_AUDIENCE", false);
			final String internalCloudApiClientId = getEnvironmentvariable("INTERNAL_CLOUD_CLIENT_ID", false);
			final String internalCloudApiClientSecret = getEnvironmentvariable("INTERNAL_CLOUD_CLIENT_SECRET", false);
			final String internalCloudApiUsername = getEnvironmentvariable("INTERNAL_CLOUD_USERNAME", false);
			final String internalCloudApiPassword = getEnvironmentvariable("INTERNAL_CLOUD_PASSWORD", false);

			final OAuthUserAccountAuthenticationDetails internalCloudApiAuthenticationDetails = new OAuthUserAccountAuthenticationDetails(
					internalCloudApiAuthenticationServerUrl, internalCloudApiAudience, internalCloudApiClientId,
					internalCloudApiClientSecret, internalCloudApiUsername, internalCloudApiPassword);

			// test report sheets
			final String sheetsApiKeyfileContent = getEnvironmentvariable("SHEETS_API_KEYFILE_CONTENT", false);
			final String reportSheetID = getEnvironmentvariable("REPORT_SHEET_ID", false);

			final String slackToken = getEnvironmentvariable("SLACK_TOKEN", false);
			final String slackChannel = getEnvironmentvariable("SLACK_CHANNEL", false);

			new Launcher(contactPoint, testOrchestrationAuthenticatonDetails, cloudApiUrl,
					cloudApiAuthenticationDetails, inernalCloudApiUrl, internalCloudApiAuthenticationDetails,
					sheetsApiKeyfileContent, reportSheetID, slackToken, slackChannel).launch();
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			System.exit(-1);
		}
	}

	private static String getEnvironmentvariable(String suffix, boolean optional) {
		Map<String, String> envVars = System.getenv();

		String key = PREFIX + suffix;

		if (!optional && !envVars.containsKey(key)) {
			throw new RuntimeException("Unable to find mandatory environment variable " + key);
		}

		return envVars.get(key);
	}

}
