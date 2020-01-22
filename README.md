# android-bluetooth-serial

[![Build Status](https://travis-ci.com/harry1453/android-bluetooth-serial.svg?branch=master)](https://travis-ci.com/harry1453/android-bluetooth-serial)

A library for Android to simplify basic serial communication over Bluetooth, for example when communicating with Arduinos.

## How to include the library

[![JitPack](https://jitpack.io/v/harry1453/android-bluetooth-serial.svg)](https://jitpack.io/#harry1453/android-bluetooth-serial)

**Gradle**

- **Project level `build.gradle`**
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
- **App level `build.gradle`**
```gradle
dependencies {
    implementation 'com.github.harry1453:android-bluetooth-serial:v1.1'
    
    // RxJava is also required.
    implementation 'io.reactivex.rxjava2:rxjava:2.1.12'
    implementation 'io.reactivex.rxjava2:rxandroid:2.0.2'
}
```

## Using the library

Please see the `demoApplication` directory for a fully-featured demo app.

1. Declare your `BluetoothManager` (Make sure to include the library `BluetoothManager`, not the Android one):

```JAVA
import com.harrysoft.androidbluetoothserial.BluetoothManager;
```

Your Activity's `onCreate()`:

```JAVA
// Setup our BluetoothManager
BluetoothManager bluetoothManager = BluetoothManager.getInstance();
if (bluetoothManager == null) {
    // Bluetooth unavailable on this device :( tell the user
    Toast.makeText(context, "Bluetooth not available.", Toast.LENGTH_LONG).show(); // Replace context with your context instance.
    finish();
}
```

2. Get the list of paired devices:

```JAVA
Collection<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevices();
for (BluetoothDevice device : pairedDevices) {
    Log.d("My Bluetooth App", "Device name: " + device.getName());
    Log.d("My Bluetooth App", "Device MAC Address: " + device.getAddress());
}
```

3. Select a device you want to connect to from the list and fetch its MAC Address.

4. Connect to the device and send/receive messages:

```JAVA
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
```

```JAVA
private SimpleBluetoothDeviceInterface deviceInterface;

private void connectDevice(String mac) {
    bluetoothManager.openSerialDevice(mac)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onConnected, this::onError);
}

private void onConnected(BluetoothSerialDevice connectedDevice) {
    // You are now connected to this device!
    // Here you may want to retain an instance to your device:
    deviceInterface = connectedDevice.toSimpleDeviceInterface();
    
    // Listen to bluetooth events
    deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError);
    
    // Let's send a message:
    deviceInterface.sendMessage("Hello world!");
}

private void onMessageSent(String message) {
    // We sent a message! Handle it here.
    Toast.makeText(context, "Sent a message! Message was: " + message, Toast.LENGTH_LONG).show(); // Replace context with your context instance.
}

private void onMessageReceived(String message) {
    // We received a message! Handle it here.
    Toast.makeText(context, "Received a message! Message was: " + message, Toast.LENGTH_LONG).show(); // Replace context with your context instance.
}

private void onError(Throwable error) {
    // Handle the error
}
```

5. Disconnect the device:
```JAVA
// Please remember to destroy your instance after closing as it will no longer function!

// Disconnect one device
bluetoothManager.closeDevice(macAddress); // Close by mac
// OR
bluetoothManager.closeDevice(connectedDevice); // Close by device instance
// OR
bluetoothManager.closeDevice(deviceInterface); // Close by interface instance

// Disconnect all devices
bluetoothManager.close();
```
