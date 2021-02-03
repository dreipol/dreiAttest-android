package ch.dreipol.dreiattest.multiplatform.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun launchAndWait(block: suspend () -> Unit) {
    runBlocking { GlobalScope.launch { block.invoke() }.join() }
}