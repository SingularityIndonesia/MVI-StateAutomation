package com.singularity_indonesia.mvi_stateautomations.util.pattern

interface Payload : JsonConvertible {
    fun getQueries(): Map<String, String> {
        TODO()
    }

    fun getFields(): Map<String, String> {
        TODO()
    }

    fun getBody(): Map<String, String> {
        TODO()
    }
}