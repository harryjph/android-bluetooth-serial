package com.harrysoft.androidbluetoothserial

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import io.reactivex.Single
import java.nio.charset.Charset

interface BluetoothManager : AutoCloseable {
    /**
     * A collection of paired Bluetooth devices, not restricted to serial devices.
     */
    val pairedDevices: Collection<BluetoothDevice>

    /**
     * A collection of paired Bluetooth devices, not restricted to serial devices.
     */
    @Deprecated("Use pairedDevices instead", replaceWith = ReplaceWith("pairedDevices"))
    val pairedDevicesList: List<BluetoothDevice> get() = pairedDevices.toList()

    /**
     * @param mac The MAC address of the device
     * you are trying to connect to
     * @return An RxJava Single, that will either emit
     * a BluetoothSerialDevice or a BluetoothConnectException
     */
    fun openSerialDevice(mac: String): Single<BluetoothSerialDevice>

    /**
     * @param mac The MAC address of the device
     * you are trying to connect to
     * @param charset The Charset to use for input/output streams
     * @return An RxJava Single, that will either emit
     * a BluetoothSerialDevice or a BluetoothConnectException
     */
    fun openSerialDevice(mac: String, charset: Charset): Single<BluetoothSerialDevice>

    /**
     * Closes the connection to a device. After calling,
     * you should probably set your instance to null
     * to avoid trying to read/write from it.
     *
     * @param mac The MAC Address of the device you are
     * trying to close the connection to
     */
    fun closeDevice(mac: String)

    /**
     * Closes the connection to a device. After calling,
     * you should probably set your instance to null
     * to avoid trying to read/write from it.
     *
     * @param device The instance of the device you are
     * trying to close the connection to
     */
    fun closeDevice(device: BluetoothSerialDevice)

    /**
     * Closes the connection to a device. After calling,
     * you should probably set your instance to null
     * to avoid trying to read/write from it.
     *
     * @param deviceInterface The interface accessing the device
     * you are trying to close the connection to
     */
    fun closeDevice(deviceInterface: SimpleBluetoothDeviceInterface)

    /**
     * Closes all connected devices
     */
    override fun close()

    companion object {
        /**
         * @return A BluetoothManager instance if the device
         * has Bluetooth or null otherwise.
         */
        @JvmStatic
        val instance: BluetoothManager? by lazy {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter != null) {
                BluetoothManagerImpl(bluetoothAdapter)
            } else null
        }
    }
}
