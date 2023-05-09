package com.singularity_indonesia.mvi_stateautomations.domain.payload

import com.singularity_indonesia.mvi_stateautomations.util.pattern.Payload

data class GetTodoListPLD(
    val params: String = ""
): Payload {
}