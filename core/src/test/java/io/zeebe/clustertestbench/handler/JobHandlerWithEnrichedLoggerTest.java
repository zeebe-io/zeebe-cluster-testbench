package io.zeebe.clustertestbench.handler;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.zeebe.clustertestbench.handler.JobHandlerWithEnrichedLogger.LoggingEnricher;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.camunda.community.zeebe.testutils.stubs.ActivatedJobStub;
import org.camunda.community.zeebe.testutils.stubs.JobClientStub;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class JobHandlerWithEnrichedLoggerTest {

  @Test
  void shouldDelegateJobHandling() throws Exception {
    // given
    final var isDelegated = new AtomicBoolean(false);

    // when
    new JobHandlerWithEnrichedLogger((c, j) -> isDelegated.set(true))
        .handle(DataHelper.createClient(), DataHelper.createActivatedJob());

    // then
    assertThat(isDelegated).isTrue();
  }

  static class DataHelper {
    static JobClient createClient() {
      return new JobClientStub();
    }

    static ActivatedJob createActivatedJob() {
      final var job = new ActivatedJobStub(123);
      job.setType("testJobType");
      job.setProcessInstanceKey(9001);
      job.setInputVariables(
          Map.ofEntries(
              Map.entry("clusterId", "testClusterId"),
              Map.entry("clusterName", "testClusterName"),
              Map.entry("clusterPlan", "testClusterPlan"),
              Map.entry("clusterPlanUUID", "testClusterPlanUUID"),
              Map.entry("channel", "testChannel"),
              Map.entry("channelUUID", "testChannelUUID"),
              Map.entry("generation", "testGeneration"),
              Map.entry("generationUUID", "testGenerationUUID"),
              Map.entry("region", "testRegion"),
              Map.entry("regionUUID", "testRegionUUID"),
              Map.entry("zeebeImage", "testZeebeImage")));
      return job;
    }
  }

  @Nested
  class LoggingEnricherTest {

    @Test
    void shouldEnrichMDCWhenCreated() {
      // given
      final var job = DataHelper.createActivatedJob();

      // when
      try (@SuppressWarnings("unused")
          final var enricher = new LoggingEnricher(job)) {

        // then
        assertThat(MDC.getCopyOfContextMap())
            .containsExactlyInAnyOrderEntriesOf(
                Map.ofEntries(
                    Map.entry("jobType", "testJobType"),
                    Map.entry("processInstanceKey", "9001"),
                    Map.entry("clusterId", "testClusterId"),
                    Map.entry("clusterName", "testClusterName"),
                    Map.entry("clusterPlan", "testClusterPlan"),
                    Map.entry("clusterPlanUUID", "testClusterPlanUUID"),
                    Map.entry("channel", "testChannel"),
                    Map.entry("channelUUID", "testChannelUUID"),
                    Map.entry("generation", "testGeneration"),
                    Map.entry("generationUUID", "testGenerationUUID"),
                    Map.entry("region", "testRegion"),
                    Map.entry("regionUUID", "testRegionUUID"),
                    Map.entry("zeebeImage", "testZeebeImage")));
      }
    }

    @Test
    void shouldCleanupMDCWhenClosed() {
      // given
      final var job = DataHelper.createActivatedJob();
      final var enricher = new LoggingEnricher(job);

      // when
      enricher.close();

      // then
      assertThat(MDC.getCopyOfContextMap()).isEmpty();
    }
  }
}
