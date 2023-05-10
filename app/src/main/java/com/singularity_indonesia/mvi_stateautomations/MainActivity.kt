package com.singularity_indonesia.mvi_stateautomations

import android.os.Bundle
import android.support.v4.os.IResultReceiver.Default
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.singularity_indonesia.mvi_stateautomations.domain.model.TodoDisplay
import com.singularity_indonesia.mvi_stateautomations.domain.payload.GetTodoListPLD
import com.singularity_indonesia.mvi_stateautomations.util.enums.IDFilter
import com.singularity_indonesia.mvi_stateautomations.util.enums.Sorting
import com.singularity_indonesia.mvi_stateautomations.util.theme.MVIStateAutomationsTheme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initUI()
        initData()
    }

    private fun initUI() {
        setContent {
            MVIStateAutomationsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(vm)
                }
            }
        }
    }

    private fun initData() {
        startAutoUpdate()
    }

    /** this function will emulate data update every 5 sec **/
    private fun startAutoUpdate() {
        /** job **/
        var autoUpdateJob: Job? = null

        /** auto refresh logic **/
        suspend fun updater() {
            while (true) {
                vm.todoListDataProvider.update(
                    GetTodoListPLD()
                )
                delay(5000)
            }
        }

        /** flow with lifecycle **/
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onResume(
                    owner: LifecycleOwner
                ) {
                    super.onResume(owner)
                    autoUpdateJob?.cancel()
                    autoUpdateJob = lifecycleScope.launch {
                        updater()
                    }
                }

                override fun onStop(
                    owner: LifecycleOwner
                ) {
                    autoUpdateJob?.cancel()
                    super.onStop(owner)
                }
            }
        )
    }
}

@Composable
fun MainScreen(
    vm: MainViewModel = viewModel()
) = Column {
    Spacer(
        modifier = Modifier.height(24.dp)
            .fillMaxWidth()
    )
    Search(vm)
    Filter(vm)
    ListView(vm)
}

@Composable
fun Search(
    vm: MainViewModel = viewModel()
) = TextField(
    value = vm.nameFilter.collectAsState().value,
    onValueChange = { text ->
        CoroutineScope(
            Dispatchers.Default
        ).launch {
            vm.nameFilter.emit(text)
        }
    },
    label = { Text("Search") },
    modifier = Modifier.fillMaxWidth()
        .padding(
            horizontal = 16.dp,
            vertical = 0.dp
        )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Filter(
    vm: MainViewModel = viewModel()
) {
    Column(
        modifier = Modifier.padding(
            horizontal = 16.dp
        )
    ) {
        Row{
            FilterChip(
                selected = vm.idFilter.collectAsState().value == IDFilter.EvenOnly,
                label = { Text("Even ID Only") },
                onClick = {
                    if (vm.idFilter.value != IDFilter.EvenOnly)
                        CoroutineScope(Dispatchers.Main).launch {
                            vm.idFilter.emit(
                                IDFilter.EvenOnly
                            )
                        }
                    else
                        CoroutineScope(Dispatchers.Main).launch {
                            vm.idFilter.emit(
                                IDFilter.None
                            )
                        }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = vm.idFilter.collectAsState().value == IDFilter.OddOnly,
                label = { Text("Odd ID Only") },
                onClick = {
                    if (vm.idFilter.value != IDFilter.OddOnly)
                        CoroutineScope(Dispatchers.Main).launch {
                            vm.idFilter.emit(
                                IDFilter.OddOnly
                            )
                        }
                    else
                        CoroutineScope(Dispatchers.Main).launch {
                            vm.idFilter.emit(
                                IDFilter.None
                            )
                        }
                }
            )
        }
        Row{
            FilterChip(
                selected = vm.sortingMethod.collectAsState().value == Sorting.NameAsc,
                label = { Text("Sort Name Ascending") },
                onClick = {
                    if (vm.sortingMethod.value != Sorting.NameAsc)
                        CoroutineScope(Dispatchers.Main).launch {
                            vm.sortingMethod.emit(
                                Sorting.NameAsc
                            )
                        }
                    else
                        CoroutineScope(Dispatchers.Main).launch {
                            vm.sortingMethod.emit(
                                Sorting.None
                            )
                        }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = vm.sortingMethod.collectAsState().value == Sorting.NameDsc,
                label = { Text("Sort Name Descending") },
                onClick = {
                    if (vm.sortingMethod.value != Sorting.NameDsc)
                        CoroutineScope(Dispatchers.Main).launch {
                            vm.sortingMethod.emit(
                                Sorting.NameDsc
                            )
                        }
                    else
                        CoroutineScope(Dispatchers.Main).launch {
                            vm.sortingMethod.emit(
                                Sorting.None
                            )
                        }
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ListView(
    vm: MainViewModel = viewModel()
) {
    val list = vm.todoListDisplayable.collectAsState()

    return LazyColumn {
        list.value.map { todo ->
            item(todo.parent.invoke().id) {
                TodoItem(
                    item = todo
                ) { todo ->
                    CoroutineScope(
                        Dispatchers.Default
                    ).launch {
                        vm.selectedItem.emit(todo)
                    }
                }
            }
        }

        item {
            Spacer(
                modifier = Modifier.height(32.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodoItem(
    item: TodoDisplay,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    onClick: (TodoDisplay) -> Unit,
) = Card(
    modifier = Modifier.fillMaxWidth()
        .padding(16.dp, 8.dp, 16.dp, 0.dp),

    /** Card Color Driven by Model **/
    colors = CardDefaults.cardColors(
        containerColor = if (item.selected)
            MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.primaryContainer
    ),

    onClick = {
        onClick.invoke(item)
        keyboardController?.hide()
    }
) {
    val todoData = item.parent.invoke()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = todoData.title,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = todoData.detail
        )
        Text(
            text = "Updated at: ${todoData.lastModifiedTime}"
        )
    }
}