package com.lee_idle.flowdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

class DemoViewModel : ViewModel() {
    // flowOf(2, 3, 4) 처럼 고정된 값의 집합으로도 가능
    // asFlow를 사용해 데이터를 플로로 변환할 수도 있다.
    // arrayOf<String>("one", "two", "three").asFlow()
    val myFlow: Flow<Int> = flow {
        // 생산자 블록
        for(i in 0..9){
            emit(i)
            delay(2000)
        }
    }

    val newFlow = myFlow.map{
        "Current value = $it"
    }

    // 콜드 플로는 shareIn() 함수를 호출해 핫 플로로 만들 수 있다
    val hotFlow = myFlow.shareIn(
        viewModelScope,
        replay = 1,
        started = SharingStarted.WhileSubscribed()
    )
    /*
        WhileSubscribed(): 활성화 상태의 구독자가 있는 한 해당 플로를 활성화 상태로 유지한다.
        Eagerly(): 활성화 상태의 구독자가 없어도 해당 플로는 즉시 활성화되고 그 상태를 유지한다.
        Lazily(): 첫 번째 소비자가 구독ㅇ르 하는 순간부터 플로가 시작되고, 활성화 상태의 구독자가
         없어도 플로는 황성화 상태를 유지한다.
     */

        /*
        .transform {
            emit("Value = $it")
            delay(1000)
            val doubled = it * 2
            emit("Value doubled = $doubled")
        }
         */
        /*
        .filter {
            it % 2 == 0
        }
        .map {
            "Current value = $it"
        }
         */

    fun doubleIt(value: Int) = flow {
        emit(value)
        delay(1000)
        emit(value + value)
    }

    private val _stateFlow = MutableStateFlow(0)
    val stateFlow = _stateFlow.asStateFlow()

    fun increaseValue() {
        _stateFlow.value += 1
    }


    private val _sharedFlow = MutableSharedFlow<Int>(
        replay = 10, // 최대 10개 저장
        onBufferOverflow = BufferOverflow.DROP_OLDEST // 오래된 것부터 버린다.
    )

    val sharedFlow = _sharedFlow.asSharedFlow()

    fun startSharedFlow() {
        viewModelScope.launch {
            for(i in 1..5){
                _sharedFlow.emit(i)
                delay(2000)
            }
        }
    }
}