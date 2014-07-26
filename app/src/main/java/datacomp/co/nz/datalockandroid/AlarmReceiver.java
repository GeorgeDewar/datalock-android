package datacomp.co.nz.datalockandroid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String TAG = "AlarmReceiver";
    BluetoothLeService mBluetoothLeService;

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        // For our recurring task, we'll just display a message
//        Toast.makeText(arg0, "I'm running", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "AlarmReceiver broadcastintent received");
        Intent myIntent = new Intent( arg0, BluetoothLeService.class );
        arg0.startService( myIntent );



    }

}
