package com.example.pingpongrobot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SessionActivity extends AppCompatActivity {
    private PrintWriter output;
    private BufferedReader input;
    private enum Mode {AUTOMATIC, MANUAL};
    private Mode mode;
    private TextView textTitle, textSpeed,textCounter;
    private Chronometer timer;
    private Button ButtonstartStop, buttonRight, buttonLeft;
    private int speed, counter = 0;
    private long lastTime;
    private Boolean playing = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        textTitle = (TextView) findViewById(R.id.textTitle);
        textSpeed = (TextView) findViewById(R.id.textSpeed2);
        textCounter = (TextView) findViewById(R.id.textCounter);
        timer = (Chronometer) findViewById(R.id.textTimer);
        ButtonstartStop = (Button) findViewById(R.id.buttonStartStop);
        buttonLeft = (Button) findViewById(R.id.buttonLeft);
        buttonRight = (Button) findViewById(R.id.buttonRight);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String modeString = extras.getString("mode");
            mode = Mode.valueOf(modeString);
            speed = extras.getInt("speed");
        }
        textTitle.setText(mode.toString());
        textSpeed.setText(String.valueOf(speed));
        Socket socket = MainActivity.SocketHandler.getSocket();
        try {
            output = new PrintWriter(socket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        switch (mode) {
            case AUTOMATIC:
                buttonLeft.setVisibility(View.INVISIBLE);
                buttonRight.setVisibility(View.INVISIBLE);
        }

    }

    class threadIn implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine();
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Input: " + message, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        //new Thread(new threadConnected()).start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class threadOut implements Runnable {
        private String message;
        threadOut(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            output.write(message);
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    
                }
            });
        }
    }

    public void onStartStopButton( View caller)
    {
        if (playing == false){ startSession();}
        else{ stopSession();}
    }

    public void startSession(){
        if (lastTime != 0){
            timer.setBase(timer.getBase() + SystemClock.elapsedRealtime() - lastTime);
        }
        else{
            timer.setBase(SystemClock.elapsedRealtime());
        }
        timer.start();
        ButtonstartStop.setText("Stop");
        playing = true;
        signalRunnable.run();
    }

    public void stopSession(){
        lastTime = SystemClock.elapsedRealtime();
        timer.stop();
        ButtonstartStop.setText("Start");
        playing = false;
        handler.removeCallbacks(signalRunnable);
    }

    private Runnable signalRunnable = new Runnable() {
        @Override
        public void run() {
            new Thread(new threadOut("TOGGLE")).start();
            counter++;
            textCounter.setText(String.valueOf(counter));
            handler.postDelayed(signalRunnable,60000/speed);
        }
    };

    public void onButBack(View caller){
        stopSession();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("speed", speed);
        startActivity(intent);
    }

}