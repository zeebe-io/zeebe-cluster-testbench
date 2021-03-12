package io.zeebe.clustertestbench.bootstrap;

import io.zeebe.client.ZeebeClient;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowDeployer {
  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowDeployer.class);

  private final ZeebeClient zeebeClient;

  public WorkflowDeployer(final ZeebeClient zeebeClient) {
    this.zeebeClient = Objects.requireNonNull(zeebeClient);
  }

  protected boolean deployWorkflowsInClasspathFolder(final String folderName) throws IOException {
    boolean success = true;
    final List<File> workflowsToDeploy = getWorkflows(folderName);

    LOGGER.info("Found workflows to deploy: {}", workflowsToDeploy);

    for (final File workflow : workflowsToDeploy) {
      try {
        final String workflowName = workflow.getName();

        LOGGER.info("Deploying {}", workflowName);
        zeebeClient.newDeployCommand().addResourceFile(workflow.getAbsolutePath()).send().join();
      } catch (final Exception e) {
        LOGGER.error(e.getMessage(), e);
        success = false;
      }
    }

    return success;
  }

  protected List<File> getWorkflows(final String path) throws IOException {

    final File folder = new File(path);

    if (!folder.exists()) {
      LOGGER.error("Folder '{}' does not exist", path);
    }

    return Arrays.asList(folder.listFiles(file -> file.getName().endsWith(".bpmn")));
  }
}
