package com.example.concurrencyfactory.draft

import com.example.concurrencyfactory.draft.ColdFlowDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StateFlowViewModel {

    private val _stateFlow = MutableStateFlow(value = "")
    val stateFlow = _stateFlow.asStateFlow()

    init {
        /**
         * В большинстве случаев Producer StateFlow я вляется Consumer`ом какого-то другого Flow.
         * StateFlow - держетель стайта, отлично подходящий для экранов на UDF.
         */
        CoroutineScope(Dispatchers.IO).launch {
            ColdFlowDataSource().getStringsFlow(10).collect { stringValue ->
                _stateFlow.value = stringValue
            }
        }

    }

}