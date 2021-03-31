package com.example.pingpongrobot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private static SeekBar seekBarSpeed;
    private static TextView textSeekBar;
    private static ToggleButton toggleMode;
    private PrintWriter output;
    private BufferedReader input;
    private int speed = 1;
    private boolean connected = false;
    private static final int SERVER_PORT = 3333;
    private static final String SERVER_IP = "192.168.4.1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toggleMode =  (ToggleButton) findViewById(R.id.toggleMode);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            new Thread(new threadConnected()).start();
        }
        else{
            speed = extras.getInt("speed");
            connected = true;
        }
        seekbar();
    }

    public static class SocketHandler {
        private static Socket socket;
        public static synchronized Socket getSocket(){ return socket; }
        public static synchronized void setSocket(Socket socket){ SocketHandler.socket = socket; }
    }

    class threadConnected implements Runnable {
        @Override
        public void run() {
            try {
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                SocketHandler.setSocket(socket);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Connected to: " + SERVER_IP + " " + SERVER_PORT , Toast.LENGTH_LONG).show();
                        connected = true;
                    }
                });
                //new Thread(new SessionActivity.threadIn()).start();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    public void seekbar(){
        seekBarSpeed = (SeekBar) findViewById(R.id.seekBarSpeed);
        textSeekBar = (TextView) findViewById(R.id.textSeekBar);
        textSeekBar.setText("#balls/minute = " + seekBarSpeed.getProgress());
        seekBarSpeed.setProgress(0);
        seekBarSpeed.incrementProgressBy(speed);
        textSeekBar.setText("#balls/minute = " + speed);
        seekBarSpeed.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress_value;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progress_value = progress + 1;
                        textSeekBar.setText("#balls/minute = " + progress); }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { textSeekBar.setText("#balls/minute = " + progress_value); }
                }
        );
    }


    public void onPlayButton( View caller)
    {
        if (connected) {
            Intent intent = new Intent(this, SessionActivity.class);
            intent.putExtra("mode", toggleMode.getText().toString().toUpperCase());
            intent.putExtra("speed", seekBarSpeed.getProgress()+1);
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "Not connected to ESP" , Toast.LENGTH_LONG).show();
        }
    }

    public void onHistoryButton( View caller)
    {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void onSettingsButton( View caller)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}