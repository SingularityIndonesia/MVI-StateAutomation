package com.singularity_indonesia.mvi_stateautomations.util

import com.singularity_indonesia.mvi_stateautomations.util.pattern.DataProvider
import com.singularity_indonesia.mvi_stateautomations.util.pattern.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun <P : Payload, M> dataProvider(
    default: M,
    operator: suspend () -> M
): Lazy<DataProvider<P, M>> = lazy {

    object : DataProvider<P, M> {

        private val _state = MutableStateFlow(default)

        override val state: StateFlow<M> get() = _state

        override fun update(
            payload: P
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val data = operator.invoke()
                _state.emit(data)
            }
        }
    }
}