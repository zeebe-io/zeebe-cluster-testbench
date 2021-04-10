package io.zeebe.chaos

import org.junit.Test;
import org.junit.jupiter.api.Assertions
import java.io.File

class ProcessTest {

    @Test
    fun `Should Run`() {
        // given

        // when
        val exitCode =
            ProcessRunner().runProcess(
                listOf("echo", "Test"),
                File("/"),
                mapOf<String, String>(),
                null
            )

        // then
        Assertions.assertEquals(0, exitCode!!)
    }

    @Test
    fun `Should Fail`() {
        // given

        // when
        val exitCode =
            ProcessRunner().runProcess(listOf("cat", "1"), File("/"), mapOf<String, String>(), null)

        // then
        val failed = exitCode?.let { false } ?: true
        Assertions.assertTrue(failed)
    }

}
