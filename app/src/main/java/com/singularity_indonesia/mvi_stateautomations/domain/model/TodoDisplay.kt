package com.singularity_indonesia.mvi_stateautomations.domain.model

import com.singularity_indonesia.mvi_stateautomations.util.pattern.Descendant

data class TodoDisplay(
    override val parent: () -> Todo,
    val selected: Boolean,
    val lastModifiedTime: Long
) : Descendant<Todo>
