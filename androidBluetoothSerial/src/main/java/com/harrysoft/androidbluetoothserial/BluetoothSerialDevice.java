package com.harrysoft.androidbluetoothserial;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface BluetoothSerialDevice {
    /**
     * @param message The message to send to the device
     * @return An RxJava Completable to asynchronously
     *          send the message.
     */
    Completable send(String message);

    /**
     * @return An RxJava Flowable that, when observed, will
     *          provide a stream of messages from the device.
     *          A message is considered to be terminated by a
     *          newline ('\n') character. If a newline is not
     *          received, the message will continue buffering
     *          forever. If this is not the desired behaviour,
     *          please manually manage the input using getInputStream()
     */
    @NotNull
    Flowable<String> openMessageStream();

    /**
     * Wrap using a SimpleBluetoothDeviceInterface.
     * This makes things a lot simpler within the class accessing this device
     *
     * @return a SimpleBluetoothDeviceInterface that will access this device object
     */
    @NotNull
    SimpleBluetoothDeviceInterfaceImpl toSimpleDeviceInterface();

    /**
     * @return The MAC address of the closed bluetooth device
     */
    @NotNull
    String getMac();

    /**
     * @return The underlying InputStream representing the device's input.
     *          This can be used to manually manage the device's input,
     *          and is most useful in cases where the device does not send
     *          a newline character at the end of each message.
     */
    @NotNull
    InputStream getInputStream();
}
