// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.python.junit5Tests.unit.alsoWin

import com.intellij.platform.eel.EelPlatform
import com.intellij.platform.eel.provider.asNioPath
import com.intellij.platform.eel.provider.localEel
import com.intellij.platform.eel.provider.utils.where
import com.intellij.python.community.execService.ExecService
import com.intellij.python.community.execService.WhatToExec
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.python.Result
import com.jetbrains.python.errorProcessing.PyError
import com.jetbrains.python.getOrThrow
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * How to use [ExecService]
 */
@TestApplication
class ExecServiceShowCase {

  private val eel = localEel // TODO: Check other eels

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun tesSunnyDay(useLocalPath: Boolean): Unit = timeoutRunBlocking {
    val execService = ExecService()

    val expectedPhrase = "Usage"
    val (command, args) = when (eel.platform) {
      is EelPlatform.Windows -> Pair("ping.exe", arrayOf("/?"))
      is EelPlatform.Posix -> {
        Pair("sh", arrayOf("-c", "echo $expectedPhrase"))
      }
    }

    val whatToExec = if (useLocalPath) {
      val fullPath = (eel.exec.where(command) ?: error("no $command found on $eel")).asNioPath()
      WhatToExec.Binary(fullPath)
    }
    else {
      WhatToExec.Command(eel, command)
    }

    val output = execService.execGetStdout(whatToExec, args.toList()).getOrThrow()
    assertThat("Command doesn't have expected output", output, CoreMatchers.containsString(expectedPhrase))
  }

  @Test
  fun testRainyDay(): Unit = timeoutRunBlocking {
    val arg = "foo"
    val command = WhatToExec.Command(eel, "Some_command_that_never_exists_on_any_machine${Math.random()}")
    when (val output = ExecService().execGetStdout(command, listOf(arg))) {
      is Result.Success -> fail("Execution of bad command should lead to an error")
      is Result.Failure -> {
        when (val err = output.error) {
          is PyError.Message -> fail("Execution of bad command should lead to an execution error, not bare message")
          is PyError.ExecException -> {
            val failure = err.execFailure
            assertEquals(command.command, failure.command, "Wrong command reported")
            assertEquals("foo", failure.args[0], "Wrong args reported")
          }
        }
      }
    }
  }
}