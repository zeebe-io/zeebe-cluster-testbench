package io.zeebe.clustertestbench.bootstrap.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.testdriver.api.CamundaCloudAuthenticationDetails;
import io.zeebe.clustertestbench.testdriver.impl.CamundaCLoudAuthenticationDetailsImpl;

/**
 * Worker implementation for the {@code create-zeebe-cluster-job} service task.
 * Instead of creating a new cluster, it will look up the authentication details
 * of an existing cluster
 */
public class PreexistingClusterConnector implements JobHandler {
	private static final Logger logger = LoggerFactory.getLogger(PreexistingClusterConnector.class);

	private final Properties properties = new Properties();

	public PreexistingClusterConnector() throws FileNotFoundException, IOException {
		properties.load(new FileInputStream(new File("config.properties")));
	}

	@Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		String clusterPlan = (String) job.getVariablesAsMap().get("clusterPlan");

		logger.info( "PreexistingClusterConnector: looking up authentication details for " + clusterPlan);

		CamundaCloudAuthenticationDetails authenticationDetails = new AuthenticationDetailsBuilder(clusterPlan,
				properties).build();

		logger.info( "PreexistingClusterConnector: found authentication details " + authenticationDetails);

		client.newCompleteCommand(job.getKey()).variables(Map.of(CamundaCloudAuthenticationDetails.VARIABLE_KEY,
				authenticationDetails, "clusterId", authenticationDetails.getContactPoint())).send();
	}

	private static class AuthenticationDetailsBuilder {

		private final String prefix;
		private final Properties properties;

		private AuthenticationDetailsBuilder(String prefix, Properties properties) {
			this.prefix = prefix;
			this.properties = properties;
		}

		private CamundaCloudAuthenticationDetails build() {
			final CamundaCLoudAuthenticationDetailsImpl result = new CamundaCLoudAuthenticationDetailsImpl();

			result.setAudience(lookup(properties, prefix, "audience"));
			result.setAuthorizationURL(lookup(properties, prefix, "authorizationURL"));
			result.setClientId(lookup(properties, prefix, "clientId"));
			result.setClientSecret(lookup(properties, prefix, "clientSecret"));
			result.setContactPoint(lookup(properties, prefix, "contactPoint"));

			return result;
		}

		private static String lookup(Properties properties, String prefix, String key) {
			return properties.getProperty(prefix + "." + key);
		}
	}

}
