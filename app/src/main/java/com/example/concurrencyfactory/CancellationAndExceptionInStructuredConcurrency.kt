package com.example.concurrencyfactory

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CancellationAndExceptionInStructuredConcurrency {

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("MyTest", "CoroutineExceptionHandler: ${throwable.message}")
    }
    val rootScope = CoroutineScope(Dispatchers.Default)

    init {
        val rootJob = rootScope.launch {

            val job1 = launch {
                delay(3000)
                val job1Child1 = launch {
                    delay(3000)
                }

                val job1Child2 = launch {
                    delay(3000)
                }
                while (true) {
                    delay(1000)
                    Log.d("MyTest", "job1Child1.isActive: ${job1Child1.isActive}. job1Child2.isActive: ${job1Child2.isActive}")
                }
            }

            val job2 = launch {
                val job2Child1 = launch {
                    delay(3000)
                    /**
                     * Выброшенное в любой корутине исключение отменяет работу всей иерархии корутин
                     * и приводит к крашу приложения, если исключение не обработать.
                     */
                    throw RuntimeException()
                }

                val job2Child2 = launch {
                    delay(3000)
                }
                while (true) {
                    delay(1000)
                    Log.d("MyTest", "job2Child1.isActive: ${job2Child1.isActive}. job2Child2.isActive: ${job2Child2.isActive}")
                }
            }

            while (true) {
                delay(1000)
                Log.d("MyTest", "job1.isActive: ${job1.isActive}. job2.isActive: ${job2.isActive}")
            }

        }

    }

}