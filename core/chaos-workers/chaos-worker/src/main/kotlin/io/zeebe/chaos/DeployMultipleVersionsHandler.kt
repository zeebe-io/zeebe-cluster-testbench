package io.zeebe.chaos;

import io.zeebe.client.ZeebeClient
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.response.DeploymentEvent
import io.zeebe.client.api.worker.JobClient
import io.zeebe.client.api.worker.JobHandler
import io.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder
import io.zeebe.model.bpmn.Bpmn
import org.awaitility.kotlin.await

class DeployMultipleVersionsHandler : JobHandler {

    private val PROCESS_ID = "multiVersion"
    private val RESOURCE_NAME = PROCESS_ID +".bpmn"
    private val LOG =
        org.slf4j.LoggerFactory.getLogger("io.zeebe.chaos.DeployMultipleVersionsHandler")

    companion object {
        const val JOB_TYPE = "deploy-different-versions.sh"
    }

    override fun handle(testbench: JobClient, job: ActivatedJob) {
        setMDCForJob(job)
        LOG.info("Handle job $JOB_TYPE")

        createClientForClusterUnderTest(job).use { clusterUnderTest ->
            LOG.info("Connected to ${clusterUnderTest.configuration.gatewayAddress}, start deploying multiple versions...")

            val lastVersion = IntRange(1, 10)
                    .map{i -> waitForModelDeployment(clusterUnderTest, i)}
                    .map{e -> e?.workflows?.get(0)?.version ?: -1 }
                    .last()

            if (lastVersion < 10) {
                LOG.warn("Deployed 10 different versions of process $PROCESS_ID, last version: $lastVersion. Fail $JOB_TYPE")
                testbench.newFailCommand(job.key)
                        .retries(job.retries)
                        .errorMessage("Expected to deploy 10 different versions of process $PROCESS_ID, but only deployed $lastVersion")
                        .send()
            } else {
                LOG.info("Deployed 10 different versions of process $PROCESS_ID, last version: $lastVersion. Complete $JOB_TYPE")
                testbench.newCompleteCommand(job.key).send()
            }
        }
    }

    private fun waitForModelDeployment(client: ZeebeClient, index: Int): DeploymentEvent? {
        var event: DeploymentEvent? = null
        await.untilAsserted {
            event = client.newDeployCommand()
                    .addWorkflowModel(
                            Bpmn.createExecutableProcess(PROCESS_ID)
                                    .name("Multi version process")
                                    .startEvent("start-" + index)
                                    .endEvent()
                                    .done(),
                            RESOURCE_NAME)
                    .send()
                    .join()
        }
        return event
    }

    private fun createClientForClusterUnderTest(job: ActivatedJob): ZeebeClient {
        val authenticationDetails = job.variablesAsMap["authenticationDetails"]!! as Map<String, Any>
        val clientId = authenticationDetails["clientId"]!!.toString()
        val clientSecret = authenticationDetails["clientSecret"]!!.toString()
        val authorizationURL = authenticationDetails["authorizationURL"]!!.toString()
        val audience = authenticationDetails["audience"]!!.toString()
        val contactPoint = authenticationDetails["contactPoint"]!!.toString()

        val credentialsProvider = OAuthCredentialsProviderBuilder()
            .audience(audience)
            .authorizationServerUrl(authorizationURL)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .credentialsCachePath("/tmp/$JOB_TYPE.cred")
            .build()

        return ZeebeClient.newClientBuilder()
            .credentialsProvider(credentialsProvider)
            .gatewayAddress(contactPoint)
            .build()
    }
}
