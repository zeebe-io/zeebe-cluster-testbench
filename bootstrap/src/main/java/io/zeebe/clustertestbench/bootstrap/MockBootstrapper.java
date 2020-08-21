package io.zeebe.clustertestbench.bootstrap;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;

/**
 * This class registers mock workers for job types where the workers are not
 * implemented yet
 * 
 */
public class MockBootstrapper {

	private static final Logger logger = Logger.getLogger("io.zeebe.clustertestbench.bootstrap");

	private static final List<String> jobsToMock = Arrays.asList("create-zeebe-cluster-job", "run-simple-test-job",
			"record-test-result-job", "notify-engineers-job", "destroy-zeebe-cluster-job");

	private final ZeebeClient client;

	private final Map<String, JobWorker> registeredJobWorker = new HashMap<>();

	public MockBootstrapper(ZeebeClient client) {
		this.client = requireNonNull(client);
	}

	public void registerMockWorkers() {
		for (String jobType : jobsToMock) {
			logger.log(Level.INFO, "Registering mock job worker for:" + jobType);

			final JobWorker workerRegistration = client.newWorker().jobType(jobType).handler(new MoveAlongJobHandler())
					.timeout(Duration.ofSeconds(10)).open();

			registeredJobWorker.put(jobType, workerRegistration);

			logger.log(Level.INFO, "Job worker opened and receiving jobs.");
		}
	}

	public void stop() {
		registeredJobWorker.values().forEach(JobWorker::close);
	}

	private static class MoveAlongJobHandler implements JobHandler {
		@Override
		public void handle(final JobClient client, final ActivatedJob job) {
			logger.log(Level.INFO, job.toString());
			client.newCompleteCommand(job.getKey()).send().join();
		}
	}
}
