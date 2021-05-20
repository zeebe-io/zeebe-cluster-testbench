package io.zeebe.chaos

import io.zeebe.client.ZeebeClient
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import io.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder
import org.slf4j.MDC
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private const val ENV_TESTBENCH_ADDRESS = "TESTBENCH_ADDRESS"
private const val ENV_TESTBENCH_CLIENT_ID = "TESTBENCH_CLIENT_ID"
private const val ENV_TESTBENCH_CLIENT_SECRET = "TESTBENCH_CLIENT_SECRET"
private const val ENV_TESTBENCH_AUTHORIZATION_SERVER_URL = "TESTBENCH_AUTHORIZATION_SERVER_URL"

private const val ROOT_PATH = "zeebe-chaos/chaos-experiments"
private const val SHELL_EXTENSION = "sh"
private const val EXPERIMENT_FILE_NAME = "experiment.json"

private val LOG = org.slf4j.LoggerFactory.getLogger("io.zeebe.chaos.ChaosWorker")

private fun createClient(): ZeebeClient {
    val audience = OAuthCredentialsProviderBuilder()
        .audience(System.getenv(ENV_TESTBENCH_ADDRESS).removeSuffix(":443"))
        .authorizationServerUrl(System.getenv(ENV_TESTBENCH_AUTHORIZATION_SERVER_URL))
        .clientId(System.getenv(ENV_TESTBENCH_CLIENT_ID))
        .clientSecret(System.getenv(ENV_TESTBENCH_CLIENT_SECRET))
        .build()

    return ZeebeClient.newClientBuilder()
        .credentialsProvider(audience)
        .gatewayAddress(System.getenv(ENV_TESTBENCH_ADDRESS))
        .numJobWorkerExecutionThreads(4)
        .build()
}

fun main() {
    // given
    showGitStatus()
    val zeebeClient = createClient()

    LOG.info("Connected to ${zeebeClient.configuration.gatewayAddress}")
    val topology = zeebeClient.newTopologyRequest().send().join()
    LOG.info("Topology: $topology")

    // register workers
    val scriptPath = File("$ROOT_PATH/scripts/")
    LOG.info("Fetch script from folder ${scriptPath.absolutePath}")

    scriptPath.listFiles { file -> file.extension == SHELL_EXTENSION }!!
        .map { it.name }
        .filterNot { it.contains("utils") }
        .filterNot { it.equals(DeployMultipleVersionsHandler.JOB_TYPE) }
        .forEach { script ->
            LOG.info("Start worker with type `$script`")
            zeebeClient.newWorker().jobType(script).handler(::handler).open()
        }

    zeebeClient.newWorker().jobType(DeployMultipleVersionsHandler.JOB_TYPE).handler(DeployMultipleVersionsHandler()).open()
    zeebeClient.newWorker().jobType("readExperiments").handler(::readExperiments).open()

    // keep workers running
    val latch = CountDownLatch(1)
    Runtime.getRuntime()
        .addShutdownHook(
            object : java.lang.Thread("Close thread") {
                override fun run() {
                    LOG.info("Received shutdown signal")
                    latch.countDown()
                }
            })

    latch.await()
}

fun readExperiments(client: JobClient, activatedjob: ActivatedJob) {
    setMDCForJob(activatedjob)
    val clusterPlan = activatedjob
        .variablesAsMap["clusterPlan"]!!
        .toString()
        .toLowerCase() // we expected lower case names
        .replace("\\s".toRegex(), "") // without spaces, like production-m

    LOG.info("Read experiments for cluster plan: $clusterPlan")

    val clusterPlanDir = File("$ROOT_PATH/camunda-cloud/$clusterPlan")
    val experiments = clusterPlanDir.listFiles()!!.map {
        Files.readString(File(it, EXPERIMENT_FILE_NAME).toPath())
    }

    client.newCompleteCommand(activatedjob.key).variables("{\"experiments\": $experiments}").send()
}

fun handler(client: JobClient, activatedjob: ActivatedJob) {
    val clusterId = activatedjob.variablesAsMap["clusterId"]!! as String
    setMDCForJob(activatedjob)

    val namespace = "$clusterId-zeebe"
    prepareForChaosExperiments(namespace)

    val provider = activatedjob.variablesAsMap["provider"]!! as Map<String, Any>
    val command = provider["path"]!!.toString()
    val scriptPath = File("$ROOT_PATH/scripts/")

    val commandList = createCommandList(scriptPath, command, provider)
    LOG.info("Commands to run: $commandList")

    val processBuilder = ProcessBuilder(commandList)
        .directory(scriptPath)
    processBuilder
        .environment()["NAMESPACE"] = namespace

    var timeoutInSeconds = 15 * 60L // per default 15 min timeout
    provider["timeout"]?.let {
        timeoutInSeconds = provider["timeout"].toString().toLong()
    }

    // redirects the error stream to the output stream
    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()
    // the input stream of the process object is connected to the output stream we want to consume, don't ask.
    consumeOutputStream(process.inputStream)
    val inTime = process.waitFor(timeoutInSeconds, TimeUnit.SECONDS)

    if (inTime && process.exitValue() == 0) {
        client.newCompleteCommand(activatedjob.key).send()
    } else {
        process.destroyForcibly()
    }
}

internal fun consumeOutputStream(inputStream: InputStream) {
    thread(start = true) {
        BufferedReader(InputStreamReader(inputStream, UTF_8)).use { reader ->
            reader.forEachLine {
                LOG.debug(it)
            }
        }
    }
}


private fun createCommandList(
    scriptPath: File,
    command: String,
    provider: Map<String, Any>
): MutableList<String> {
    val rootCommand = "${scriptPath.absolutePath}/$command"
    val commandList = mutableListOf(rootCommand)

    val args = provider["arguments"]
    args?.let {
        when (it) {
            is List<*> -> {
                commandList.addAll(it as List<String>)
            }
            is String -> {
                commandList.add(it)
            }
            else -> {
                // ?!
            }
        }
    }
    return commandList
}

/**
 * Get current status information of git zeebe-chaos repo.
 */
fun showGitStatus() {
    runCommands(File(ROOT_PATH), "git", "status")
}

/**
 * Prepares for running chaos experiments:
 *
 * * Switch the namespace to the target namespace (kubens "$NAMESPACE")
 * * deploy workers for chaos experiments
 *
 * Workers are needed in some of our chaos experiments.
 * Be aware that we are not delete them here, since if the experiments fails we might want to check
 * the logs of the workers AND they are deleted if we delete the namespace anyway.
 * kubectl apply -f worker.yaml &>> "$logFile"
 */
fun prepareForChaosExperiments(namespace: String) {
    LOG.info("Prepare chaos experiments.")

    // we should not use kubens when we want to scale our workers, it will change the shared context
    // runCommands(null, "kubens", namespace)
    val workerPath = File("$ROOT_PATH/camunda-cloud")
    runCommands(workerPath, "kubectl", "--namespace=$namespace", "apply", "--filename=worker.yaml")
}

fun runCommands(workingDir: File?, vararg commands: String) : Int {
    val processBuilder = ProcessBuilder(commands.asList())
    workingDir?.let {
        processBuilder.directory(workingDir)
    }
    val process = processBuilder.start()
    process.waitFor()
    LOG.info(
        "Run ${commands.contentToString()} \n {} {}",
        String(process.inputStream.readAllBytes()),
        String(process.errorStream.readAllBytes())
    )
    return process.exitValue()
}

internal fun setMDCForJob(job: ActivatedJob) {
    MDC.put("jobType", job.type)

    val clusterId = job.variablesAsMap["clusterId"]!! as String
    MDC.put("clusterId", clusterId)

    val clusterPlan = job.variablesAsMap["clusterPlan"]!! as String
    MDC.put("clusterPlan", clusterPlan)

    MDC.put("workflowInstanceKey", job.workflowInstanceKey.toString())
}
