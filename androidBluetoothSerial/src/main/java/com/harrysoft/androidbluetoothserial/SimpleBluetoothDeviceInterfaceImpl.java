package com.harrysoft.androidbluetoothserial;

import org.jetbrains.annotations.Nullable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Implementation of SimpleBluetoothDeviceInterface, package-private
 */
class SimpleBluetoothDeviceInterfaceImpl implements SimpleBluetoothDeviceInterface {

    private final BluetoothSerialDeviceImpl device;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    private OnMessageReceivedListener messageReceivedListener;
    @Nullable
    private OnMessageSentListener messageSentListener;
    @Nullable
    private OnErrorListener errorListener;

    /**
     * Package private constructor
     */
    SimpleBluetoothDeviceInterfaceImpl(BluetoothSerialDeviceImpl device) {
        this.device = device;

        compositeDisposable.add(device.openMessageStream()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onReceivedMessage, this::onError));
    }

    @Override
    public void sendMessage(String message) {
        device.checkNotClosed();
        compositeDisposable.add(device.send(message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> onSentMessage(message), this::onError));
    }

    /**
     * Internal callback called when the BluetoothSerialDevice receives a message
     *
     * @param message The message received from the bluetooth device
     */
    private void onReceivedMessage(String message) {
        if (messageReceivedListener != null) {
            messageReceivedListener.onMessageReceived(message);
        }
    }

    /**
     * Internal callback called when the BluetoothSerialDevice sends a message
     *
     * @param message The message sent to the bluetooth device
     */
    private void onSentMessage(String message) {
        if (messageSentListener != null) {
            messageSentListener.onMessageSent(message);
        }
    }

    /**
     * Internal callback called when a Bluetooth send/receive error occurs
     *
     * @param error The error that occurred
     */
    private void onError(Throwable error) {
        if (errorListener != null) {
            errorListener.onError(error);
        }
    }

    @Override
    public void setListeners(@Nullable OnMessageReceivedListener messageReceivedListener,
                             @Nullable OnMessageSentListener messageSentListener,
                             @Nullable OnErrorListener errorListener) {
        this.messageReceivedListener = messageReceivedListener;
        this.messageSentListener = messageSentListener;
        this.errorListener = errorListener;
    }

    @Override
    public void setMessageReceivedListener(@Nullable OnMessageReceivedListener listener) {
        messageReceivedListener = listener;
    }

    @Override
    public void setMessageSentListener(@Nullable OnMessageSentListener listener) {
        messageSentListener = listener;
    }

    @Override
    public void setErrorListener(@Nullable OnErrorListener listener) {
        errorListener = listener;
    }

    @Override
    public BluetoothSerialDevice getDevice() {
        return device;
    }

    void close() {
        compositeDisposable.dispose();
    }
}
