// "Remove initializer from property" "true"
abstract class A {
    abstract var i = 0<caret>
}

// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.quickfix.RemovePartsFromPropertyFix
// FUS_K2_QUICKFIX_NAME: org.jetbrains.kotlin.idea.k2.codeinsight.fixes.RemovePartsFromPropertyFixFactory$RemovePartsFromPropertyFix