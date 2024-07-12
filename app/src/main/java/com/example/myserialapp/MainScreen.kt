package com.example.myserialapp

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myserialapp.ui.theme.MySerialAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: SerialViewModel, innerPadding:PaddingValues){
    var dialog by rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.TopCenter
    ){
        Column {
            ConnectingButton(viewModel = viewModel)
            TextButton(
                onClick = {viewModel.appendLineText("asdfasdf")}
            ){
                Text(text = "asdf")
            }
            SerialLog(viewModel)



        }
    }
}
@Composable
fun ConnectingButton(viewModel: SerialViewModel){
    val connectedUSBItem = viewModel.connectedUSBItem.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    if (connectedUSBItem == null) {
        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.connectSerialDevice(context)
                }
            },
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Connect to Serial Device")
        }
    }
    else {
        Row {
            Card(
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth()
            ) {
                androidx.compose.material3.Text(
                    text = "Port:${connectedUSBItem.port.portNumber}\n" +
                            "Name:${connectedUSBItem.device.deviceName}\n" +
                            "${connectedUSBItem.device.deviceId}\n "
                )
            }
            TextButton(
                onClick = {
                    coroutineScope.launch{
                        viewModel.disConnectSerialDevice()
                    }
                },
                colors = ButtonDefaults.textButtonColors(backgroundColor = Color.Magenta),
                modifier = Modifier.width(80.dp)
            ){
                Text("Disconnect")
            }
        }
    }

}

@Composable
fun SerialLog(viewModel:SerialViewModel){
    LazyColumn() {
        item {
            Text(text = viewModel.lineTexts)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MySerialAppTheme {
        MainScreen(SerialViewModel(Application()),PaddingValues(0.dp))
    }
}