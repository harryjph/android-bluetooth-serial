package com.harrysoft.androidbluetoothserial;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;

public class BluetoothSerialDevice {

    private boolean closed = false;

    private final String mac;
    private final BluetoothSocket socket;
    private final OutputStream outputStream;
    private final InputStream inputStream;

    @Nullable
    private SimpleBluetoothDeviceInterface owner;

    private BluetoothSerialDevice(String mac, BluetoothSocket socket, OutputStream outputStream, InputStream inputStream) {
        this.mac = mac;
        this.socket = socket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    static BluetoothSerialDevice getInstance(String mac, BluetoothSocket socket) throws IOException {
        return new BluetoothSerialDevice(mac, socket, socket.getOutputStream(), socket.getInputStream());
    }

    /**
     * @param message The message to send to the device
     * @return An RxJava Completable to asynchronously
     *          send the message.
     */
    public Completable send(String message) {
        requireNotClosed();
        return Completable.fromAction(() -> { if (!closed) outputStream.write(message.getBytes()); });
    }

    /**
     * @return An RxJava Flowable that, when observed, will
     *          provide a stream of messages from the device.
     */
    public Flowable<String> openMessageStream() {
        requireNotClosed();
        return Flowable.create(emitter -> {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            while (!emitter.isCancelled() && !closed) {
                synchronized (this) {
                    try {
                        String receivedString = in.readLine();
                        if (!TextUtils.isEmpty(receivedString)) {
                            emitter.onNext(receivedString);
                        }
                    } catch (Exception e) {
                        if (!emitter.isCancelled() && !closed) {
                            emitter.onError(e);
                        } else {
                            break;
                        }
                    }
                }
            }
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * @throws IOException if one of the streams
     *          throws an exception whilst closing
     */
    void close() throws IOException {
        if (!closed) {
            closed = true;
            inputStream.close();
            outputStream.close();
            socket.close();
        }
        if (owner != null) {
            owner.close();
            owner = null;
        }
    }

    /**
     * Wrap using a SimpleBluetoothDeviceInterface.
     * This makes things a lot simpler within the class accessing this device
     *
     * @return a SimpleBluetoothDeviceInterface that will access this device object
     */
    public SimpleBluetoothDeviceInterface toSimpleDeviceInterface() {
        requireNotClosed();
        if (owner != null) {
            return owner;
        } else {
            return owner = new SimpleBluetoothDeviceInterface(this);
        }
    }

    /**
     * Internal function that checks that
     * this instance has not been closed
     */
    void requireNotClosed() {
        if (closed) {
            throw new IllegalArgumentException("Device connection closed");
        }
    }

    /**
     * @return The MAC address of the closed bluetooth device
     */
    public String getMac() {
        return mac;
    }
}
