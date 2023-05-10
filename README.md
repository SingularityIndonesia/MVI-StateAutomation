# MVI + State Automation Example
Pada project kali ini, saya akan memberikan contoh dimana View direpresentasikan sepenuhnya oleh Model, dan State yang melakukan update secara otomatis. Automasi State bertujuan untuk melokalisasi State Logic di satu tempat (didalam state itu sendiri), dan membuat State sepenuhnya Immutable. Serta pattern MVI ditujukan supaya view sepenuhnya di representasikan oleh Model sebagai State.

Dalam proyek ini beberapa yang menjadi poin penting sebagai berikut:

### 1. Descendant Pattern
Sebuah model data (misalnya TodoModel) tidak dapat langsung merepresentasikan sebuah view, oleh karena view kadang memiliki beberapa property tambahan seperti **selected** , **active**, **clickable**, **suspended**, dan lain lain untuk mendukung interaksi. Oleh karena itu untuk MVI bisa dilakukan, Model harus di ubah menjadi model baru yang mendukung semua property yang di butuhkan sebuah view.

Dalam project ini saya memperkenalkan **Descendant** pattern yang bertujuan untuk membuat displayable-object dari sebuah **Model Data**. Descendant pattern adalah sebuah pattern dimana object memiliki **pointer** ke **Parent Object**. Descendant pattern bertujuan untuk menggambarkan inheritansi sebuah model dengan parent model, tanpa perlu melakukan abstraksi. Dengan demikian property-property pendukung dapat di tambahkan pada descendant, dan view akan dapat sepenuhnya direpresentasikan oleh Model tersebut.

### 2. State Automation
Salah satu problem utama dari Front End development adalah **Side Effect**, termasuk diantaranya **Behavioral Side Effect**. Dalam aplikasi frontend compleks yang memiliki banyak Side Effect termasuk juga Behavior Side Effect, menghandle UI state bisa jadi sangat sulit. Kesulitan untuk melakukan perubahan dan modifikasi pada kode, data flow yang rumit (Spagetty flow) adalah hal yang sering kita jumpai. Semua problem itu dapat terjadi hanya karena kita mengijinkan adanya Side Effect. Oleh karena itu, dalam project ini, kita akan menghilangkan side effect tersebut dengan melakukan **State Automation** serta menyederhanakannya dengan melakukan **Exclusive Sate Logic**.
State Automation adalah proses dimana sebuah state memiliki kemampuan untuk mengupdate dirinya sendiri. Sehingga, side effect tidak di perlukan untuk mengupdate state tersebut.

### 3. Exclusive State Logic
Untuk memastikan tidak ada Side Effect, sebuah state haruslah Immutable, dan logic dari sebuah state harus **Exclusive** (hanya di ketahui oleh state tersebut).

## Example
Berikut pattern yang saya rekomendasikan:
Contoh implementasi dapat anda lihat pada MainViewModel.kt
```kotlin
/**
* State Automation + Exclusive State Logic
**/
val someState: StateFlow<SomeModel> by lazy {
	val realState = MutableStateFlow(defaultValue)
	
	/** Exclusive State Logic **/
	suspend fun updateState() {
		
		... state logic goes here
		
		CoroutineScope {
			realState.emit(newState)
		}
	}

	/** State Relation
	* The state sense another state to notify update.
	**/
	run {
		CoroutineScope {
			anotherState1.collect {
				updateState()
			}
		}
		
		CoroutineScope {
			anotherState2.collect {
				updateState()
			}
		}
	}

	state // return private state object
}
```

Untuk menambahkan flexibility, anda juga bisa menambahkan updaterJob sebagai cancelable job, untuk mendukung interaksi yang lebih cepat.
```kotlin
/**
* State Automation + Exclusive State Logic
**/
val someState: StateFlow<SomeModel> by lazy {
	val realState = MutableStateFlow(defaultValue)
	var updaterJob: Job? = null
	
	/** Exclusive State Logic **/
	suspend fun updateState() {
		
		... state logic goes here
		
		CoroutineScope {
			realState.emit(newState)
		}
	}

	/** State Relation
	* The state sense another state to notify update.
	**/
	run {
		CoroutineScope {
			anotherState1.collect {
				updaterJob?.cancel()
				updaterJob = launch {
					updateState()
				}
			}
		}
		
		CoroutineScope {
			anotherState2.collect {
				updaterJob?.cancel()
				updaterJob = launch {
					updateState()
				}
			}
		}
	}

	state // return private state object
}
```

### Note:
Dalam project ini saya menggunakan Jetpack Compose, akan tetapi implementasi tidak terbatas pada UI toolkit.

### Author: [stefanus ayudha](https://github.com/stefanusayudha)
