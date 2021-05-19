package com.example.pingpongrobot;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService  extends Service {

    static BluetoothSocket btSocket = null;
    private BluetoothAdapter myBluetooth = null;
    static boolean isConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static Handler handler;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;

    String firstAddress;
    String info;
    String difSelected = "Recruit";
    String mode = "Automatic";
    private int speed = 0;
    static Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();
        Bundle extras = intent.getExtras();
        if (extras == null) {
        }
        else{
            info = extras.getString("Name");
            firstAddress = extras.getString("MAC");
            speed = extras.getInt("Speed");
            difSelected = extras.getString("Difficulty");
            mode = extras.getString("Mode");
        }
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        checkBT();

        createConnectThread = new CreateConnectThread(myBluetooth, firstAddress);
        createConnectThread.start();

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch(msg.what) {
                    case CONNECTING_STATUS:
                        switch(msg.arg1) {
                            case 1:
                                //Able to create a socket
                                Toast.makeText(getApplicationContext(),"Connected to " + info,Toast.LENGTH_LONG).show();
                                break;
                            case -1:
                                //Unable to create socket
                                Toast.makeText(getApplicationContext(),"Device fails to connect",Toast.LENGTH_LONG).show();
                                break;
                        }
                }
            }
        };

        return Service.START_STICKY;
    }

    public boolean isIsConnected() {
        return isConnected;
    }

    @Override
    public void onDestroy() {
        createConnectThread.cancel();
        connectedThread.cancel();
        super.onDestroy();
    }

    public void sendData(String message) {
        if (connectedThread != null) {
            connectedThread.write(message);
        } else {
            Toast.makeText(getApplicationContext(),"Data could not be sent", Toast.LENGTH_LONG).show();
        }
    }

    private void checkBT() {
        if (myBluetooth == null) {
            Toast.makeText(this, "Bluetooth device not available", Toast.LENGTH_LONG).show();
            stopSelf();
        } else {
            if (myBluetooth.isEnabled()) {

            } else {
                Intent on = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(on);
            }
        }
    }

    public static class CreateConnectThread extends Thread {
        public CreateConnectThread(BluetoothAdapter adapter, String address) {
            BluetoothDevice bluetoothDevice = adapter.getRemoteDevice(address);
            BluetoothSocket socket = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                socket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.d("TAG", "Socket creation failed");
            }
            btSocket = socket;
        }

        //Cancel discovery because it slows down the connection
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            adapter.cancelDiscovery();
            try {
                btSocket.connect();
                Log.d("TAG", "Device connected");
                isConnected = true;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor edit = preferences.edit();
                edit.putBoolean("Connection", isConnected);
                edit.apply();
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    isConnected = false;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putBoolean("Connection", isConnected);
                    edit.apply();
                    btSocket.close();
                    handler.obtainMessage(CONNECTING_STATUS, -1,-1).sendToTarget();
                } catch (IOException f) {
                    f.printStackTrace();
                }
                return;
            }
            //Perform work from connection in a separate thread
            connectedThread = new ConnectedThread(btSocket);
            connectedThread.run();
        }
        //Closes client socket and causes thread to finish
        public void cancel() {
            try {
                btSocket.close();
                isConnected = false;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor edit = preferences.edit();
                edit.putBoolean("Connection", isConnected);
                edit.apply();
            } catch (IOException e) {
                Log.d("TAG", "Could not close the client socket");
            }
        }
    }

    //Thread for data transfer
    public static class ConnectedThread extends Thread{
        private final BluetoothSocket btSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        public ConnectedThread(BluetoothSocket socket) {
            btSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) { }
            inputStream = in;
            outputStream = out;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes = 0;

            while(true) {
                try {
                    buffer[bytes] = (byte) inputStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n') {
                        readMessage = new String(buffer, 0, bytes);
                        //handler.obtainMessage().sendToTarget();
                        bytes = 0;
                    } else { bytes++;}
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(String input) {
            byte[] bytes = input.getBytes();
            try {
                outputStream.write(bytes);
            } catch (IOException e) {}
        }
        public void cancel() {
            try {
                btSocket.close();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor edit = preferences.edit();
                edit.putBoolean("Connection", isConnected);
                edit.apply();
            } catch (IOException e) {}
        }
    }
}


