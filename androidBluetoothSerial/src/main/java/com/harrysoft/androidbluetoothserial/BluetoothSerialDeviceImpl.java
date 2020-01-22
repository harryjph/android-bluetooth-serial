package com.harrysoft.androidbluetoothserial;

import android.bluetooth.BluetoothSocket;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

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

/**
 * Implementation of BluetoothSerialDevice, package-private
 */
class BluetoothSerialDeviceImpl implements BluetoothSerialDevice {
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final String mac;
    private final BluetoothSocket socket;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final Charset charset;

    @Nullable
    private SimpleBluetoothDeviceInterfaceImpl owner;

    /**
     * Package private constructor
     */
    BluetoothSerialDeviceImpl(String mac, BluetoothSocket socket, Charset charset) throws IOException {
        this.mac = mac;
        this.socket = socket;
        this.outputStream = socket.getOutputStream();
        this.inputStream = socket.getInputStream();
        this.charset = charset;
    }

    @Override
    @NotNull
    public Completable send(String message) {
        checkNotClosed();
        return Completable.fromAction(() -> {
            synchronized (outputStream) {
                if (!closed.get()) outputStream.write(message.getBytes(charset));
            }
        });
    }

    @Override
    @NotNull
    public Flowable<String> openMessageStream() {
        checkNotClosed();
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
     * Package-private.
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

    @Override
    @NotNull
    public SimpleBluetoothDeviceInterfaceImpl toSimpleDeviceInterface() {
        checkNotClosed();
        if (owner != null) {
            return owner;
        } else {
            return owner = new SimpleBluetoothDeviceInterfaceImpl(this);
        }
    }

    /**
     * Package private function that checks that
     * this instance has not been closed
     */
    void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("Device connection closed");
        }
    }

    @Override
    @NotNull
    public String getMac() {
        return mac;
    }

    @Override
    @NotNull
    public InputStream getInputStream() {
        return inputStream;
    }
}
