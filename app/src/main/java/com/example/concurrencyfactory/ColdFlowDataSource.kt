package com.example.concurrencyfactory

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class ColdFlowDataSource {

    /**
     * Producer. Поставляет данные во Flow.
     */
    fun getNumbers(amount: Int): Flow<Int> = flow {
        repeat(amount) { number ->
            delay(1000)
            emit(number)
            Log.d("MyTest", "In Provider: $number emitted ")
        }
        error("Error")
    }

    fun getStrings(amount: Int): Flow<String> = flow {
        repeat(amount) { index ->
            delay(1000)
            val string = Random.nextBits(index * 10).toString()
            emit(string)
        }
    }


}