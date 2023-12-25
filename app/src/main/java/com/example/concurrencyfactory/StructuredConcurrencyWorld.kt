package com.example.concurrencyfactory

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StructuredConcurrencyWorld(
    private val rootScope: CoroutineScope
) {

    init {
        val rootJob = rootScope.launch {
            runStructure()
        }

        CoroutineScope(Dispatchers.Default).launch {
            while (rootJob.isActive) {
                delay(1000)
                Log.d("MyTest", "rootJob.isActive")
            }
        }
    }

    private suspend fun runStructure() {
        with(rootScope) {

            val childJob1 = launch {
                repeat(1000) {
                    Log.d("MyTest", "1 coroutine: $it")
                    delay(1000)
                }
            }

            launch {
                repeat(1000) {
                    Log.d("MyTest", "2 coroutine: $it")
                    delay(1000)
                }
            }

            val deferred = async {
                repeat(5) {
                    Log.d("MyTest", "3 coroutine: $it")
                    delay(1000)
                }
                "AsyncResult"
            }

            val result = deferred.await()
            childJob1.cancel()

            Log.d("MyTest", "3 coroutine result: $result")

        }
    }

    /**
     * Отмена job родительской корутины отменяет выполнение всех дочерних корутин.
     * Отмена дочерней job не отменяет родительскую job.
     */
    fun cancelRootScope() {
        rootScope.cancel()
        Log.d("MyTest", "root scope canceled")
    }
}