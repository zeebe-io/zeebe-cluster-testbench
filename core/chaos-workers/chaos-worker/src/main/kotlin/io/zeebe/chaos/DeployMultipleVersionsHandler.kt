package io.zeebe.chaos

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.response.DeploymentEvent
import io.camunda.zeebe.client.api.worker.JobClient
import io.camunda.zeebe.client.api.worker.JobHandler
import io.camunda.zeebe.model.bpmn.Bpmn
import org.awaitility.kotlin.await

class DeployMultipleVersionsHandler(val createClient: (ActivatedJob) -> ZeebeClient = ::createClientForClusterUnderTest) :
    JobHandler {

    private val PROCESS_ID = "multiVersion"
    private val RESOURCE_NAME = PROCESS_ID +".bpmn"
    private val LOG =
        org.slf4j.LoggerFactory.getLogger("io.zeebe.chaos.DeployMultipleVersionsHandler")

    companion object {
        const val JOB_TYPE = "deploy-different-versions.sh"
    }

    override fun handle(client: JobClient, job: ActivatedJob) {
        setMDCForJob(job)
        LOG.info("Handle job $JOB_TYPE")

        createClient(job).use {
            LOG.info("Connected to ${it.configuration.gatewayAddress}, start deploying multiple versions...")

            val lastVersion = IntRange(1, 10)
                    .map{i -> waitForModelDeployment(it, i)}
                    .map{e -> e?.processes?.get(0)?.version ?: -1 }
                    .last()

            LOG.info("Deployed 10 different versions of process $PROCESS_ID, last version: $lastVersion. Complete $JOB_TYPE")
        }

        client.newCompleteCommand(job.key).send()
    }

    private fun waitForModelDeployment(client: ZeebeClient, index: Int): DeploymentEvent? {
        var event: DeploymentEvent? = null
        await.untilAsserted {
            event = client.newDeployCommand()
                    .addProcessModel(
                            Bpmn.createExecutableProcess(PROCESS_ID)
                                    .name("v1")
                                    .startEvent("start-" + index)
                                    .endEvent()
                                    .done(),
                            RESOURCE_NAME)
                    .send()
                    .join()
        }
        return event
    }
}
