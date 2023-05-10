package com.singularity_indonesia.mvi_stateautomations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularity_indonesia.mvi_stateautomations.domain.model.Todo
import com.singularity_indonesia.mvi_stateautomations.domain.model.TodoDisplay
import com.singularity_indonesia.mvi_stateautomations.domain.payload.GetTodoListPLD
import com.singularity_indonesia.mvi_stateautomations.util.dataProvider
import com.singularity_indonesia.mvi_stateautomations.util.enums.IDFilter
import com.singularity_indonesia.mvi_stateautomations.util.enums.Sorting
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    /** ## Data Source **/
    val todoListDataProvider by dataProvider<GetTodoListPLD, List<Todo>>(
        default = listOf(),
        operator = suspend {
            /**
             * ## Fake Operator
             * We will focus on the presentation, so we will leave the data layer dummy.
             * This fake operator will emmit fake list.
             **/
            (1..100).map {
                Todo(
                    id = "$it",
                    title = "Title $it",
                    detail = "Detail $it",
                    lastModifiedTime = System.currentTimeMillis()
                )
            }
        }
    )

    /** ## Name Filter **/
    val nameFilter = MutableStateFlow<String>("")

    /** ## ID Filter **/
    val idFilter = MutableStateFlow<IDFilter>(IDFilter.None)

    /** ## Sorting **/
    val sortingMethod = MutableStateFlow<Sorting>(Sorting.None)

    /** ## Selected Data **/
    val selectedItem = MutableStateFlow<TodoDisplay?>(null)

    /** ## Displayable data with Automated state
     * This state will sense another states such: Data Flow, Selected Item, and Filters, and update itself.
     * The goal is to keep the state immutable and eliminate side effects.
     * **/
    val todoListDisplayable: StateFlow<List<TodoDisplay>> by lazy {
        /** real state **/
        val state = MutableStateFlow(listOf<TodoDisplay>())
        var updaterJob: Job? = null

        /** AUTOMATION FUNCTION , EXCLUSIVE STATE LOGIC **/
        suspend fun updateState() {

            val selected = selectedItem.first()
            val clueFilter = nameFilter.first()
            val idFilter = idFilter.first()
            val sorting = sortingMethod.first()
            val data = todoListDataProvider.state.first()

            val newData = data
                /**
                 * if name filter is blank then let everybody join the party,
                 * else name or data contain clue
                 * **/
                .filter {
                    if (clueFilter.isBlank())
                        return@filter true
                    else
                        it.title.contains(clueFilter, true)
                                || it.detail.contains(clueFilter, true)
                }
                /** ID Filter **/
                .filter {
                    when (idFilter) {
                        IDFilter.EvenOnly -> {
                            it.id.toInt() % 2 == 0
                        }

                        IDFilter.OddOnly -> {
                            it.id.toInt() % 2 != 0
                        }

                        IDFilter.None -> true
                    }
                }
                /**
                 * map item to displayable item
                 */
                .map {
                    TodoDisplay(
                        parent = { it },
                        selected = false, // selected is false by default
                        lastModifiedTime = System.currentTimeMillis()
                    )
                }
                /**
                 * attach extra properties
                 * if selected item id == current item id, set selected to true
                 * **/
                .map {
                    if (it.parent.invoke().id == selected?.parent?.invoke()?.id)
                        it.copy(
                            selected = true
                        )
                    else
                        it
                }
                /** Sorting **/
                .let {
                    when (sorting) {
                        Sorting.NameAsc -> it.sortedBy { todo ->
                            todo.parent.invoke().id
                        }

                        Sorting.NameDsc -> it.sortedBy {todo ->
                            todo.parent.invoke().id
                        }.reversed()

                        Sorting.None -> it
                    }
                }

            /** update current state **/
            state.emit(newData)
        }

        /** state relation sensing **/
        run {
            viewModelScope.launch {
                todoListDataProvider.state.collect {
                    updaterJob?.cancel()
                    updaterJob = launch {
                        updateState()
                    }
                }
            }

            viewModelScope.launch {
                nameFilter.collect {
                    updaterJob?.cancel()
                    updaterJob = launch {
                        updateState()
                    }
                }
            }

            viewModelScope.launch {
                selectedItem.collect {
                    updaterJob?.cancel()
                    updaterJob = launch {
                        updateState()
                    }
                }
            }

            viewModelScope.launch {
                idFilter.collect {
                    updaterJob?.cancel()
                    updaterJob = launch {
                        updateState()
                    }
                }
            }

            viewModelScope.launch {
                sortingMethod.collect {
                    updaterJob?.cancel()
                    updaterJob = launch {
                        updateState()
                    }
                }
            }
        }

        state
    }

}