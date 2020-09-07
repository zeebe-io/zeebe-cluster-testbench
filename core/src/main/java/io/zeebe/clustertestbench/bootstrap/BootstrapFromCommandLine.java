package io.zeebe.clustertestbench.bootstrap;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "bootstrap", mixinStandardHelpOptions = true, description = "Deploys workflows to the test orchestration Zeebe cluster and starts workers needed for the test orchestration", exitCodeListHeading = "Exit Codes:%n", exitCodeList = {
		" 0: Successful program execution", "-1: Unsuccessful program execution" })
public class BootstrapFromCommandLine implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(BootstrapFromCommandLine.class);

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

	public static void main(String[] args) {
		int exitCode = new CommandLine(new BootstrapFromCommandLine()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {

		logger.info("Bootstrapper starting");

		deriveMissingOptions();

		logger.info("Testbench cluster - contactPoint: " + contactPoint);
		logger.info("Testbench cluster - audience: " + audience);
		logger.info("Testbench cluster - clientId: " + clientId);

		if (authenticationServerUrl != null) {
			logger.info("Testbench cluster - authorizationServerUrl:" + authenticationServerUrl);
		}

		logger.info("Camunda cloud - API URL: " + cloudApiUrl);
		logger.info("Camunda cloud - audience: " + cloudApiAudience);
		logger.info("Camunda cloud - clientId: " + cloudApiClientId);
		logger.info("Camunda cloud - authorizationServerUrl:" + cloudApiAuthenticationServerUrl);

		final OAuthAuthenticationDetails testOrchestrationAuthenticatonDetails = new OAuthAuthenticationDetails(
				authenticationServerUrl, audience, clientId, clientSecret);
		final OAuthAuthenticationDetails cloudApiAuthenticationDetails = new OAuthAuthenticationDetails(
				cloudApiAuthenticationServerUrl, cloudApiAudience, cloudApiClientId, cloudApiClientSecret);

		try {
			new Launcher(contactPoint, testOrchestrationAuthenticatonDetails, cloudApiUrl,
					cloudApiAuthenticationDetails, reportSheetID, slackToken).launch();
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			System.exit(-1);
		}

		return 0;
	}

	private void deriveMissingOptions() {
		if (audience == null) {
			audience = contactPoint.substring(0, contactPoint.lastIndexOf(":"));
		}
	}

}
