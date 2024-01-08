package com.example.concurrencyfactory.draft

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SharedFlowDataSource {

    private val _sharedFlow = MutableSharedFlow<Int>(
        replay = 0,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    val sharedFlow: SharedFlow<Int> = _sharedFlow.asSharedFlow()

    fun runSharedFlow(amount: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            repeat(amount) { index ->
                _sharedFlow.emit(index)
                delay(1000)
            }
        }
    }
}