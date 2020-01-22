package com.harrysoft.androidbluetoothserial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.ArrayMap;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Single;

/**
 * Implementation of BluetoothManager, package-private
 */
class BluetoothManagerImpl implements BluetoothManager {

    private final BluetoothAdapter adapter;

    private final Map<String, BluetoothSerialDeviceImpl> devices = new ArrayMap<>();

    /**
     * Package private constructor
     */
    BluetoothManagerImpl(BluetoothAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    @NotNull
    public List<BluetoothDevice> getPairedDevicesList() {
        return new ArrayList<>(adapter.getBondedDevices());
    }

    @Override
    @NotNull
    public Single<BluetoothSerialDevice> openSerialDevice(@NotNull String mac) {
        return openSerialDevice(mac, StandardCharsets.UTF_8);
    }

    @Override
    @NotNull
    public Single<BluetoothSerialDevice> openSerialDevice(@NotNull String mac, @NotNull Charset charset) {
        if (devices.containsKey(mac)) {
            return Single.just(devices.get(mac));
        } else {
            return Single.fromCallable(() -> {
                try {
                    BluetoothDevice device = adapter.getRemoteDevice(mac);
                    BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                    adapter.cancelDiscovery();
                    socket.connect();
                    BluetoothSerialDeviceImpl serialDevice = new BluetoothSerialDeviceImpl(mac, socket, charset);
                    devices.put(mac, serialDevice);
                    return serialDevice;
                } catch (Exception e) {
                    throw new BluetoothConnectException(e);
                }
            });
        }
    }

    @Override
    public void closeDevice(@NotNull String mac) {
        BluetoothSerialDeviceImpl removedDevice = devices.remove(mac);
        if (removedDevice != null) {
            try {
                removedDevice.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closeDevice(@NotNull BluetoothSerialDevice device) {
        closeDevice(device.getMac());
    }

    @Override
    public void closeDevice(@NotNull SimpleBluetoothDeviceInterface deviceInterface) {
        closeDevice(deviceInterface.getDevice().getMac());
    }

    @Override
    public void close() {
        for (Map.Entry<String, BluetoothSerialDeviceImpl> deviceEntry :  devices.entrySet()) {
            try {
                deviceEntry.getValue().close();
            } catch (Throwable ignored) {}
        }
        devices.clear();
    }
}
