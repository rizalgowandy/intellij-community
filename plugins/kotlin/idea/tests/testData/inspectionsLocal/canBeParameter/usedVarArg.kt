// FIX: Remove 'val' from parameter

class Wrapper(vararg <caret>val x: Int) {
    val y = x
}

// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.inspections.CanBeParameterInspection$RemoveValVarFix