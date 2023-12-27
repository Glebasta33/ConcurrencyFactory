package com.example.concurrencyfactory

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
}