package com.example.concurrencyfactory

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MyTest", "onCreate start")
        setContent { UI() }

        scope.launch {
            runTwoAsyncAndMapResult(scope)
        }

        Log.d("MyTest", "onCreate end")
    }
}

@Composable
private fun UI() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }
}

/**
 * join() приостанавливает выполнение корутины, внутри которой был вызван,
 * и ожидает завершения выполнения корутины, у который был вызван.
 */
private suspend fun launch2CoroutinesAndJoin(scope: CoroutineScope) {
    Log.d("MyTest", "coroutine 1 start ${Thread.currentThread().name}")

    val job1 = scope.launch {
        delay(1000)
        Log.d("MyTest", "coroutine 1.1 ${Thread.currentThread().name}")
    }

    val job2 = scope.launch {
        delay(2000)
        Log.d("MyTest", "coroutine 1.2 ${Thread.currentThread().name}")
    }

    job1.join()
    job2.join()

    Log.d("MyTest", "coroutine 1 end ${Thread.currentThread().name}")

    /**
     * 11:27:54.798  D  onCreate start
     * 11:27:54.839  D  onCreate end
     * 11:27:55.174  D  coroutine 1 start main
     * 11:27:56.238  D  coroutine 1.1 main
     * 11:27:57.243  D  coroutine 1.2 main
     * 11:27:57.244  D  coroutine 1 end main
     *
     */
}

/**
 *  await() как и join() приостанавливает выполнение корутины, внутри которой был вызван,
 * и ожидает завершения выполнения корутины, у который был вызван.
 */
private suspend fun runTwoAsyncAndMapResult(scope: CoroutineScope) {
    Log.d("MyTest", "coroutine 1 start ${Thread.currentThread().name}")

    val deferred1: Deferred<String> = scope.async {
        delay(1000)
        Log.d("MyTest", "coroutine 1.1 ${Thread.currentThread().name}")
        "Ivan"
    }

    val deferred2: Deferred<Int> = scope.async {
        delay(2000)
        Log.d("MyTest", "coroutine 1.2 ${Thread.currentThread().name}")
        33
    }

    val mappedResult = mapOf(deferred1.await() to deferred2.await())

    Log.d("MyTest", "2 async result: $mappedResult")

    Log.d("MyTest", "coroutine 1 end ${Thread.currentThread().name}")

    /**
     * 11:25:58.235  D  onCreate start
     * 11:25:58.276  D  onCreate end
     * 11:25:58.616  D  coroutine 1 start main
     * 11:25:59.685  D  coroutine 1.1 main
     * 11:26:00.682  D  coroutine 1.2 main
     * 11:26:00.687  D  2 async result: {Ivan=33}
     * 11:26:00.687  D  coroutine 1 end main
     */
}