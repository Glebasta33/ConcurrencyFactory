package com.example.concurrencyfactory

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UI() }

        scope.launch {

            collectFlowAndCancel()

        }
    }
}

private suspend fun collectFlowAndCancel() = coroutineScope{
    val job = launch {
        /**
         * Consumer - потребитель данных. В холодных потоках потребитель (вызов collect)
         * запускает Producer и даёт ему команду начать эмитить данные в поток.
         */
        ColdFlowDataSource().getNumbers(5)
            .modifyFlow()
            .collect {
                Log.d("MyTest", "> $it")
            }
    }

    /**
     * Отмена корутины, внутри которой происходит коллектинг,
     * отменяет работу Flow (Producer перестаёт эмитить данные).
     */
    delay(3000)
    job.cancel()
}

/**
 * Цепочка терминальных операторов, модифицирующих значения Flow.
 */
private fun <T> Flow<T>.modifyFlow(): Flow<T> {
    map { "Number: $it" }
    onStart { Log.d("MyTest", "onStart") }
    catch { Log.d("MyTest", "caught exception: ${it.message}") }
    onCompletion { Log.d("MyTest", "onCompletion") }
        .also { return it }
}

@Composable
private fun UI() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Корутина (код внутри launch) выполняется одновременно (параллельно == concurrently) с кодом функции,
 * внутри которой была вызвана, не блокируя поток исполнения.
 */
private suspend fun launchCoroutinesConcurrently(scope: CoroutineScope) {
    scope.launch {
        repeat(3) {
            Log.d("MyTest", "Hello from coroutines! ${it + 1}")
            delay(1000)
        }
    }

    repeat(3) {
        Log.d("MyTest", "Hello from rest of code! ${it + 1}")
        delay(1000)
    }

    /**
     * 16:09:15.542  D  Hello from rest of code! 1
     * 16:09:15.611  D  Hello from coroutines! 1
     * 16:09:16.546  D  Hello from rest of code! 2
     * 16:09:16.617  D  Hello from coroutines! 2
     * 16:09:17.546  D  Hello from rest of code! 3
     * 16:09:17.624  D  Hello from coroutines! 3
     */
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

/**
 * Родительская корутина ждёт выполнения всех дочерних корутин.
 */
private suspend fun parentCoroutineWaitsAllChildToEnd(scope: CoroutineScope) {
    Log.d("MyTest", "scope start")
    val parentJob = scope.launch {
        repeat(10) { index ->
            launch {
                val millis = Random.nextInt(1, index + 10) * 1000
                delay(millis.toLong())
                Log.d("MyTest", "child coroutine finishes. millis: $millis")
            }
        }
    }

    parentJob.join()
    Log.d("MyTest", "scope finish")

    /**
     * 11:40:41.400  D  onCreate start
     * 11:40:41.440  D  onCreate end
     * 11:40:41.767  D  scope start
     * 11:40:42.852  D  child coroutine finishes. millis: 1000
     * 11:40:43.857  D  child coroutine finishes. millis: 2000
     * 11:40:44.854  D  child coroutine finishes. millis: 3000
     * 11:40:46.854  D  child coroutine finishes. millis: 5000
     * 11:40:47.849  D  child coroutine finishes. millis: 6000
     * 11:40:48.856  D  child coroutine finishes. millis: 7000
     * 11:40:49.850  D  child coroutine finishes. millis: 8000
     * 11:40:52.850  D  child coroutine finishes. millis: 11000
     * 11:40:54.853  D  child coroutine finishes. millis: 13000
     * 11:40:57.856  D  child coroutine finishes. millis: 16000
     * 11:40:57.858  D  scope finish
     */
}