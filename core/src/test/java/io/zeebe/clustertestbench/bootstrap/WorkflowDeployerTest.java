package io.zeebe.clustertestbench.bootstrap;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.zeebe.client.ZeebeClient;

@ExtendWith(MockitoExtension.class)
class WorkflowDeployerTest {	
	
	@Mock
	ZeebeClient mockZeebeClient;
	
	private WorkflowDeployer sutWorkflowDeployer;
	
	@BeforeEach
	public void setUp() {
		sutWorkflowDeployer = new WorkflowDeployer(mockZeebeClient);
	}

	@Test
	void shouldFindWorkflowsInClasspath() throws IOException {
		//given
		List<String> workflowFiles = sutWorkflowDeployer.getResourceFiles("workflows");
		
		// then
		Assertions.assertThat(workflowFiles).contains("run-all-tests-in-camunda-cloud.bpmn");				
	}

}
