package com.example.pingpongrobot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SessionActivity extends AppCompatActivity {
    private TextView textTitle, textSpeed,textCounter;
    private Chronometer timer;
    private ToggleButton ButtonstartStop;
    private int counter = 0;
    private long lastTime;
    private Boolean playing = false;
    private Handler handler = new Handler();

    private static SeekBar seekBarSpeed;
    private static TextView textSeekBar;
    private TextView difficulty;
    private Spinner difSpin;
    private static ToggleButton toggleMode;
    private int speed;
    private Button confirm;
    ProgressBar bar;
    BluetoothService bservice;
    TextView a, b, c, d;

    //database stuff
    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        bservice = new BluetoothService();
        a = (TextView)findViewById(R.id.txtTimer);
        b = (TextView) findViewById(R.id.txtCounter);
        c = (TextView) findViewById(R.id.txtBalls);
        textCounter = (TextView) findViewById(R.id.textCounter);
        timer = (Chronometer) findViewById(R.id.textTimer);
        timer.setVisibility(View.INVISIBLE);
        a.setVisibility(View.INVISIBLE);
        b.setVisibility(View.INVISIBLE);
        c.setVisibility(View.INVISIBLE);
        textCounter.setVisibility(View.INVISIBLE);

        ButtonstartStop = (ToggleButton) findViewById(R.id.buttonStartStop2);
        ButtonstartStop.setBackgroundColor(getResources().getColor(R.color.teal_200));
        bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setVisibility(View.INVISIBLE);
        toggleMode =  (ToggleButton) findViewById(R.id.toggleMode);
        toggleMode.setActivated(true);
        difficulty = (TextView) findViewById(R.id.difText);
        difSpin = (Spinner) findViewById(R.id.spinner3);
        difficulty.setVisibility(View.INVISIBLE);
        difSpin.setVisibility(View.INVISIBLE);

        toggleMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleMode.getText().equals("Automatic")) {
                    difficulty.setVisibility(View.VISIBLE);
                    difSpin.setVisibility(View.VISIBLE);
                } else {
                    difficulty.setVisibility(View.INVISIBLE);
                    difSpin.setVisibility(View.INVISIBLE);
                }
            }
        });

        String[] difficulties = {"Recruit", "Medium", "Veteran"};
        ArrayAdapter a = new ArrayAdapter(this, android.R.layout.simple_spinner_item, difficulties);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difSpin.setAdapter(a);

        seekbar();
        startSession();
    }

    public void seekbar(){
        seekBarSpeed = (SeekBar) findViewById(R.id.seekBarSpeed);
        textSeekBar = (TextView) findViewById(R.id.textSeekBar);
        textSeekBar.setText("#balls/minute = " + (seekBarSpeed.getProgress()));
        seekBarSpeed.setProgress(0);
        //seekBarSpeed.incrementProgressBy(1);
        textSeekBar.setText("Speed Level = " + (speed));
        //speed = seekBarSpeed.getProgress();
        seekBarSpeed.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress_value;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progress_value = progress;
                        textSeekBar.setText("Speed Level = " + (progress));
                        speed = progress_value;} //Seekbar of 1 to 4, but 15 balls for each seekbar progress
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { textSeekBar.setText("Speed Level = " + progress_value); }
                }
        );
    }

    public void startSession(){
        ButtonstartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ButtonstartStop.getText().equals("Stop")) {
                    bar.setVisibility(View.VISIBLE);
                    //bservice.sendData("Start button was clicked");
                    bservice.sendData(String.valueOf(speed));
                    SystemClock.sleep(200);
                    bservice.sendData(toggleMode.getText().toString());
                    if(toggleMode.getText().toString().contains("Automatic")) {
                        SystemClock.sleep(200);
                        bservice.sendData(difSpin.getSelectedItem().toString());
                    }
                    SystemClock.sleep(300);
                    signalRunnable.run();
                    bar.setVisibility(View.INVISIBLE);
                    timer.setVisibility(View.VISIBLE);
                    a.setVisibility(View.VISIBLE);
                    b.setVisibility(View.VISIBLE);
                    c.setVisibility(View.VISIBLE);
                    textCounter.setVisibility(View.VISIBLE);

                    if (lastTime != 0){
                        timer.setBase(timer.getBase() + SystemClock.elapsedRealtime() - lastTime);
                    }
                    else{
                        timer.setBase(SystemClock.elapsedRealtime());
                    }
                    timer.start();
                    ButtonstartStop.setBackgroundColor(getResources().getColor(R.color.design_default_color_error));
                } else {
                    ButtonstartStop.setBackgroundColor(getResources().getColor(R.color.teal_200));
                    bservice.sendData("5");
                    //bar.setVisibility(View.INVISIBLE);
                    //timer.setVisibility(View.INVISIBLE);
                    //a.setVisibility(View.INVISIBLE);
                    //b.setVisibility(View.INVISIBLE);
                    //c.setVisibility(View.INVISIBLE);
                    textCounter.setVisibility(View.INVISIBLE);
                    timer.stop();
                    handler.removeCallbacks(signalRunnable);
                }


            }
        });

    }

    private Runnable signalRunnable = new Runnable() {
        @Override
        public void run() {
            counter++;
            textCounter.setText(String.valueOf(counter));
            handler.postDelayed(signalRunnable,60000/speed);
        }
    };

    public void StopSession() {

        Intent a = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(a);

        SessionModel sessionModel;
        double sessionTime = SystemClock.elapsedRealtime();
        int dataBaseTime = ((int)sessionTime)/1000;
        try {
            sessionModel = new SessionModel(-1, toggleMode.getText().toString(),0 , true, dataBaseTime, 60.0 );

        }
        catch (Exception e){
            Toast.makeText(SessionActivity.this, "Error creating session", Toast.LENGTH_SHORT).show();
            sessionModel = new SessionModel(-1, "error", 0, false, 0, 0);
        }
        DataBaseHelper dataBaseHelper = new DataBaseHelper(SessionActivity.this);
        boolean success = dataBaseHelper.addOne(sessionModel);

        Toast.makeText(SessionActivity.this, "added= " + success, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);


        intent.putExtra("speed", speed);
        startActivity(intent);
    }

    public void onButBack(View caller){
        StopSession();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("speed", speed);
        startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.finish();
        return super.onOptionsItemSelected(item);
    }
}