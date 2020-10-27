package io.zeebe.clustertestbench.worker;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.zeebe.client.api.ZeebeFuture;
import io.zeebe.client.api.command.CompleteJobCommandStep1;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient;
import io.zeebe.clustertestbench.worker.DeleteGenerationInCamundaCloudWorker.Input;

@ExtendWith(MockitoExtension.class)
class DeleteGenerationInCamundaCloudWorkerTest {

	private static final String generationUUID = "test-generation-uuid";

	@Mock
	InternalCloudAPIClient mockInternalApiClient;

	@Mock
	JobClient mockJobClient;

	@Mock
	CompleteJobCommandStep1 mockCompleteJobCommandStep1;

	@SuppressWarnings("rawtypes")
	@Mock
	ZeebeFuture mockZeebeFuture;

	@Mock
	ActivatedJob mockActivatedJob;

	DeleteGenerationInCamundaCloudWorker sut;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void setUp() {
		sut = new DeleteGenerationInCamundaCloudWorker(mockInternalApiClient);
		when(mockJobClient.newCompleteCommand(Mockito.anyLong())).thenReturn(mockCompleteJobCommandStep1);
		when(mockCompleteJobCommandStep1.send()).thenReturn(mockZeebeFuture);

		var input = new Input();
		input.setGenerationUUID(generationUUID);

		when(mockActivatedJob.getVariablesAsType(Input.class)).thenReturn(input);
	}

	@Test
	void shouldCallApiToDeleteGeneration() throws Exception {
		// when
		sut.handle(mockJobClient, mockActivatedJob);

		// then
		verify(mockInternalApiClient).deleteGeneration(Mockito.any());
		verifyNoMoreInteractions(mockInternalApiClient);
	}

	@Test
	void shouldDeleteTheRightGeneration() throws Exception {
		// when
		sut.handle(mockJobClient, mockActivatedJob);

		// then
		verify(mockInternalApiClient).deleteGeneration(generationUUID);
		verifyNoMoreInteractions(mockInternalApiClient);
	}

	@Test
	void shouldCompleteJob() throws Exception {
		// when
		sut.handle(mockJobClient, mockActivatedJob);

		// then
		verify(mockJobClient).newCompleteCommand(Mockito.anyLong());
		verify(mockCompleteJobCommandStep1).send();
		verify(mockZeebeFuture).join();

		verifyNoMoreInteractions(mockJobClient);
		verifyNoMoreInteractions(mockCompleteJobCommandStep1);
		verifyNoMoreInteractions(mockZeebeFuture);
	}

	@Test
	void shouldCompleteJobAfterDeletingTheGeneration() throws Exception {
		// when
		sut.handle(mockJobClient, mockActivatedJob);

		// then
		var inOrder = inOrder(mockInternalApiClient, mockJobClient);

		inOrder.verify(mockInternalApiClient).deleteGeneration(Mockito.any());
		inOrder.verify(mockJobClient).newCompleteCommand(Mockito.anyLong());

		verifyNoMoreInteractions(mockInternalApiClient);
		verifyNoMoreInteractions(mockJobClient);
	}

	@Test
	void shouldCompleteTheRightJob() throws Exception {
		// given
		var jobKey = 42L;
		when(mockActivatedJob.getKey()).thenReturn(jobKey);

		// when
		sut.handle(mockJobClient, mockActivatedJob);

		// then
		verify(mockJobClient).newCompleteCommand(jobKey);
	}

}
