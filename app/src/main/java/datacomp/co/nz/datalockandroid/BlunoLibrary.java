package datacomp.co.nz.datalockandroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public abstract  class BlunoLibrary extends ActionBarActivity{

	private Context mainContext=this;

    public static final String SERVER_ARG= "server";

	
//	public BlunoLibrary(Context theContext) {
//		
//		mainContext=theContext;
//	}
	
	public abstract void onConectionStateChange(connectionStateEnum theconnectionStateEnum);

	public abstract void onSerialReceived(String theString);

	public void serialSend(String theString){
		if (mConnectionState == connectionStateEnum.isConnected) {
			Log.d(TAG, "is connected, doing send");
            mSCharacteristic.setValue(theString);
			mBluetoothLeService.writeCharacteristic(mSCharacteristic);
		} else {
            Log.w(TAG, "not connected");
        }
	}
	
	private int mBaudrate=115200;	//set the default baud rate to 115200
	private String mPassword="AT+PASSWOR=DFRobot\r\n";

    private long lastUnlockTime;

    private ArrayList<Integer> rssis = new ArrayList<Integer>(10);
	
	
	private String mBaudrateBuffer = "AT+CURRUART="+mBaudrate+"\r\n";
	
	public void serialBegin(int baud){
		mBaudrate=baud;
		mBaudrateBuffer = "AT+CURRUART="+mBaudrate+"\r\n";
	}
	
	
	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}

    private static BluetoothGattCharacteristic mSCharacteristic, mModelNumberCharacteristic, mSerialPortCharacteristic, mCommandCharacteristic;
    BluetoothLeService mBluetoothLeService;

    Service dummyService;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private LeDeviceListAdapter mLeDeviceListAdapter=null;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning =false;
	AlertDialog mScanDeviceDialog;
    private String mDeviceName;
    private String mDeviceAddress;
	public enum connectionStateEnum{isNull, isScanning, isToScan, isConnecting , isConnected, isDisconnecting};
	public connectionStateEnum mConnectionState = connectionStateEnum.isNull;
	private static final int REQUEST_ENABLE_BT = 1;

	private Handler mHandler= new Handler();
	
	public boolean mConnected = false;

    private final static String TAG = BlunoLibrary.class.getSimpleName();

    private Runnable mConnectingOverTimeRunnable=new Runnable(){

		@Override
		public void run() {
        	if(mConnectionState==connectionStateEnum.isConnecting)
			mConnectionState=connectionStateEnum.isToScan;
			onConectionStateChange(mConnectionState);
			mBluetoothLeService.close();
		}};


		
    private Runnable mDisonnectingOverTimeRunnable=new Runnable(){

		@Override
		public void run() {
        	if(mConnectionState==connectionStateEnum.isDisconnecting)
			mConnectionState=connectionStateEnum.isToScan;
			onConectionStateChange(mConnectionState);
			mBluetoothLeService.close();
		}};
    
	public static final String SerialPortUUID="0000dfb1-0000-1000-8000-00805f9b34fb";
	public static final String CommandUUID="0000dfb2-0000-1000-8000-00805f9b34fb";
    public static final String ModelNumberStringUUID="00002a24-0000-1000-8000-00805f9b34fb";
	
    public void onCreateProcess()
    {
    	if(!initiate())
		{
			Toast.makeText(mainContext, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			((Activity) mainContext).finish();
		}
		
        Intent gattServiceIntent = new Intent(mainContext, BluetoothLeService.class);
        mainContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    
    
    
    public void onResumeProcess() {
    	System.out.println("BlUNOActivity onResume");
		// Ensures Bluetooth is enabled on the device. If Bluetooth is not
		// currently enabled,
		// fire an intent to display a dialog asking the user to grant
		// permission to enable it.
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				((Activity) mainContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		
		
	    mainContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

	}
    

    public void onPauseProcess() {
    	System.out.println("BLUNOActivity onPause");
		scanLeDevice(false);
		mainContext.unregisterReceiver(mGattUpdateReceiver);
		mLeDeviceListAdapter.clear();
    	mConnectionState=connectionStateEnum.isToScan;
    	onConectionStateChange(mConnectionState);
		mScanDeviceDialog.dismiss();
		if(mBluetoothLeService!=null)
		{
            getGattServices(mBluetoothLeService.getSupportedGattServices());
			mBluetoothLeService.disconnect();
            mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);

//			mBluetoothLeService.close();
		}
		mSCharacteristic=null;

	}

	
	public void onStopProcess() {
		System.out.println("MiUnoActivity onStop");
		if(mBluetoothLeService!=null)
		{
//			mBluetoothLeService.disconnect();
//            mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
        	mHandler.removeCallbacks(mDisonnectingOverTimeRunnable);
			mBluetoothLeService.close();
		}
		mSCharacteristic=null;
	}

	public void onDestroyProcess() {
        mainContext.unbindService(mServiceConnection);
        mBluetoothLeService = null;
	}
	
	public void onActivityResultProcess(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			((Activity) mainContext).finish();
			return;
		}
	}

	boolean initiate()
	{
		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!mainContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			return false;
		}
		
		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) mainContext.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
	
		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			return false;
		}
		return true;
	}



	 // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
		@Override
        public void onReceive(Context context, Intent intent) {
        	final String action = intent.getAction();
            System.out.println("mGattUpdateReceiver->onReceive->action="+action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            	mHandler.removeCallbacks(mConnectingOverTimeRunnable);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mConnectionState = connectionStateEnum.isToScan;
                onConectionStateChange(mConnectionState);
            	mHandler.removeCallbacks(mDisonnectingOverTimeRunnable);
            	mBluetoothLeService.close();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
            	for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            		System.out.println("ACTION_GATT_SERVICES_DISCOVERED  "+
            				gattService.getUuid().toString());
            	}
            	getGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	if(mSCharacteristic==mModelNumberCharacteristic)
            	{
            		if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA).toUpperCase().startsWith("DF BLUNO")) {
						mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, false);
						mSCharacteristic=mCommandCharacteristic;
						mSCharacteristic.setValue(mPassword);
						mBluetoothLeService.writeCharacteristic(mSCharacteristic);
						mSCharacteristic.setValue(mBaudrateBuffer);
						mBluetoothLeService.writeCharacteristic(mSCharacteristic);
						mSCharacteristic=mSerialPortCharacteristic;
						mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
						mConnectionState = connectionStateEnum.isConnected;
						onConectionStateChange(mConnectionState);
						
					}
            		else {
            			Toast.makeText(mainContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
                        mConnectionState = connectionStateEnum.isToScan;
                        onConectionStateChange(mConnectionState);
					}
            	}
            	else if (mSCharacteristic==mSerialPortCharacteristic) {
            		onSerialReceived(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
				}
            	
            
            	System.out.println("displayData "+intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            	
//            	mPlainProtocol.mReceivedframe.append(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)) ;
//            	System.out.print("mPlainProtocol.mReceivedframe:");
//            	System.out.println(mPlainProtocol.mReceivedframe.toString());

            	
            }
        }
    };
	
    void buttonScanOnClickProcess()
    {
    	switch (mConnectionState) {
		case isNull:
			mConnectionState=connectionStateEnum.isScanning;
			onConectionStateChange(mConnectionState);
			scanLeDevice(true);
//			mScanDeviceDialog.show();
			break;
		case isToScan:
			mConnectionState=connectionStateEnum.isScanning;
			onConectionStateChange(mConnectionState);
			scanLeDevice(true);

//			mScanDeviceDialog.show();
			break;
		case isScanning:
			
			break;

		case isConnecting:
			
			break;
		case isConnected:
			mBluetoothLeService.disconnect();
            mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);

//			mBluetoothLeService.close();
			mConnectionState=connectionStateEnum.isDisconnecting;
			onConectionStateChange(mConnectionState);
			break;
		case isDisconnecting:
			
			break;

		default:
			break;
		}
    	
    	
    }
    
	void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.

			System.out.println("mBluetoothAdapter.startLeScan");
			
			if(mLeDeviceListAdapter != null)
			{
				mLeDeviceListAdapter.clear();
				mLeDeviceListAdapter.notifyDataSetChanged();
			}
			
			if(!mScanning)
			{
				mScanning = true;
				mBluetoothAdapter.startLeScan(mLeScanCallback);

//                mLeDeviceListAdapter.addDevice(mBluetoothAdapter.getRemoteDevice("D0:39:72:A0:9A:BE"));
//                mLeDeviceListAdapter.notifyDataSetChanged();

			}
		} else {
			if(mScanning)
			{
				mScanning = false;
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}
	}
	
	// Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("mServiceConnection onServiceConnected");
        	mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                ((Activity) mainContext).finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	System.out.println("mServiceConnection onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };
	
	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord) {

			((Activity) mainContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					System.out.println("mLeScanCallback onLeScan run ");
//					mLeDeviceListAdapter.addDevice(device);
                    Log.d("BLAH", "address: " + device.getAddress());
//					mLeDeviceListAdapter.notifyDataSetChanged();
                    if (device.getAddress().equals("D0:39:72:A0:9A:BE")){
                        addRssi(rssi, device);
//                        Toast.makeText(getApplicationContext(), "rssi: " + rssi, Toast.LENGTH_SHORT).show();


                    }
				}
			});
		}
	};

    private void addRssi (int rssi, BluetoothDevice device){
        if (rssis.size() == 10){
            rssis.remove(9);
        }
        rssis.add(0, rssi);
        if (rssis.size() == 10){
            Log.d(TAG, "10 rssis: " + rssi);
            double averageRssi = calculateAverage();
            Log.d(TAG, "averageRssi: " + averageRssi);
//            ((TextView) findViewById(R.id.serialReveicedText)).append("10 rssis, average: " + averageRssi);
//            Toast.makeText(getApplicationContext(), "10 rssis, average: "  + averageRssi, Toast.LENGTH_SHORT).show();

            //within about ~5 meters maybe???
            if (averageRssi > -67){
                long currentTime = System.currentTimeMillis();
                if (mDeviceAddress == null ) Log.d(TAG, "mDeviceAddress is null");
                if (lastUnlockTime == 0){
                    // do request
                    lastUnlockTime = currentTime;
                    Log.d(TAG, "sending unlock");
                    new UnlockDoorTask().execute(String.valueOf(getPreferences(MODE_PRIVATE).getInt("PIN", -1)));
                } else if ((currentTime - lastUnlockTime) > 18000) {
                    //do request
                    lastUnlockTime = currentTime;
                    Log.d(TAG, "sending unlock");
                    new UnlockDoorTask().execute(String.valueOf(getPreferences(MODE_PRIVATE).getInt("PIN", -1)));
                } else {
                    //do nothing
                    Log.d(TAG, "less than 15 seconds apart, skip this one");
                }
            }
        }
    }

    private double calculateAverage() {
        Integer sum = 0;
        if(!rssis.isEmpty()) {
            for (Integer mark : rssis) {
                sum += mark;
            }
            return sum.doubleValue() / rssis.size();
        }
        return sum;
    }

    public void connectToDevice(String address){
//        BluetoothDevice bdDevice = mBluetoothAdapter.getRemoteDevice(address);
//        bdDevice.connectGatt(getApplicationContext(), true, mGattCallback);
//        if (mConnectionState == connectionStateEnum.isConnected) {
//            serialSend("unlock");
//        } else {
//            boolean connected = mBluetoothLeService.connect(address);
//            if (connected) {
//                sendUnlockCommand();
//            }
//        }

        boolean connected = mBluetoothLeService.connect(address);

        for(int i=1; i<11; i++){
            System.out.println("Count is: " + i);

            Thread thread=  new Thread(){
                @Override
                public void run(){
                    try {
                        synchronized(this){
                            wait(1000);
                            mBluetoothLeService.mBluetoothGatt.readRemoteRssi();
                        }
                    }
                    catch(InterruptedException ex){
                    }

                    // TODO
                }
            };

            thread.start();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               Log.d(TAG, "2 seconds later trying to send");
                serialSend("unlock");
            }
        }, 2000);
//        boolean connected = mBluetoothLeService.connect(address);
//        Log.d(TAG, "connected: " + connected);
//        if (connected){
////            sendUnlockCommand();
//        }

//
//        mLeDeviceListAdapter.addDevice(mBluetoothAdapter.getRemoteDevice(address));
//        mLeDeviceListAdapter.notifyDataSetChanged();
    }

    private void sendUnlockCommand() {
        if (mConnectionState == connectionStateEnum.isConnected) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendUnlockCommand();
                }
            }, 1000);
        } else {
            Log.d(TAG, "is connected");
            serialSend("unlock");
        }
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "status: " + status);
            Log.d(TAG, "newState: " + newState);

            //Connection established
            if (status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothProfile.STATE_CONNECTED) {

                //Discover services
                gatt.discoverServices();

            } else if (status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothProfile.STATE_DISCONNECTED) {

                //Handle a disconnect event

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            mSCharacteristic.setValue(theString);
            mBluetoothLeService.writeCharacteristic(mSCharacteristic);
            //Now we can start reading/writing characteristics

        }
    };

    private void getGattServices(List<BluetoothGattService> gattServices) {
        Log.d(TAG, "GETGATTSERVICES");
        if (gattServices == null) return;
        String uuid = null;
        mModelNumberCharacteristic=null;
        mSerialPortCharacteristic=null;
        mCommandCharacteristic=null;
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            System.out.println("displayGattServices + uuid="+uuid);
            
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                if(uuid.equals(ModelNumberStringUUID)){
                	mModelNumberCharacteristic=gattCharacteristic;
                	System.out.println("mModelNumberCharacteristic  "+mModelNumberCharacteristic.getUuid().toString());
                }
                else if(uuid.equals(SerialPortUUID)){
                	mSerialPortCharacteristic = gattCharacteristic;
                	System.out.println("mSerialPortCharacteristic  "+mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                }
                else if(uuid.equals(CommandUUID)){
                	mCommandCharacteristic = gattCharacteristic;
                	System.out.println("mSerialPortCharacteristic  "+mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                }
            }
            mGattCharacteristics.add(charas);
        }
        
        if (mModelNumberCharacteristic==null || mSerialPortCharacteristic==null || mCommandCharacteristic==null) {
			Toast.makeText(mainContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
            mConnectionState = connectionStateEnum.isToScan;
            onConectionStateChange(mConnectionState);
		}
        else {
        	mSCharacteristic=mModelNumberCharacteristic;
        	mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
        	mBluetoothLeService.readCharacteristic(mSCharacteristic);
		}
        
    }
    
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
	
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator =  ((Activity) mainContext).getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view
						.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view
						.findViewById(R.id.device_name);
				System.out.println("mInflator.inflate  getView");
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);
			viewHolder.deviceAddress.setText(device.getAddress());

			return view;
		}
	}

    protected class UnlockDoorTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {

                URL url = new URL(getPreferences(Context.MODE_PRIVATE).getString(SERVER_ARG, "") + "/api/remote_unlock");

                Log.d(TAG, "url: " + url);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("GET");

                con.setRequestProperty("Accept", "*/*");
                con.setRequestProperty("Host", getPreferences(Context.MODE_PRIVATE).getString(SERVER_ARG, ""));
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.2; GT-I9505 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.141 Mobile Safari/537.36");

                con.setUseCaches(false);
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
                Log.d(TAG, "status: " + status);

                if (status == 200){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Door successfully unlocked!", Toast.LENGTH_SHORT).show();
                        }
                    });
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

            } catch (Exception e) {
                Log.e(TAG, "Failed to connect: ", e);
            }
            return null;
        }


        protected void onProgressUpdate(Integer... params) {
            //Update a progress bar here, or ignore it, it's up to you
        }

        protected void onPostExecute(String result) {
            Log.d(TAG, "post execute");
//            Log.d(TAG, "respons: " + result.getStatusLine());
        }

        protected void onCancelled() {
        }

    }

}
