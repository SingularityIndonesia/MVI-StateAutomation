package com.singularity_indonesia.mvi_stateautomations.util.pattern

import kotlinx.coroutines.flow.StateFlow

interface DataProvider<P,M> {
    fun update(payload: P)
    val state: StateFlow<M>
}