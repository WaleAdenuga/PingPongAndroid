package com.example.pingpongrobot;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;


public class HistoryActivity extends AppCompatActivity {

    Button btn_add;
    ToggleButton modeButton;
    EditText et_balls;
    ListView lv_sessionList;
    ArrayAdapter playerAdapter;
    DataBaseHelper databaseHelper;
    public String shooterMode = "Automatic";
    long sessionTime;
    int dataBaseTime;
    long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        btn_add = findViewById(R.id.btn_add);
        lv_sessionList = findViewById(R.id.sessionList);
        et_balls = findViewById(R.id.ballCount);
        modeButton = findViewById(R.id.toggleButton);

        databaseHelper = new DataBaseHelper(HistoryActivity.this);

        ShowPlayersOnList(databaseHelper);

        modeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    shooterMode = "Manual";
                    startTime = SystemClock.elapsedRealtime();
                } else {
                    shooterMode = "Automatic";
                    startTime = SystemClock.elapsedRealtime();
                }

            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionModel sessionModel;
                sessionTime = SystemClock.elapsedRealtime() - startTime;
                dataBaseTime = ((int)sessionTime)/1000;
                try {
                    sessionModel = new SessionModel(-1, shooterMode, Integer.parseInt(et_balls.getText().toString()), true, dataBaseTime, 60.0 );

                }
                catch (Exception e){
                    Toast.makeText(HistoryActivity.this, "Error creating customer", Toast.LENGTH_SHORT).show();
                    sessionModel = new SessionModel(-1, "error", 0, false, 0, 0);
                }
                DataBaseHelper dataBaseHelper = new DataBaseHelper(HistoryActivity.this);

                boolean success = dataBaseHelper.addOne(sessionModel);

                Toast.makeText(HistoryActivity.this, "added= " + success, Toast.LENGTH_SHORT).show();
                ShowPlayersOnList(databaseHelper);

            }
        });
        lv_sessionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SessionModel clickedSession = (SessionModel) parent.getItemAtPosition(position);
                    databaseHelper.deleteOne(clickedSession);
                    ShowPlayersOnList(databaseHelper);
            }
        });


    }

    private void ShowPlayersOnList(DataBaseHelper databaseHelper) {
        List<SessionModel> playerList = databaseHelper.getEveryone();
        SessionListAdapter playerAdapter1 = new SessionListAdapter(HistoryActivity.this, R.layout.session_adapter, playerList);
        lv_sessionList.setAdapter(playerAdapter1);
    }

    }
