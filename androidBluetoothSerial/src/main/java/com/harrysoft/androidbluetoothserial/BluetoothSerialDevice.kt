package com.harrysoft.androidbluetoothserial

import io.reactivex.Completable
import io.reactivex.Flowable
import java.io.InputStream
import java.io.OutputStream

interface BluetoothSerialDevice {
    /**
     * @return The MAC address of the closed bluetooth device
     */
    val mac: String

    /**
     * @return The underlying InputStream representing the device's output.
     * This can be used to manually manage the device's output.
     * This is useful in cases where the device does not send
     * a newline character at the end of each message.
     * **Should not be used in conjunction with [openMessageStream]**
     */
    val inputStream: InputStream

    /**
     * @return The underlying OutputStream representing the device's input.
     * This can be used to manually manage the device's input.
     * **Should not be used in conjunction with [send]**
     */
    val outputStream: OutputStream

    /**
     * @param message The message to send to the device
     * @return An RxJava Completable to asynchronously
     * send the message.
     */
    fun send(message: String): Completable

    /**
     * @return An RxJava Flowable that, when observed, will
     * provide a stream of messages from the device.
     * A message is considered to be terminated by a
     * newline ('\n') character. If a newline is not
     * received, the message will continue buffering
     * forever. If this is not the desired behaviour,
     * please manually manage the input using getInputStream()
     */
    fun openMessageStream(): Flowable<String>

    /**
     * Wrap using a SimpleBluetoothDeviceInterface.
     * This makes things a lot simpler within the class accessing this device
     *
     * @return a SimpleBluetoothDeviceInterface that will access this device object
     */
    fun toSimpleDeviceInterface(): SimpleBluetoothDeviceInterface
}
