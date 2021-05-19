package com.example.pingpongrobot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.UUID;
import com.example.pingpongrobot.BluetoothService;

public class MainActivity extends AppCompatActivity {
    private int speed = 30;
    String firstAddress;
    String info;
    String difSelected = "Recruit";
    String mode = "Automatic";
    boolean checkConnected = false;

    private ImageView image;
    String dif_tag = "0000 1111";
    String manualTag = "0000 1000";
    String autoTag = "0000 1110";
    String speedTag = "0000 1100";
    String startTag = "1111 1111";
    String stopTag = "0000 0000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.instructions);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), instructionsActivity.class);
                startActivity(intent);
            }
        });
        //Get confirmed values for desired states from linked activities
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
        }
        else{
            info = extras.getString("Name");
            firstAddress = extras.getString("MAC");
            checkConnected = extras.getBoolean("Connection");
        }
        BluetoothService bservice = new BluetoothService();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        checkConnected = prefs.getBoolean("Connection", false);
        if (!bservice.isIsConnected()) {
            Intent service = new Intent(getApplicationContext(), BluetoothService.class);
            service.putExtra("MAC", firstAddress);
            service.putExtra("Name", info);
            startService(service);
        } else {
            Toast.makeText(this,"Device already connected", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onBackPressed() {

        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    protected void onDestroy() {
        BluetoothService service = new BluetoothService();
        service.onDestroy();
        super.onDestroy();
    }



    public void onPlayButton(View caller)
    {
        BluetoothService service = new BluetoothService();
        service.sendData("Play button was clicked"); //Play button clicked

        Intent intent = new Intent(this, SessionActivity.class);
        startActivity(intent);

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