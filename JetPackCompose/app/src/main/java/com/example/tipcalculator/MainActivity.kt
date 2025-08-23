package com.example.tipcalculator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tipcalculator.components.InputField
import com.example.tipcalculator.ui.theme.TipCalculatorTheme
import com.example.tipcalculator.utils.calculateTotalPerPerson
import com.example.tipcalculator.utils.calculateTotalTip
import com.example.tipcalculator.widgets.RoundIconButton

val TAG = "tanmay"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MyApp {
                MainContent()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    TipCalculatorTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.systemBars
            )
        ) {
            content()
        }

    }
}

//@Preview()
@Composable
fun TopHeader(totalPerPerson: Double = 0.0) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(10.dp)
            .clip(shape = CircleShape.copy(all = CornerSize(12.dp))),
        color = Color.Gray
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            val total = "%.2f".format(totalPerPerson)
            Text(
                text = "Total Per Person",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "$$total",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Preview
@Composable
fun MainContent() {
    val totalPerson = remember {
        mutableStateOf(1)
    }
    val tipAmountState = remember {
        mutableStateOf(0.0)
    }

    Column(modifier = Modifier.padding(12.dp)) {
        val totalBillState = remember {
            mutableStateOf(0.0)
        }
        TopHeader(totalBillState.value)
        BillForm(totalPerPerson = totalBillState, totalPerson = totalPerson, tipAmountState = tipAmountState)
    }

}

@Composable
fun BillForm(
    modifier: Modifier = Modifier,
    totalPerPerson: MutableState<Double>,
    range: IntRange = 1..100,
    totalPerson: MutableState<Int>,
    tipAmountState: MutableState<Double>,
    onValChange: (String) -> Unit = {}
) {
    val totalBillState = remember {
        mutableStateOf("")
    }
    val validState = remember(totalBillState.value) {
        totalBillState.value.trim().isNotEmpty()
    }
    val keyboardController = LocalFocusManager.current


    val sliderPositionState = remember {
        mutableStateOf(0f)
    }


    val tipPercate = (sliderPositionState.value * 100).toInt()
//    TopHeader()
    Surface(
        modifier = modifier
            .padding(2.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(width = 1.dp, color = Color.LightGray)
    ) {
        Column(modifier = modifier.padding(6.dp)) {
            InputField(
                valueState = totalBillState,
                labelId = "Enter Bill",
                enabled = true,
                isSingleLine = true,
                onAction = KeyboardActions {
                    if (!validState) return@KeyboardActions
                    onValChange(totalBillState.value.trim())
                    keyboardController.clearFocus()
                })
            if (validState) {
                Row(
                    modifier = modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        "Split",
                        modifier = Modifier.align(alignment = Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(120.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 3.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        RoundIconButton(imageVector = Icons.Default.Remove, onClick = {
                            if (totalPerson.value == 1) return@RoundIconButton
                            totalPerson.value -= 1
                            totalPerPerson.value =
                                calculateTotalPerPerson(totalBillState.value.toDouble(), totalPerson.value, tipPercate)

                        })
                        Text(
                            text = "${totalPerson.value}", modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 9.dp, end = 9.dp),
                            fontSize = 20.sp
                        )
                        RoundIconButton(imageVector = Icons.Default.Add, onClick = {
                            totalPerson.value += 1
                            totalPerPerson.value =
                                calculateTotalPerPerson(totalBillState.value.toDouble(), totalPerson.value, tipPercate)
                        })
                    }
                }
                Row(modifier = Modifier.padding(horizontal = 3.dp, vertical = 4.dp)) {
                    Text("Tip", modifier = Modifier.align(alignment = Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(200.dp))
                    Text(
                        "$ ${tipAmountState.value}",
                        modifier = Modifier.align(alignment = Alignment.CenterVertically)
                    )
                }
                Column(
                    modifier = Modifier.padding(5.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("$tipPercate %")
                    Spacer(modifier = Modifier.height(10.dp))
                    Slider(
                        value = sliderPositionState.value, onValueChange = { newVal ->
                            sliderPositionState.value = newVal
                            tipAmountState.value =
                                calculateTotalTip(
                                    totalBillState.value.toDouble(),
                                    (sliderPositionState.value * 100).toInt()
                                )
                            totalPerPerson.value =
                                calculateTotalPerPerson(totalBillState.value.toDouble(), totalPerson.value, tipPercate)
                        },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )
                }
            }
        }
    }

}

