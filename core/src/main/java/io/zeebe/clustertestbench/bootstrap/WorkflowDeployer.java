package io.zeebe.clustertestbench.bootstrap;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
		List<File> workflowsToDeploy = getWorkflows(folderName);

		logger.info("Found workflows to deploy:" + workflowsToDeploy);

		for (File workflow : workflowsToDeploy) {
			try {
				String workflowName = workflow.getName();

				logger.info("Deploying " + workflowName);
				zeebeClient.newDeployCommand().addResourceFile(workflow.getAbsolutePath()).send().join();
			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
				success = false;
			}
		}

		return success;
	}

	protected List<File> getWorkflows(String path) throws IOException {

		File folder = new File(path);
		
		if (folder.exists()) {
			logger.error("Folder '" + path + "' does not exist");
		}

		return Arrays.asList(folder.listFiles(file -> file.getName().endsWith(".bpmn")));
	}

}
