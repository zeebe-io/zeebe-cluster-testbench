package io.zeebe.clustertestbench.bootstrap;

import java.net.URL;
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
		logger.info( "Bootstrapper starting");
		
		try {

			// test orchestration Zeebe cluster
			final String contactPoint = getEnvironmentvariable("CONTACT_POINT", false);

			final String authenticationServerUrl = getEnvironmentvariable("AUTHENTICATION_SERVER_URL", true);
			String audience = getEnvironmentvariable("AUDIENCE", true);

			if (audience == null) {
				audience = contactPoint.substring(0, contactPoint.lastIndexOf(":"));
			}

			final String clientId = getEnvironmentvariable("CLIENT_ID", false);
			final String clientSecret = getEnvironmentvariable("CLIENT_SECRET", false);

			final OAuthAuthenticationDetails testOrchestrationAuthenticatonDetails = new OAuthAuthenticationDetails(
					authenticationServerUrl, audience, clientId, clientSecret);

			// cloud API
			final String cloudApiUrl = getEnvironmentvariable("CLOUD_API_URL", false);
			final String cloudApiAuthenticationServerUrl = getEnvironmentvariable("CLOUD_AUTHENTICATION_SERVER_URL",
					false);

			final String cloudApiAudience = getEnvironmentvariable("CLOUD_AUDIENCE", false);
			final String cloudApiClientId = getEnvironmentvariable("CLOUD_CLIENT_ID", false);
			final String cloudApiClientSecret = getEnvironmentvariable("CLOUD_CLIENT_SECRET", false);

			final OAuthAuthenticationDetails cloudApiAuthenticationDetails = new OAuthAuthenticationDetails(
					cloudApiAuthenticationServerUrl, cloudApiAudience, cloudApiClientId, cloudApiClientSecret);

			// misc
			final String reportSheetID = getEnvironmentvariable("REPORT_SHEET_ID", false);
			final String slackToken = getEnvironmentvariable("SLACK_TOKEN", false);

			new Launcher(contactPoint, testOrchestrationAuthenticatonDetails, cloudApiUrl,
					cloudApiAuthenticationDetails, reportSheetID, slackToken).launch();
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
