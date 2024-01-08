package com.example.concurrencyfactory.draft

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExceptionHandlingPlayground {

    private suspend fun getDataFromNetwork(): String {
        delay(1000)
        throw RuntimeException("network request failed")
    }

    /**
     * try-catch, если им обернуть suspend-функцию, которая бросает исключение, перехватит это исключение.
     */
    suspend fun getDataWithTryCatchAndSuspend(): String {
        try {
            return getDataFromNetwork()
        } catch (e: Exception) {
            Log.d("MyTest", "caught exception: ${e.message}")
        }
        return "Exception"
    }

    /**
     * try-catch, если им обернуть launch, который бросает исключение, НЕ перехватит это исключение.
     */
    suspend fun getDataWithTryCatchAndLaunch(scope: CoroutineScope): String {
        try {
            scope.launch {
                delay(1000)
                throw RuntimeException("coroutine failed")
            }
        } catch (e: Exception) {
            Log.d("MyTest", "caught exception: ${e.message}")
        }
        return "Exception"
    }

    /**
     * [CoroutineExceptionHandler] способен перехватить исключение, которое бросается из launch
     */
    suspend fun getDataExceptionHandlerAndLaunch(scope: CoroutineScope): String {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.d("MyTest", "exceptionHandler: ${throwable.message}")
        }
        scope.launch(exceptionHandler) {
            delay(1000)
            throw RuntimeException("coroutine failed")
        }
        return "Exception"
    }
}