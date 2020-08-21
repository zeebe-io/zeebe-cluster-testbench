package io.zeebe.clustertestbench.bootstrap.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;

/**
 * Worker implementation for the {@code create-zeebe-cluster-job} service task.
 * Instead of creating a new cluster, it will look up the authentication details
 * of an existing cluster
 */
public class PreexistingClusterConnector implements JobHandler {
	private static final Logger logger = Logger.getLogger("io.zeebe.clustertestbench.bootstrap.mock");

	private final Properties properties = new Properties();

	public PreexistingClusterConnector() throws FileNotFoundException, IOException {
		properties.load(new FileInputStream(new File("config.properties")));
	}

	@Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		String clusterPlan = (String) job.getVariablesAsMap().get("clusterPlan");

		logger.log(Level.INFO, "PreexistingClusterConnector: looking up authentication details for " + clusterPlan);

		AuthenticationDetails authenticationDetails = new AuthenticationDetails(clusterPlan, properties);

		logger.log(Level.INFO, "PreexistingClusterConnector: found authentication details " + authenticationDetails);
		
		client.newCompleteCommand(job.getKey()).variables(authenticationDetails).send();
	}

	private static class AuthenticationDetails {

		private final String audience;
		private final String authorizationURL;
		private final String clientId;
		private final String clientSecret;
		private final String contactPoint;

		private AuthenticationDetails(String prefix, Properties properties) {
			audience = lookup(properties, prefix, "audience");
			authorizationURL = lookup(properties, prefix, "authorizationURL");
			clientId = lookup(properties, prefix, "clientId");
			clientSecret = lookup(properties, prefix, "clientSecret");
			contactPoint = lookup(properties, prefix, "contactPoint");
		}

		private static String lookup(Properties properties, String prefix, String key) {
			return properties.getProperty(prefix + "." + key);
		}

		public String getAudience() {
			return audience;
		}

		public String getAuthorizationURL() {
			return authorizationURL;
		}

		public String getClientId() {
			return clientId;
		}

		public String getClientSecret() {
			return clientSecret;
		}

		public String getContactPoint() {
			return contactPoint;
		}

		@Override
		public String toString() {
			return "AuthenticationDetails [audience=" + audience + ", authorizationURL=" + authorizationURL
					+ ", clientId=" + clientId + ", clientSecret=" + clientSecret + ", contactPoint=" + contactPoint
					+ "]";
		}
	}

}
