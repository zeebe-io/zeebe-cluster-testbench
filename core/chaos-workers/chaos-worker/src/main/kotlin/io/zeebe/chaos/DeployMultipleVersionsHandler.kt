package io.zeebe.chaos;

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.JobClient
import io.camunda.zeebe.client.api.worker.JobHandler
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder
import io.camunda.zeebe.model.bpmn.Bpmn
import io.camunda.zeebe.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.xml.ModelInstance

class DeployMultipleVersionsHandler : JobHandler {

    private val PROCESS_ID = "multiVersion"
    private val MODEL_V1 = Bpmn.createExecutableProcess(PROCESS_ID).name("v1").startEvent().endEvent().done()
    private val MODEL_V2 = Bpmn.createExecutableProcess(PROCESS_ID).name("v2").startEvent().endEvent().done()
    private val LOG = org.slf4j.LoggerFactory.getLogger("io.zeebe.chaos.DeployMultipleVersionsHandler")

    companion object {
        const val JOB_TYPE = "deploy-different-versions.sh"
    }

    override fun handle(client: JobClient, job: ActivatedJob) {
        LOG.info("Handle job $JOB_TYPE")

        val authenticationDetails = job.variablesAsMap["authenticationDetails"]!! as Map<String, Any>
        createClient(authenticationDetails).use {
            LOG.info("Connected to ${it.configuration.gatewayAddress}, start deploying multiple versions...")

            var lastVersion = -1
            for (i in 1..5) {
                deployModel(it, MODEL_V1, "modelV1.bpmn")
                lastVersion = deployModel(it, MODEL_V2, "modelV2.bpmn")
            }

            LOG.info("Deployed 10 different versions of process $PROCESS_ID, last version: $lastVersion. Complete $JOB_TYPE")
        }

        client.newCompleteCommand(job.key).send()
    }

    private fun deployModel(client: ZeebeClient, model: BpmnModelInstance, name: String) : Int{
        var version = -1
        do {
            try {
                val deploymentEvent = client.newDeployCommand().addProcessModel(model, name).send().join()
                version = deploymentEvent.processes[0].version
            } catch (e: Exception) {
                // try again
                LOG.debug("Failed to deploy $name, try again.", e)
                Thread.sleep(100)
            }
        } while (version == -1)
        return version
    }


    private fun createClient(authenticationDetails: Map<String, Any>): ZeebeClient {
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
