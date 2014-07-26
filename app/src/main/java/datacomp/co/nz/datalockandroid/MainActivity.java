package datacomp.co.nz.datalockandroid;

import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;

public class MainActivity  extends BlunoLibrary {
    static final String TAG = "MainActivity";

    //main flow views
    private Button buttonScan;
//    private CircularProgressButton buttonWifiUnlock;

    //Registration flow views
    EditText email;
    EditText password;
    CircularProgressButton registerButton;


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

        if (getPreferences(MODE_PRIVATE).getInt("PIN", -1) != -1) {
            Log.d(TAG, "PIN: " + getPreferences(MODE_PRIVATE).getInt("PIN", -1));
            mainFlow();
        } else {
            Log.d(TAG, "No Pin");
            registrationFlow();
        }
    }


    private void mainFlow(){

        setContentView(R.layout.activity_main);
        onCreateProcess();														//onCreate Process by BlunoLibrary

        serialBegin(115200);
//        buttonWifiUnlock = (CircularProgressButton) findViewById(R.id.wifi_unlock);
//
//        buttonWifiUnlock.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new UnlockDoorTask().execute("");
//            }
//        });

        buttonScan = (Button) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
//        buttonScanOnClickProcess(); // change this so it's not coupled with the button (may be necessary)

        getSupportFragmentManager().beginTransaction().replace(R.id.container_layout, TabbedPagerFragment.newInstance(), TabbedPagerFragment.TAG).commit();
    }

    private void registrationFlow(){
        setContentView(R.layout.registration_layout);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        registerButton = (CircularProgressButton) findViewById(R.id.register_button);


        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                registerButton.setProgress(0);
                registerButton.setProgress(1);
                if (email.getText().toString().trim().isEmpty() || password.getText().toString().trim().isEmpty()){
                    //fail
                    simulateErrorProgress(registerButton);
                } else {
                    //success

                    simulateSuccessProgress(registerButton);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_unlock:
                new UnlockDoorTask().execute("");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void simulateErrorProgress(final CircularProgressButton button) {
        ValueAnimator widthAnimation = ValueAnimator.ofInt(1, 99);
        widthAnimation.setDuration(500);
        widthAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                button.setProgress(value);
                if (value == 99) {
                    button.setProgress(-1);
                }
            }
        });
        widthAnimation.start();
    }

    private void simulateSuccessProgress(final CircularProgressButton button) {
        ValueAnimator widthAnimation = ValueAnimator.ofInt(1, 100);
        widthAnimation.setDuration(500);
        widthAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                button.setProgress(value);
                Log.d(TAG, "animation value: " + value);
                // Finish the activity at the end of animation
                if (value == 100) {
                    //move to main flow
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getPreferences(MODE_PRIVATE).edit().putInt("PIN", 1234).commit();
                            mainFlow();
                        }
                    }, 700);


                }
            }
        });
        widthAnimation.start();
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
        try {
            onDestroyProcess();														//onDestroy Process by BlunoLibrary
        } catch (Exception e) {
            Log.e(TAG, "crash in onDestroy...", e);
        }
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
        //The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
        Log.w(TAG, "receiving data... should be doing something with it... this is the data: " + theString);
    }



}