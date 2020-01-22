package com.harrysoft.androidbluetoothserial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.List;

import io.reactivex.Single;

public interface BluetoothManager extends AutoCloseable {
    /**
     * @return A BluetoothManager instance if the device
     *          has Bluetooth or null otherwise.
     */
    @Nullable
    static BluetoothManager getInstance() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            return new BluetoothManagerImpl(bluetoothAdapter);
        }
        return null;
    }

    /**
     * @return A list of paired Bluetooth devices
     */
    @NotNull
    List<BluetoothDevice> getPairedDevicesList();

    /**
     * @param mac The MAC address of the device
     *             you are trying to connect to
     * @return An RxJava Single, that will either emit
     *          a BluetoothSerialDevice or a BluetoothConnectException
     */
    @NotNull
    Single<BluetoothSerialDevice> openSerialDevice(@NotNull String mac);

    /**
     * @param mac The MAC address of the device
     *             you are trying to connect to
     * @param charset The Charset to use for input/output streams
     * @return An RxJava Single, that will either emit
     *          a BluetoothSerialDevice or a BluetoothConnectException
     */
    @NotNull
    Single<BluetoothSerialDevice> openSerialDevice(@NotNull String mac, @NotNull Charset charset);

    /**
     * Closes the connection to a device. After calling,
     * you should probably set your instance to null
     * to avoid trying to read/write from it.
     *
     * @param mac The MAC Address of the device you are
     *            trying to close the connection to
     */
    void closeDevice(@NotNull String mac);

    /**
     * Closes the connection to a device. After calling,
     * you should probably set your instance to null
     * to avoid trying to read/write from it.
     *
     * @param device The instance of the device you are
     *               trying to close the connection to
     */
    void closeDevice(@NotNull BluetoothSerialDevice device);

    /**
     * Closes the connection to a device. After calling,
     * you should probably set your instance to null
     * to avoid trying to read/write from it.
     *
     * @param deviceInterface The interface accessing the device
     *                        you are trying to close the connection to
     */
    void closeDevice(@NotNull SimpleBluetoothDeviceInterface deviceInterface);

    /**
     * Closes all connected devices
     */
    @Override
    void close();
}
