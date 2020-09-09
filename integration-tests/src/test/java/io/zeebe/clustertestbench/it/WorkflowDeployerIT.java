package io.zeebe.clustertestbench.it;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.zeebe.client.ZeebeClient;
import io.zeebe.clustertestbench.bootstrap.WorkflowDeployer;

@ExtendWith(MockitoExtension.class)
class WorkflowDeployerIT {

	@Mock
	ZeebeClient mockZeebeClient;

	private TestWorkflowDeployer sutWorkflowDeployer;

	@BeforeEach
	public void setUp() {
		sutWorkflowDeployer = new TestWorkflowDeployer(mockZeebeClient);
	}

	/**
	 * This test exists because in one of the first iterations the workflow deployer
	 * was able to find resources when started from the IDE, but found no resources
	 * when being started in the JAR. Most likely cause with different behavior of
	 * different class loaders
	 * 
	 */
	@Test
	void shouldFindWorkflowsInClasspath() throws IOException {
		// given
		List<String> workflowFiles = sutWorkflowDeployer.getResourceFiles("workflows");

		// then
		Assertions.assertThat(workflowFiles).contains("run-all-tests-in-camunda-cloud.bpmn");
	}

	private static final class TestWorkflowDeployer extends WorkflowDeployer {

		public TestWorkflowDeployer(ZeebeClient zeebeClient) {
			super(zeebeClient);
		}

		@Override
		public List<String> getResourceFiles(String path) throws IOException {
			return super.getResourceFiles(path);
		}
	}

}
