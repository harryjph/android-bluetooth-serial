package com.harrysoft.androidbluetoothserial;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SimpleBluetoothDeviceInterface {
    void sendMessage(String message);

    /**
     * Set all of the listeners for the interfact
     *
     * @param messageReceivedListener Receive message callback
     * @param messageSentListener Send message callback (indicates that a message was successfully sent)
     * @param errorListener Error callback
     */
    void setListeners(@Nullable OnMessageReceivedListener messageReceivedListener,
                      @Nullable OnMessageSentListener messageSentListener,
                      @Nullable OnErrorListener errorListener);

    /**
     * Set the message received listener
     *
     * @param listener Receive message callback
     */
    void setMessageReceivedListener(@Nullable OnMessageReceivedListener listener);

    /**
     * Set the message sent listener
     *
     * @param listener Send message callback (indicates that a message was successfully sent)
     */
    void setMessageSentListener(@Nullable OnMessageSentListener listener);

    /**
     * Set the error listener
     *
     * @param listener Error callback
     */
    void setErrorListener(@Nullable OnErrorListener listener);

    /**
     * @return The BluetoothSerialDevice instance that the interface is wrapping.
     */
    @NotNull
    BluetoothSerialDevice getDevice();

    interface OnMessageReceivedListener {
        void onMessageReceived(@NotNull String message);
    }

    interface OnMessageSentListener {
        void onMessageSent(@NotNull String message);
    }

    interface OnErrorListener {
        void onError(@NotNull Throwable error);
    }
}
