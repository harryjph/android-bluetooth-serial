package com.harrysoft.androidbluetoothserial;

import android.support.annotation.Nullable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SimpleBluetoothDeviceInterface {

    private final BluetoothSerialDevice device;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    private OnMessageReceivedListener messageReceivedListener;
    @Nullable
    private OnMessageSentListener messageSentListener;
    @Nullable
    private OnErrorListener errorListener;

    SimpleBluetoothDeviceInterface(BluetoothSerialDevice device) {
        this.device = device;

        compositeDisposable.add(device.openMessageStream()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onReceivedMessage, this::onError));
    }

    public void sendMessage(String message) {
        device.requireNotClosed();
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

    /**
     * Set all of the listeners for the interfact
     *
     * @param messageReceivedListener Receive message callback
     * @param messageSentListener Send message callback (indicates that a message was successfully sent)
     * @param errorListener Error callback
     */
    public void setListeners(@Nullable OnMessageReceivedListener messageReceivedListener,
                             @Nullable OnMessageSentListener messageSentListener,
                             @Nullable OnErrorListener errorListener) {
        this.messageReceivedListener = messageReceivedListener;
        this.messageSentListener = messageSentListener;
        this.errorListener = errorListener;
    }

    /**
     * Set the message received listener
     *
     * @param listener Receive message callback
     */
    public void setMessageReceivedListener(@Nullable OnMessageReceivedListener listener) {
        messageReceivedListener = listener;
    }

    /**
     * Set the message sent listener
     *
     * @param listener Send message callback (indicates that a message was successfully sent)
     */
    public void setMessageSentListener(@Nullable OnMessageSentListener listener) {
        messageSentListener = listener;
    }

    /**
     * Set the error listener
     *
     * @param listener Error callback
     */
    public void setErrorListener(@Nullable OnErrorListener listener) {
        errorListener = listener;
    }

    /**
     * @return The device instance that the interface is wrapping around.
     */
    BluetoothSerialDevice getDevice() {
        return device;
    }

    void close() {
        compositeDisposable.dispose();
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(String message);
    }

    public interface OnMessageSentListener {
        void onMessageSent(String message);
    }

    public interface OnErrorListener {
        void onError(Throwable error);
    }
}
