package io.zeebe.chaos

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.JobClient
import io.camunda.zeebe.client.api.worker.JobHandler
import io.camunda.zeebe.model.bpmn.Bpmn
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.awaitility.pollinterval.FibonacciPollInterval.fibonacci
import java.util.concurrent.TimeUnit

class AwaitProcessWithResultHandler(val createClient: (ActivatedJob) -> ZeebeClient = ::createClientForClusterUnderTest) :
    JobHandler {

    companion object {
        const val JOB_TYPE = "await-processes-with-result.sh"

        private const val PROCESS_ID = "benchmark"
        private val PROCESS =
            Bpmn.createExecutableProcess(PROCESS_ID).name("One task process").startEvent("start")
                .serviceTask("task").zeebeJobType("benchmark-task").endEvent("ebd").done()
        private val LOG =
            org.slf4j.LoggerFactory.getLogger(AwaitProcessWithResultHandler::class.java.name)
    }

    override fun handle(client: JobClient, job: ActivatedJob) {
        LOG.info("Handle job $JOB_TYPE")

        createClient(job).use {
            LOG.info("Connected to ${it.configuration.gatewayAddress}")

            LOG.info("Deploying model")

            await.withPollInterval(fibonacci(TimeUnit.SECONDS)).until { ->
                it.deployModel(
                    PROCESS,
                    "one_task.bpmn"
                )
            }

            LOG.info("Creating process instance")
            await.withPollInterval(fibonacci(TimeUnit.SECONDS))
                .until { -> createInstanceWithResult(it) }
        }

        client.newCompleteCommand(job.key).send()
    }

    private fun createInstanceWithResult(
        client: ZeebeClient
    ): Boolean {
        return succeeds({ ->
            client.newCreateInstanceCommand().bpmnProcessId(PROCESS_ID).latestVersion()
                .withResult().send()
                .join()
        })
    }
}