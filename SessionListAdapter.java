package com.example.pingpongrobot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SessionListAdapter extends ArrayAdapter<SessionModel> {

    private static final String TAG = "player";
    private Context mContext;
    int mResource;

    public SessionListAdapter(@NonNull Context context, int resource, @NonNull List<SessionModel> objects) {
        super(context, resource, objects);
        this.mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String sID = String.valueOf(getItem(position).getId());
        String sMode = getItem(position).getMode();
        String sBalls = String.valueOf(getItem(position).getBalls());
        String sSpeed = String.valueOf(getItem(position).getSpeed());
        String sTime = String.valueOf(getItem(position).getTime());

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false );
        TextView ID = convertView.findViewById(R.id.sessionID);
        TextView mode = convertView.findViewById(R.id.textMode);
        TextView balls = convertView.findViewById(R.id.TextBalls);
        TextView speed = convertView.findViewById(R.id.textSpeed);
        TextView time = convertView.findViewById(R.id.textTime);

        ID.setText("Session " + sID);
        mode.setText(sMode);
        balls.setText("Balls = " + sBalls);
        speed.setText("Speed = " + sSpeed + "m/s");
        time.setText("Time = " + sTime + "s");

        return convertView;
    }
}
