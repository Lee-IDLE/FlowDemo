package com.lee_idle.flowdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.lee_idle.flowdemo.ui.theme.FlowDemoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlin.system.measureTimeMillis

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlowDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        ScreenSetup()
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenSetup(viewModel: DemoViewModel = DemoViewModel()) {
    MainScreen(viewModel, viewModel.newFlow)
}

@Composable
fun MainScreen(viewModel: DemoViewModel, flow: Flow<String>) {
    // collect() 함수를 사용한 방법
    var count by remember {
        mutableStateOf<String>("Current value =")
    }

    val count2 by viewModel.stateFlow.collectAsState()

    val count3 by viewModel.sharedFlow.collectAsState(initial = 0)

    LaunchedEffect(key1 = Unit) {
        flow.collect {
            count = it
        }

        // 스트림이 종료되는 시점에 실행되는 코드는 try/finnaly 구조 사용
        /*
        try{
            flow.collect{
                count = it
            }
        }finally {
            count = "Flow stream ended"
        }
         */

        val elapsedTime = measureTimeMillis {
            // buffer를 사용하면 소비자가 이전에 방출된 값을 모두 처리하는 동안에도 생산자가 값을 방출하고,
            // 방출된 모든 값이 수집됨을 보장한다
            flow.buffer()
                .collect {
                delay(1000)
            }

            // reduce() 연산자는 컬렉션 연산자를 대신해 플로 데이터를 변경하기 위해 사용하는 연산자 중 하나다.
            /*
            flow.reduce { accumulator, value ->
                count = accumulator
                accumulator + value
            }
             */

            // fold() 연산자는 reduce() 연산자와 유사하나 accumulator의 초깃값을 전달한다
            /*
            flow.fold(10) { accumulator, value ->
                count = accumulator
                accumulator + value
            }
             */
        }
        count = "Duration = $elapsedTime"

        // flatMapConcat을 사용한 플로우 평탄화 (flow안에 flow가 있는 경우에 사용)
        // doubleIt 호출이 동기적으로 실행되며, doubleIt가 두 값을 모두 방출할 때까지 기다린 뒤 다음 플로값을 처리
        // flatMapMerge를 사용해 비동기적으로 수집할 수 있다
        /* count 값을 Int로 바꾸고 난 후 사용
        viewModel.myFlow
            .flatMapConcat {viewModel.doubleIt(it) }
            .flatMapMerge { viewModel.doubleIt(it) }
            .collect { count = it}
         */

        // zip()과 combine() 연산자를 사용해 여러 플로를 단일 프롤로 조합할 수 있다
        // zip: 두 플로 모두가 새로운 값을 방출한 뒤 수집을 수행
        // combine: 두 플로 중 한 플로가 새로운 값을 방출할 때, 다른 플로가 새로운 값을 방출하지 않으면
        // 가장 최근에 방출한 이전 값을 사용한다
        val flow1 = (1..5).asFlow().onEach { delay(1000) }
        val flow2 = flowOf("one", "two", "three", "four").onEach { delay(1500) }
        flow1.zip(flow2) { value, string -> "$value, $string" }.collect{ count = it }
    }

    // collectAsState() 함수를 사용한 방법
    //val count by flow.collectAsState(initial = 0)

    // collectLatest() 연산자는 이전 값에 대한 처리가 완료되기 전에 도착하는 새로운 값들을 현재 컬렉션에서 취소하고
    // 가장 최근 값에 대한 처리를 재시작 한다.
    // conflate() 연산자는 collectLatest와 비슷하지만 새로운 값이 도착했을 때 현재 컬렉션에 대한 동작을 취소한다.
    // single() 연산자는 플로에서 단일한 값을 수집하며 스트림에서 다른 값을 발견하면 예외를 던진다.

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$count, $count2, $count3", style = TextStyle(fontSize = 30.sp))
        Button(onClick = {
            viewModel.increaseValue()
            viewModel.startSharedFlow()
        }){
            Text("Click Me")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FlowDemoTheme {
        ScreenSetup(DemoViewModel())
    }
}