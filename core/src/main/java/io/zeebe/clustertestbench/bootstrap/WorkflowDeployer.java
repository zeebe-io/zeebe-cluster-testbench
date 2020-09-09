package io.zeebe.clustertestbench.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.zeebe.client.ZeebeClient;

public class WorkflowDeployer {
	private static final Logger logger = LoggerFactory.getLogger(WorkflowDeployer.class);

	private final ZeebeClient zeebeClient;

	public WorkflowDeployer(ZeebeClient zeebeClient) {
		this.zeebeClient = Objects.requireNonNull(zeebeClient);
	}

	protected boolean deployWorkflowsInClasspathFolder(String folderName) throws IOException {
		boolean success = true;
		List<String> workflowsToDeploy = getResourceFiles(folderName);

		logger.info("Found workflows to deploy:" + workflowsToDeploy);

		for (String workflow : workflowsToDeploy) {
			try {
				logger.info("Deploying " + workflow);
				zeebeClient.newDeployCommand().addResourceFromClasspath(folderName + "/" + workflow).send().join();
			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
				success = false;
			}
		}

		return success;
	}

	protected List<String> getResourceFiles(String path) throws IOException {
		List<String> filenames = new ArrayList<>();

		try (InputStream in = getResourceAsStream(path);
				BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String resource;

			while ((resource = br.readLine()) != null) {
				filenames.add(resource);
			}
		}

		return filenames;
	}

	private InputStream getResourceAsStream(String resource) {
		final InputStream in = getContextClassLoader().getResourceAsStream(resource);

		return in == null ? getClass().getResourceAsStream(resource) : in;
	}

	private ClassLoader getContextClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

}
