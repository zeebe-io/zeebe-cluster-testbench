package io.zeebe.clustertestbench.bootstrap;

import io.zeebe.client.ZeebeClient;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessDeployer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDeployer.class);

  private final ZeebeClient zeebeClient;

  public ProcessDeployer(final ZeebeClient zeebeClient) {
    this.zeebeClient = Objects.requireNonNull(zeebeClient);
  }

  protected boolean deployProcessesInClasspathFolder(final String folderName) throws IOException {
    boolean success = true;
    final List<File> processesToDeploy = getProcesses(folderName);

    LOGGER.info("Found processes to deploy: {}", processesToDeploy);

    for (final File process : processesToDeploy) {
      try {
        final String processName = process.getName();

        LOGGER.info("Deploying {}", processName);
        zeebeClient.newDeployCommand().addResourceFile(process.getAbsolutePath()).send().join();
      } catch (final Exception e) {
        LOGGER.error(e.getMessage(), e);
        success = false;
      }
    }

    return success;
  }

  protected List<File> getProcesses(final String path) throws IOException {

    final File folder = new File(path);

    if (!folder.exists()) {
      LOGGER.error("Folder '{}' does not exist", path);
    }

    return Arrays.asList(folder.listFiles(file -> file.getName().endsWith(".bpmn")));
  }
}
