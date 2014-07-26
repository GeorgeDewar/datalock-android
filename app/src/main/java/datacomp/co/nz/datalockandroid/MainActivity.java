package datacomp.co.nz.datalockandroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity  extends BlunoLibrary {
    static final String TAG = "MainActivity";

    private Button buttonScan;
    private Button buttonSerialSend;
    private EditText serialSendText;
    private TextView serialReceivedText;
    private Button buttonConnect;
    private Button buttonWifiUnlock;
    private Button buttonRegister;



    private PendingIntent pendingIntent;
    private AlarmManager manager;
    /*
     address: D0:39:72:A0:9A:BE
     */

    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onCreateProcess();														//onCreate Process by BlunoLibrary

        serialBegin(115200);
        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);


//set the Uart Baudrate on BLE chip to 115200

        serialReceivedText=(TextView) findViewById(R.id.serialReveicedText);	//initial the EditText of the received data
        serialSendText=(EditText) findViewById(R.id.serialSendText);			//initial the EditText of the sending data

//        buttonConnect = (Button) findViewById(R.id.button_connect);
        buttonWifiUnlock = (Button) findViewById(R.id.wifi_unlock);
        buttonRegister = (Button) findViewById(R.id.register);

        if (getPreferences(MODE_PRIVATE).getInt("PIN", -1) != -1) {
            Log.d(TAG, "PIN: " + getPreferences(MODE_PRIVATE).getInt("PIN", -1));
        } else {
            Log.d(TAG, "No Pin");
        }

        buttonRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "registering PIN");
                getPreferences(MODE_PRIVATE).edit().putInt("PIN", 1234).commit();
            }
        });

        buttonWifiUnlock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new UnlockDoorTask().execute("");
            }
        });

        buttonSerialSend = (Button) findViewById(R.id.buttonSerialSend);		//initial the button for sending the data
        buttonSerialSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                serialSend(serialSendText.getText().toString());				//send the data to the BLUNO
            }
        });

//        buttonConnect.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                connectToDevice("D0:39:72:A0:9A:BE");
//            }
//        });
        buttonScan = (Button) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
        buttonScanOnClickProcess();

//
//        buttonScan.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//
////                buttonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
//            }
//        });
    }


    public void startAlarm(View view) {
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        int interval = 10000;

        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }



    protected void onResume(){
        super.onResume();
        System.out.println("BlUNOActivity onResume");
//        onResumeProcess();														//onResume Process by BlunoLibrary
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        onPauseProcess();														//onPause Process by BlunoLibrary
    }

    protected void onStop() {
        super.onStop();
        onStopProcess();														//onStop Process by BlunoLibrary
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
        switch (theConnectionState) {											//Four connection state
            case isConnected:
                buttonScan.setText("Connected");
                break;
            case isConnecting:
                buttonScan.setText("Connecting");
                break;
            case isToScan:
                buttonScan.setText("Scan");
                break;
            case isScanning:
                buttonScan.setText("Scanning");
                break;
            case isDisconnecting:
                buttonScan.setText("isDisconnecting");
                break;
            default:
                break;
        }
    }

    @Override
    public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
        // TODO Auto-generated method stub
        serialReceivedText.append(theString);							//append the text into the EditText
        //The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.

    }



    private class UnlockDoorTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try{

                URL url = new URL("http://172.26.75.139:3000/api/remote_unlock");

                Log.d(TAG, "url: " + url);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("GET");

                con.setRequestProperty("Accept", "*/*");
                con.setRequestProperty("Host", "172.26.75.139:3000");
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.2; GT-I9505 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.141 Mobile Safari/537.36");

                con.setUseCaches (false);
                con.setDoInput(true);
                con.setDoOutput(false);

                int status = con.getResponseCode();
                String message = con.getResponseMessage();
                Log.d(TAG, String.valueOf(status) + "  -  " + message);

                BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }

                Log.d(TAG, "input stream: \n" + total.toString());


//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), printResult, Toast.LENGTH_SHORT).show();
//
//                    }
//                });

//                if (status == 200) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            TaskArrayAdapter adapter = (TaskArrayAdapter) lv.getAdapter();
//                            adapter.remove(adapter.getItem(pos));
//                            adapter.notifyDataSetChanged();
//                        }
//                    });
//
//                }

//                return null;

            } catch(Exception e){
                Log.e(TAG, "Failed to connect: ", e);
            }
                return null;
        }



        protected void onProgressUpdate(Integer... params){
            //Update a progress bar here, or ignore it, it's up to you
        }

        protected void onPostExecute(String result){
            Log.d(TAG, "post execute");
//            Log.d(TAG, "respons: " + result.getStatusLine());
        }

        protected void onCancelled(){
        }

    }




}