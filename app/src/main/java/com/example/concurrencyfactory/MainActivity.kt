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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MyTest", "onCreate start")
        setContent { UI() }

        val job = scope.launch(start = CoroutineStart.LAZY) {
            Log.d("MyTest", "coroutine 1 start ${Thread.currentThread().name}")


            val job1 = launch {
                delay(1000)
                Log.d("MyTest", "coroutine 1.1 ${Thread.currentThread().name}")
            }

            val job2 = launch {
                delay(2000)
                Log.d("MyTest", "coroutine 1.2 ${Thread.currentThread().name}")
            }

            job1.join()
            job2.join()

            Log.d("MyTest", "coroutine 1 end ${Thread.currentThread().name}")
        }

        job.start()

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