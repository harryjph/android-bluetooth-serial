package com.harrysoft.androidbluetoothserial;

import android.bluetooth.BluetoothSocket;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.annotations.Nullable;

public class BluetoothSerialDevice {
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final String mac;
    private final BluetoothSocket socket;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final Charset charset;

    @Nullable
    private SimpleBluetoothDeviceInterface owner;

    private BluetoothSerialDevice(String mac, BluetoothSocket socket, OutputStream outputStream, InputStream inputStream, Charset charset) {
        this.mac = mac;
        this.socket = socket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.charset = charset;
    }

    static BluetoothSerialDevice getInstance(String mac, BluetoothSocket socket, Charset charset) throws IOException {
        return new BluetoothSerialDevice(mac, socket, socket.getOutputStream(), socket.getInputStream(), charset);
    }

    /**
     * @param message The message to send to the device
     * @return An RxJava Completable to asynchronously
     *          send the message.
     */
    public Completable send(String message) {
        requireNotClosed();
        return Completable.fromAction(() -> {
            synchronized (outputStream) {
                if (!closed.get()) outputStream.write(message.getBytes(charset));
            }
        });
    }

    /**
     * @return An RxJava Flowable that, when observed, will
     *          provide a stream of messages from the device.
     *          A message is considered to be terminated by a
     *          newline ('\n') character. If a newline is not
     *          received, the message will continue buffering
     *          forever. If this is not the desired behaviour,
     *          please manually manage the input using getInputStream()
     */
    public Flowable<String> openMessageStream() {
        requireNotClosed();
        return Flowable.create(emitter -> {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, charset));
            while (!emitter.isCancelled() && !closed.get()) {
                synchronized (inputStream) {
                    try {
                        String receivedString = in.readLine();
                        if (!TextUtils.isEmpty(receivedString)) {
                            emitter.onNext(receivedString);
                        }
                    } catch (Exception e) {
                        if (!emitter.isCancelled() && !closed.get()) {
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
        if (!closed.get()) {
            closed.set(true);
            synchronized (inputStream) {
                inputStream.close();
            }
            synchronized (outputStream) {
                outputStream.close();
            }
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
        if (closed.get()) {
            throw new IllegalArgumentException("Device connection closed");
        }
    }

    /**
     * @return The MAC address of the closed bluetooth device
     */
    public String getMac() {
        return mac;
    }

    /**
     * @return The underlying InputStream representing the device's input.
     *          This can be used to manually manage the device's input,
     *          and is most useful in cases where the device does not send
     *          a newline character at the end of each message.
     */
    public InputStream getInputStream() {
        return inputStream;
    }
}
