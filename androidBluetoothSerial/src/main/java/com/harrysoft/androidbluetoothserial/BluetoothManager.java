package com.harrysoft.androidbluetoothserial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.ArrayMap;

import java.io.Closeable;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Single;

@SuppressWarnings("WeakerAccess")
public class BluetoothManager implements Closeable {

    private final BluetoothAdapter adapter;

    private final ArrayMap<String, BluetoothSerialDevice> devices = new ArrayMap<>();

    private BluetoothManager(BluetoothAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * @return A BluetoothManager instance if the device
     *          has bluetooth or null otherwise.
     */
    public static BluetoothManager getInstance() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            return new BluetoothManager(bluetoothAdapter);
        }
        return null;
    }

    /**
     * @return A list of paired Bluetooth devices
     */
    public List<BluetoothDevice> getPairedDevicesList() {
        return new ArrayList<>(adapter.getBondedDevices());
    }

    /**
     * @param mac The MAC address of the device
     *             you are trying to connect to
     * @return An RxJava Single, that will either emit
     *          a BluetoothSerialDevice or a BluetoothConnectException
     */
    public Single<BluetoothSerialDevice> openSerialDevice(String mac) {
        if (devices.containsKey(mac)) {
            return Single.just(devices.get(mac));
        } else {
            return Single.fromCallable(() -> {
                try {
                    BluetoothDevice device = adapter.getRemoteDevice(mac);
                    BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                    adapter.cancelDiscovery();
                    socket.connect();
                    BluetoothSerialDevice serialDevice = BluetoothSerialDevice.getInstance(mac, socket);
                    devices.put(mac, serialDevice);
                    return serialDevice;
                } catch (Exception e) {
                    throw new BluetoothConnectException(e);
                }
            });
        }
    }

    /**
     * Closes the connection to a device. After calling,
     * you should probably set your instance to null
     * to avoid trying to read/write from it.
     *
     * @param mac The MAC Address of the device you are
     *            trying to close the connection to
     */
    public void closeDevice(String mac) {
        BluetoothSerialDevice removedDevice = devices.remove(mac);
        if (removedDevice != null) {
            try {
                removedDevice.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes the connection to a device. After calling,
     * you should probably set your instance to null
     * to avoid trying to read/write from it.
     *
     * @param device The instance of the device you are
     *               trying to close the connection to
     */
    public void closeDevice(BluetoothSerialDevice device) {
        closeDevice(device.getMac());
    }

    /**
     * Closes the connection to a device. After calling,
     * you should probably set your instance to null
     * to avoid trying to read/write from it.
     *
     * @param deviceInterface The interface accessing the device
     *                        you are trying to close the connection to
     */
    public void closeDevice(SimpleBluetoothDeviceInterface deviceInterface) {
        closeDevice(deviceInterface.getDevice().getMac());
    }


    /**
     * Close all connected devices
     */
    @Override
    public void close() {
        for (Iterator<Map.Entry<String, BluetoothSerialDevice>> iterator = devices.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, BluetoothSerialDevice> deviceEntry = iterator.next();
            try {
                deviceEntry.getValue().close();
            } catch (Throwable ignored) {}
            iterator.remove();
        }
    }
}
