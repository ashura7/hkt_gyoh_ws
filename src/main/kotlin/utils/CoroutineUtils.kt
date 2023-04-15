package utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

val DEFAULT_DISPATCHER = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).asCoroutineDispatcher()

val GLOBAL_COROUTINE_SCOPE = CoroutineScope(DEFAULT_DISPATCHER)