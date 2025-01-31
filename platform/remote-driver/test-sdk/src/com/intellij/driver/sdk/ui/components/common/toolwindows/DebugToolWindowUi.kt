package com.intellij.driver.sdk.ui.components.common.toolwindows

import com.intellij.driver.sdk.ui.components.ComponentData
import com.intellij.driver.sdk.ui.components.UiComponent
import com.intellij.driver.sdk.ui.components.common.IdeaFrameUI

class DebugToolWindowUi(data: ComponentData) : UiComponent(data) {
  val consoleTab get() = x { and(byClass("SimpleColoredComponent"), byAccessibleName("Console")) }
  val threadsAndVariablesTab get() = x { and(byClass("SimpleColoredComponent"), byAccessibleName("Threads & Variables")) }

  val consoleView get() = x { byType("com.intellij.execution.impl.ConsoleViewImpl") }
}

fun IdeaFrameUI.debugToolWindow(func: DebugToolWindowUi.() -> Unit = {}) =
  x(DebugToolWindowUi::class.java) { componentWithChild(byClass("InternalDecoratorImpl"), byAccessibleName("Threads & Variables")) }.apply(func)

