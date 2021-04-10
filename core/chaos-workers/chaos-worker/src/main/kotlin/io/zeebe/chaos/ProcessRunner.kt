package io.zeebe.chaos;

import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class ProcessRunner {

    fun runProcess(
        commands: List<String>,
        workingDir: File,
        envVariables: Map<String, String>,
        timeout: Long?
    ): Int? {
        val processBuilder = ProcessBuilder(commands)
            .directory(workingDir)
            .inheritIO()
        processBuilder
            .environment().putAll(envVariables)

        var timeoutInSeconds = 15 * 60L // per default 15 min timeout
        timeout?.let {
            timeoutInSeconds = timeout
        }


        val process = processBuilder.start()
        val inTime = process.waitFor(timeoutInSeconds, TimeUnit.SECONDS)

        return if (inTime && process.exitValue() == 0)
            process.exitValue()
        else
            null
    }
}
