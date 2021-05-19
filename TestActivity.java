package com.example.pingpongrobot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class TestActivity extends AppCompatActivity {
    private TextView textTitle, textSpeed,textCounter;
    private Chronometer timer;
    private Button ButtonstartStop;
    private int counter, speed;
    private long lastTime;
    private Boolean playing = false;
    private Handler handler = new Handler();
    String mode;
    private PrintWriter output;
    private BufferedReader input;
    BluetoothService bservice;

    //database stuff
    DataBaseHelper dataBaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButtonstartStop = (Button) findViewById(R.id.buttonStartStop);
        textCounter = (TextView) findViewById(R.id.textCounter);
        timer = (Chronometer) findViewById(R.id.textTimer);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
        }
        else{
            speed = extras.getInt("Speed");
            mode = extras.getString("Mode");
        }

        if (lastTime != 0){
            timer.setBase(timer.getBase() + SystemClock.elapsedRealtime() - lastTime);
        }
        else{
            timer.setBase(SystemClock.elapsedRealtime());
        }
        timer.start();
        //textCounter.setText(getSpeed(speed));
        ButtonstartStop.setText("Stop");
        ButtonstartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(a);

                SessionModel sessionModel;
                double sessionTime = SystemClock.elapsedRealtime();
                int dataBaseTime = ((int)sessionTime)/1000;
                try {
                    sessionModel = new SessionModel(-1, mode,0 , true, dataBaseTime, 60.0 );

                }
                catch (Exception e){
                    Toast.makeText(TestActivity.this, "Error creating session", Toast.LENGTH_SHORT).show();
                    sessionModel = new SessionModel(-1, "error", 0, false, 0, 0);
                }
                DataBaseHelper dataBaseHelper = new DataBaseHelper(TestActivity.this);
                boolean success = dataBaseHelper.addOne(sessionModel);

                Toast.makeText(TestActivity.this, "added= " + success, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);


                intent.putExtra("speed", speed);
                startActivity(intent);
            }
        });
        getSpeed(speed);
    }


    public int getSpeed(int speed) {
        switch (speed) {
            case 1:
                final Handler someHandler = new Handler(Looper.getMainLooper());
                someHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        counter++;
                        someHandler.postDelayed(this,4000);
                    }
                },10);
                break;
            case 2:
                final Handler someHandlr = new Handler(Looper.getMainLooper());
                someHandlr.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        counter++;
                        someHandlr.postDelayed(this,2000);
                    }
                },10);
                break;
            case 3:
                final Handler omeHandler = new Handler(Looper.getMainLooper());
                omeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        counter++;
                        omeHandler.postDelayed(this,1333);
                    }
                },10);
                break;
            case 4:
                final Handler someHandle = new Handler(Looper.getMainLooper());
                someHandle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        counter++;
                        someHandle.postDelayed(this,1000);
                    }
                },10);

                break;
            case 0:
                counter = 0;
                break;
        }
        textCounter.setText(String.valueOf(counter));
        return counter;

    }
}