package com.harrysoft.androidbluetoothserial;

import android.bluetooth.BluetoothSocket;
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

    private boolean connected = true;

    private final String mac;
    private final BluetoothSocket socket;
    private final OutputStream outputStream;
    private final InputStream inputStream;

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
        return Completable.fromAction(() -> {if (connected) outputStream.write(message.getBytes());});
    }

    /**
     * @return An RxJava Flowable that, when observed, will
     *          provide a stream of messages from the device.
     */
    public Flowable<String> openMessageStream() {
        return Flowable.create(emitter -> {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            while (!emitter.isCancelled() && connected) {
                try {
                    String receivedString = in.readLine();
                    if (!TextUtils.isEmpty(receivedString)) {
                        emitter.onNext(receivedString);
                    }
                } catch (Exception e) {
                    if (!emitter.isCancelled() && connected) {
                        emitter.onError(e);
                    } else {
                        break;
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
        if (connected) {
            connected = false;
            inputStream.close();
            outputStream.close();
            socket.close();
        }
    }

    /**
     * @return The MAC address of the connected bluetooth device
     */
    public String getMac() {
        return mac;
    }
}
