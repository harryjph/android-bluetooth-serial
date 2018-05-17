package com.harrysoft.androidbluetoothserial.demoapp;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.harrysoft.androidbluetoothserial.BluetoothManager;

import java.util.List;
import java.util.Set;

public class MainActivityViewModel extends AndroidViewModel {

    // Our BluetoothManager!
    private BluetoothManager bluetoothManager;

    // The paired devices list tha the activity sees
    private MutableLiveData<List<BluetoothDevice>> pairedDeviceList = new MutableLiveData<>();

    // A variable to help us not setup twice
    private boolean viewModelSetup = false;

    // Called by the system, this is just a constructor that matches AndroidViewModel.
    public MainActivityViewModel(@NonNull Application application) {
        super(application);
    }

    // Called in the activity's onCreate(). Checks if it has been called before, and if not, sets up the data.
    // Returns true if everything went okay, or false if there was an error and therefore the activity should finish.
    public boolean setupViewModel() {
        // Check we haven't already been called
        if (!viewModelSetup) {
            viewModelSetup = true;

            // Setup our BluetoothManager
            bluetoothManager = BluetoothManager.getInstance();
            if (bluetoothManager == null) {
                // Bluetooth unavailable on this device :( tell the user
                Toast.makeText(getApplication(), R.string.no_bluetooth, Toast.LENGTH_LONG).show();
                // Tell the activity there was an error and to close
                return false;
            }
        }
        // If we got this far, nothing went wrong, so return true
        return true;
    }

    // Called by the activity to request that we refresh the list of paired devices
    public void refreshPairedDevices() {
        pairedDeviceList.postValue(bluetoothManager.getPairedDevicesList());
    }

    // Called when the activity finishes - clear up after ourselves.
    @Override
    protected void onCleared() {
        bluetoothManager.close();
    }

    // Getter method for the activity to use.
    public LiveData<List<BluetoothDevice>> getPairedDeviceList() { return pairedDeviceList; }
}
