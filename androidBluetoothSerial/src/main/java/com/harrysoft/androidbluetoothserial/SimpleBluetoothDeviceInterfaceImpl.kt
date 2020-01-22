package com.harrysoft.androidbluetoothserial

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Implementation of SimpleBluetoothDeviceInterface, package-private
 */
internal class SimpleBluetoothDeviceInterfaceImpl(override val device: BluetoothSerialDeviceImpl) : SimpleBluetoothDeviceInterface {
    private val compositeDisposable = CompositeDisposable()

    private var messageReceivedListener: SimpleBluetoothDeviceInterface.OnMessageReceivedListener? = null
    private var messageSentListener: SimpleBluetoothDeviceInterface.OnMessageSentListener? = null
    private var errorListener: SimpleBluetoothDeviceInterface.OnErrorListener? = null

    init {
        compositeDisposable.add(device.openMessageStream()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.onReceivedMessage(it) }, { this.onError(it) }))
    }

    override fun sendMessage(message: String) {
        device.checkNotClosed()
        compositeDisposable.add(device.send(message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onSentMessage(message) }, { this.onError(it) }))
    }

    /**
     * Internal callback called when the BluetoothSerialDevice receives a message
     *
     * @param message The message received from the bluetooth device
     */
    private fun onReceivedMessage(message: String) {
        if (messageReceivedListener != null) {
            messageReceivedListener!!.onMessageReceived(message)
        }
    }

    /**
     * Internal callback called when the BluetoothSerialDevice sends a message
     *
     * @param message The message sent to the bluetooth device
     */
    private fun onSentMessage(message: String) {
        if (messageSentListener != null) {
            messageSentListener!!.onMessageSent(message)
        }
    }

    /**
     * Internal callback called when a Bluetooth send/receive error occurs
     *
     * @param error The error that occurred
     */
    private fun onError(error: Throwable) {
        if (errorListener != null) {
            errorListener!!.onError(error)
        }
    }

    override fun setListeners(messageReceivedListener: SimpleBluetoothDeviceInterface.OnMessageReceivedListener?,
                              messageSentListener: SimpleBluetoothDeviceInterface.OnMessageSentListener?,
                              errorListener: SimpleBluetoothDeviceInterface.OnErrorListener?) {
        this.messageReceivedListener = messageReceivedListener
        this.messageSentListener = messageSentListener
        this.errorListener = errorListener
    }

    override fun setMessageReceivedListener(listener: SimpleBluetoothDeviceInterface.OnMessageReceivedListener?) {
        messageReceivedListener = listener
    }

    override fun setMessageSentListener(listener: SimpleBluetoothDeviceInterface.OnMessageSentListener?) {
        messageSentListener = listener
    }

    override fun setErrorListener(listener: SimpleBluetoothDeviceInterface.OnErrorListener?) {
        errorListener = listener
    }

    fun close() {
        compositeDisposable.dispose()
    }
}
