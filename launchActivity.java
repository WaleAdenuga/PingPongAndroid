package com.example.pingpongrobot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.icu.util.Output;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class launchActivity extends AppCompatActivity {
    private ListView list; //list of paired devices for person to connect to
    private TextView text;
    private BluetoothAdapter myBluetooth = null;
    private ProgressBar bar;
    String firstAddress;
    static BluetoothSocket btSocket = null;
    static boolean isConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Set<BluetoothDevice> nearbyDevices;
    private ProgressDialog progress;
    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        launchActivity.this.setTitle("Pair to Bluetooth Device");
        list = (ListView) findViewById(R.id.list);
        text = (TextView) findViewById(R.id.connector);
        bar = (ProgressBar) findViewById(R.id.progressBar2);
        bar.setVisibility(View.INVISIBLE);
        text.setVisibility(View.INVISIBLE);
        //check if the device has bluetooth installed/enabled
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            Toast.makeText(this, "Bluetooth device not available", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (myBluetooth.isEnabled()) {

            } else {
                Intent on = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(on, 1);
            }
        }
        //Make the launch activity work just once
/*        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        if(!preferences.getBoolean(activity, false)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(activity, Boolean.TRUE);
            editor.apply();
        } else {
            finish();
            Intent intent = new Intent(this, SessionActivity.class);
            startActivity(intent);
        }*/
        showPaired();
    }

    private void showPaired(){
        nearbyDevices = myBluetooth.getBondedDevices();
        ArrayList devicelist = new ArrayList();
/*        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //Finding devices
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //Get bluetooth device object from the intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //show in listView
                    devicelist.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };*/
/*        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);*/
        if (nearbyDevices.size() > 0) {
            for(BluetoothDevice bt : nearbyDevices) {
                //get device name and address
                devicelist.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_LONG).show();
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, devicelist);
        list.setAdapter(adapter);
        list.setDividerHeight(3);
        list.setOnItemClickListener(myListClickListener); //when a device on the list is clicked
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Get the device MAC address, that's what matters for connection anyway
            String info = ((TextView) view).getText().toString();
            firstAddress = info.substring(info.length() - 17);

            bar.setVisibility(View.VISIBLE);
            text.setVisibility(View.VISIBLE);
            text.setText("Connecting to " + info);

            Intent a = new Intent(getApplicationContext(), MainActivity.class);
            a.putExtra("Name", info);
            a.putExtra("MAC", firstAddress);
            startActivity(a);
/*            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    switch (msg.arg1) {

                    }
                }
            };*/
        }
    };



    /*public class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;
        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if(btSocket == null || !isConnected) {
                    BluetoothDevice dis = myBluetooth.getRemoteDevice(firstAddress); //connects to device's address and checks if it's available
                    btSocket = dis.createInsecureRfcommSocketToServiceRecord(myUUID); //create RFCOMM connection (SPP)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect(); //start connection
                    testWrite(); //test writing to ESP32 serial monitor from the app
                }
            }
            catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(getApplicationContext(),"Connecting....", "Please wait!!!!");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Is it SPP Bluetooth ?", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                isConnected = true;
            }
            progress.dismiss();
        }
    }

    private void disconnect(){
        if(btSocket != null) {
            try {
                btSocket.close();
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void testWrite() {
        if(btSocket != null) {
            try {
                btSocket.getOutputStream().write("TESTING".toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(this, "It's not working", Toast.LENGTH_SHORT).show();
            }
        }
    }*/
}

