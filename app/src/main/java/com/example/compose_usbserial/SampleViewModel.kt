package com.example.compose_usbserial

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SampleViewModel(): ViewModel() {
    val serialString = mutableStateOf("")

    fun blockHandler(blockString: String){
        //You can Process block String in here.
        serialString.value += blockString + "\n"
    }
}