// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.python

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.target.TargetProgressIndicator
import com.intellij.execution.target.TargetedCommandLineBuilder
import com.intellij.openapi.application.EDT
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.TaskCancellation
import com.intellij.platform.ide.progress.withModalProgress
import com.jetbrains.python.packaging.PyExecutionException
import com.jetbrains.python.run.PythonExecution
import com.jetbrains.python.run.prepareHelperScriptExecution
import com.jetbrains.python.run.target.HelpersAwareLocalTargetEnvironmentRequest
import com.jetbrains.python.sdk.PySdkSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.ApiStatus.Internal
import java.nio.file.Path
import kotlin.collections.iterator

/**
 * Creates a Python virtual environment, throws [ExecutionException] if creation process failed
 */
@Throws(PyExecutionException::class)
@Internal
suspend fun createVirtualenv(
  baseInterpreterPath: PythonBinary,
  venvRoot: Path,
  projectBasePath: Path,
  inheritSitePackages: Boolean = false,
) {

  val request = HelpersAwareLocalTargetEnvironmentRequest()

  val execution = prepareHelperScriptExecution(PythonHelper.VIRTUALENV_ZIPAPP, request) // todo what about legacy pythons?
  if (inheritSitePackages) {
    execution.addParameter("--system-site-packages")
  }

  execution.addParameter(venvRoot.toString())
  request.preparePyCharmHelpers()

  val targetEnvironment = request.targetEnvironmentRequest.prepareEnvironment(TargetProgressIndicator.EMPTY)


  val commandLineBuilder = TargetedCommandLineBuilder(targetEnvironment.request)
  commandLineBuilder.setWorkingDirectory(projectBasePath.toString())

  commandLineBuilder.setExePath(baseInterpreterPath.toString())

  execution.pythonScriptPath?.let { commandLineBuilder.addParameter(it.apply(targetEnvironment)) }
  ?: throw IllegalArgumentException("Python script path must be set")

  execution.parameters.forEach { parameter ->
    val resolvedParameter = parameter.apply(targetEnvironment)
    if (resolvedParameter != PythonExecution.SKIP_ARGUMENT) {
      commandLineBuilder.addParameter(resolvedParameter)
    }
  }


  for ((name, value) in execution.envs) {
    commandLineBuilder.addEnvironmentVariable(name, value.apply(targetEnvironment))
  }

  val targetedCommandLine = commandLineBuilder.build()



  val process = try {
    targetEnvironment.createProcess(targetedCommandLine)
  }
  catch (e: ExecutionException) {
    throw PyExecutionException(PyBundle.message("sdk.venv.error", e.localizedMessage), targetedCommandLine.collectCommandsSynchronously().joinToString(" "), emptyList())
  }

  val handler = CapturingProcessHandler(process, targetedCommandLine.charset, targetedCommandLine.getCommandPresentation(targetEnvironment))

  val result = withModalProgress(ModalTaskOwner.guess(), PyBundle.message("sdk.venv.process"), TaskCancellation.nonCancellable()) {
    withContext(Dispatchers.IO) {
      handler.runProcess()
    }
  }
  if (result.exitCode != 0) {
    throw PyExecutionException(PyBundle.message("sdk.venv.error", result.exitCode), targetedCommandLine.collectCommandsSynchronously().joinToString(" "), emptyList(), result)
  }

  withContext(Dispatchers.EDT) {
    PySdkSettings.instance.preferredVirtualEnvBaseSdk = baseInterpreterPath.toString()
  }
}