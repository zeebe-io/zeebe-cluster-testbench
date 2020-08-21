package io.zeebe.clustertestbench.bootstrap.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;

/**
 * Worker implementation for the {@code create-zeebe-cluster-job} service task.
 * Instead of creating a new cluster, it will look up the authentication details
 * of an existing cluster
 */
public class PreexistingClusterConnector implements JobHandler {
	
	private final Properties properties = new Properties();

	public PreexistingClusterConnector() throws FileNotFoundException, IOException {
		properties.load(new FileInputStream(new File("config.properties")));
	}

	@Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		
		String clusterPlan = (String)job.getVariablesAsMap().get("clusterPlan");
		
		AuthenticationDetails authenticationDetails = new AuthenticationDetails(clusterPlan, properties);

		client.newCompleteCommand(job.getKey()).variables(authenticationDetails).send();
	}
	
	private static class AuthenticationDetails {
		
		private final String audience;
		private final String authenticationURL;
		private final String clientId;
		private final String clientSecret;
		private final String contactPoint;
		
		private AuthenticationDetails(String prefix, Properties properties) {
			audience=lookup(properties, prefix, "audience");
			authenticationURL=lookup(properties, prefix, "authenticationURL");
			clientId=lookup(properties, prefix, "clientId");
			clientSecret=lookup(properties, prefix, "clientSecret");
			contactPoint=lookup(properties, prefix, "contactPoint");
		}

		private static String lookup(Properties properties, String prefix, String key) {
			return properties.getProperty(prefix + "." + key);
		}

		public String getAudience() {
			return audience;
		}

		public String getAuthenticationURL() {
			return authenticationURL;
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
	}
	
	
}
