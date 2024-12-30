# SerialManager for Jetpack Compose

`SerialManager` is a library built on top of [usb-serial-for-android](https://github.com/mik3y/usb-serial-for-android), designed to simplify the use of USB serial devices in Jetpack Compose applications.
This library leverages Android USB Host Mode (OTG) to communicate with USB serial devices and makes managing USB connections and data processing straightforward within the Jetpack Compose environment.

## Features

- Easy connection and data handling for USB serial devices
- Seamless integration with Jetpack Compose
- Optimized for Compose while retaining all core functionalities of `usb-serial-for-android`
- only support CdcACM device now.(I will update soon)

## Installation

To use `SerialManager`, you can either:
1. **Use this project as a base for your application**  
   *Use the template* or Clone/fork this repository and build your application on top of it.

2. **Add `SerialManager` to your existing project**  
   Follow these steps to integrate:

### Gradle Setup
Add the following dependencies to your Gradle build file:

```build.gradle.kts
dependencies {
    implementation ("com.github.mik3y:usb-serial-for-android:3.8.0") // add this. 
    //(Dependency for usb-serial-for-android)
```

```settings.gradle.kts
dependencyResolutionManagement {
    ...
    repositories {
        ...
        maven(url = "https://jitpack.io") // add this.
    }
}
```

### Copy Files
Copy the following files from this project into your own:
- `SerialManager.kt`
- `SerialListener.kt`
---

## Usage

### 1. Initialize and Release `SerialManager`
#### Initialization
Call `SerialManager.initialize(context, blockHandler)` in your `MainActivity.onCreate()` (or wherever you start the serial connection):
- **`context`**: Provide the application (or activity) context.
- **`blockHandler`**: Define a callback function to handle incoming block data from the serial device.

#### Release
Call `SerialManager.release()` in your `MainActivity.onDestroy()` (or wherever you stop the serial connection).

### 2. Connect and Disconnect
- **Connect**: Use `SerialManager.connectSerialDevice()` to establish a connection with a USB serial device.
- **Disconnect**: Use `SerialManager.disconnectSerialDevice()` to terminate the connection.

Once connected, the `blockHandler` you registered will be triggered for every new block of data received.

---

## Dependencies

This library is built on top of `usb-serial-for-android`.  
All features and limitations of the original library apply. For more details, refer to the original library:  
[usb-serial-for-android GitHub page](https://github.com/mik3y/usb-serial-for-android)

---

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).  
The license for `usb-serial-for-android` can be found in its [MIT License](https://github.com/mik3y/usb-serial-for-android/blob/master/LICENSE).

