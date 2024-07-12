package com.example.myserialapp

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hoho.android.usbserial.BuildConfig
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Arrays


class SerialViewModel(application: Application): AndroidViewModel(application), SerialInputOutputManager.Listener {
    var connected : Boolean = false
    var connectedUSBItem = MutableStateFlow<USBItem?>(null)

    private val INTENT_ACTION_GRANT_USB: String = BuildConfig.LIBRARY_PACKAGE_NAME + ".GRANT_USB"
    private var usbIOManager: SerialInputOutputManager? = null //= SerialInputOutputManager(connectedUSBItem.value?.port, this)

    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (INTENT_ACTION_GRANT_USB == intent.action) {
                usbPermission = if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    USBPermission.Granted
                } else {
                    USBPermission.Denied
                }
                connectSerialDevice(context)
            }
        }
    }

    init {
        val filter = IntentFilter(INTENT_ACTION_GRANT_USB)
        getApplication<Application>().registerReceiver(usbPermissionReceiver, filter)
    }

    fun connectSerialDevice(context: Context){
        viewModelScope.launch(Dispatchers.IO) {

            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            Log.d("asdf", "${connectedUSBItem.value == null}")
            while (connectedUSBItem.value == null) {
                Log.d("button", "try to Connect")
                for (device in usbManager.deviceList.values) {
                    val driver = CdcAcmSerialDriver(device)
                    if (driver.ports.size == 1) {
                        connectedUSBItem.update {
                            USBItem(device, driver.ports[0], driver)
                        }
                        Log.d("asdf", "device Connected")
                    }
                }
                delay(1000L) //by 1 sec
            }
            val device: UsbDevice = connectedUSBItem.value!!.device
            val port: UsbSerialPort = connectedUSBItem.value!!.port
            val driver: UsbSerialDriver = connectedUSBItem.value!!.driver

            Log.d("asdf", "usb connection try")
            var usbConnection: UsbDeviceConnection? = null
            if (usbPermission == USBPermission.UnKnown && !usbManager.hasPermission(device)) {
                usbPermission = USBPermission.Requested
                val intent: Intent = Intent(INTENT_ACTION_GRANT_USB)
                intent.setPackage(getApplication<Application>().packageName)
                Log.d("asdf", "request Permission")
                usbManager.requestPermission(
                    device,
                    PendingIntent.getBroadcast(
                        getApplication(),
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
                return@launch
            }
            delay(1000L)
            usbConnection = usbManager.openDevice(device)
            Log.d("asdf", "Port open try")
            connectedUSBItem.value!!.port.open(usbConnection)
            Log.d("asdf", "Port open")
            connectedUSBItem.value!!.port.setParameters(baudRate, 8, 1, UsbSerialPort.PARITY_NONE)
            if(usbIOManager == null) {
                usbIOManager =
                    SerialInputOutputManager(connectedUSBItem.value!!.port, this@SerialViewModel)
                usbIOManager!!.start()
                connected = true
                connectedUSBItem.value?.port?.dtr = true
            }
        }


    }
    fun disConnectSerialDevice(){
//        connectedUSBItem.value?.port?.dtr = false
        connected = false
        usbIOManager!!.listener = null
        usbIOManager!!.stop()
        connectedUSBItem.value?.port?.close()
        connectedUSBItem.update{ null }
    }

    private var _buffer = mutableStateOf("")
    var lineTexts by mutableStateOf("")
        private set

    fun appendLineText(newText: String){
        lineTexts = lineTexts + newText + "\n"
    }

    private enum class USBPermission {UnKnown, Requested, Granted, Denied}
    //private val INTENT_ACTION_GRANT_USB: String = BuildConfig.APPLICATION_ID + ".GRANT_USB"
    private val WRITE_WAIT_MILLIS: Int = 2000
    private val READ_WAIT_MILLIS: Int = 2000
    var baudRate = 115200

    private var usbPermission: USBPermission = USBPermission.UnKnown

    override fun onNewData(data: ByteArray?) {
        viewModelScope.launch{
            receive(data)
        }
    }

    override fun onRunError(e: Exception) {
        viewModelScope.launch() {
            status("connection lost: ${e.message}")
            disConnectSerialDevice()
        }
    }


    private fun send(str: String){

    }
    private fun read(){
        if(!connected){
            Log.d("fuck", "not connected")
        }
        try {
            val buffer = ByteArray(8192)
            val len: Int = connectedUSBItem.value?.port?.read(buffer, READ_WAIT_MILLIS)
                ?: 0
            receive(buffer.copyOf(len))
        }catch(e: IOException){
            status("connection lost: " + e.message)
            disConnectSerialDevice()
        }
    }
    private fun receive(data: ByteArray?){
        Log.d("good", "receive data")
        if(data != null) {
            if (data.isNotEmpty()) {
                Log.d("condition", "buffer.value.isNotEmpty():${_buffer.value.isNotEmpty()}")
                val result : String = getLineString(data)
                Log.d("fuck","result")
                if (_buffer.value.isEmpty()) {
                    _buffer.value += result
                }else{
                    if(result.substring(0,3) == "{\"B"){ //메시지를 받다말고 새로운 메시지가 들어옴
                        appendLineText("Error")
                        _buffer.value = result
                    }else if(result.substring(result.length - 3).equals("}..")){ //메시지의 끝
                        _buffer.value += result
                        appendLineText(_buffer.value)
                        _buffer.value = ""
                    }else{
                        _buffer.value += result
                    }
                }
            }
        }
    }
    private fun status(str: String){
        appendLineText(str)
    }


}

fun getLineString(array: ByteArray): String {
    return getLineString(array, 0, array.size)
}

fun getLineString(array: ByteArray, offset: Int, length: Int): String {
    val result = StringBuilder()

    val line = ByteArray(8)
    var lineIndex = 0
    var resultString = ""
    for (i in offset until offset + length) {
        if (lineIndex == line.size) {
            for (j in line.indices) {
                if (line[j] > ' '.code.toByte() && line[j] < '~'.code.toByte()) {
                    result.append(String(line, j, 1))
                } else {
                    result.append(".")
                }
            }
            lineIndex = 0
        }
        val b = array[i]
        line[lineIndex++] = b
    }
    for (i in 0 until lineIndex) {
        if (line[i] > ' '.code.toByte() && line[i] < '~'.code.toByte()) {
            result.append(String(line, i, 1))
        } else {
            result.append(".")
        }
    }
    resultString = result.toString()

    return resultString
}