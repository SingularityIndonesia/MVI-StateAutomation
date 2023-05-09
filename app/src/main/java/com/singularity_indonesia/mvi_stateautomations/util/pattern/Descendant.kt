package com.singularity_indonesia.mvi_stateautomations.util.pattern

interface Descendant<M> {
    val parent: () -> M
}