package com.example.compose_usbserial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldSubcomposeInMeasureFix
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose_usbserial.ui.theme.Compose_USBSerialTheme
import com.example.id_location_robot.SerialManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sampleViewModel = SampleViewModel()
        //you need to register blockHandler to process data.
        SerialManager.initialize(this){blockString -> sampleViewModel.blockHandler(blockString)}

        setContent {
            Compose_USBSerialTheme {
                SampleScreen(sampleViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SerialManager.release()
    }
}

@Composable
fun SampleScreen(viewModel: SampleViewModel) {
    val serialIsConnected = SerialManager.serialIsConnected.collectAsState().value
    val serialString = SerialManager.blockString.collectAsState().value
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(Modifier.height(50.dp))
        Button(onClick = {
            if (!serialIsConnected) {
                SerialManager.connectSerialDevice()
            } else {
                SerialManager.disconnectSerialDevice()
            }
        }) {
            Text(if(serialIsConnected)"Disconnect" else "Connect")
        }
        Text("nowSerial: $serialString")
        Spacer(Modifier.height(20.dp))
        Text("--Cumulated(processed) Serial--")
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
                .background(Color.LightGray)) {
            item {
                Text(viewModel.serialString.value)
            }
        }
    }
}
