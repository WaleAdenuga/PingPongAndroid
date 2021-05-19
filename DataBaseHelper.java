package com.example.pingpongrobot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String PLAYER_TABLE = "PLAYER_TABLE";
    public static final String MODE = "MODE";
    public static final String BALL_COUNT = "BALL_COUNT";
    public static final String COLUMN_ID = "ID";
    public static final String TIME_PLAYED = "TIME_PLAYED";
    public static final String SPEED= "SPEED";

    public DataBaseHelper(@Nullable Context context) {
        super(context, "shooter.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE "+ PLAYER_TABLE + " ("  + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + MODE + " TEXT, " + BALL_COUNT + " INT, " + TIME_PLAYED + " INT, " + SPEED + " REAL)";

        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTable = "DROP TABLE IF EXISTS " + MODE;
        db.execSQL(dropTable);
    }

    public boolean addOne(SessionModel sessionModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(MODE, sessionModel.getMode());
        cv.put(BALL_COUNT, sessionModel.getBalls());
        cv.put(TIME_PLAYED, sessionModel.getTime());
        cv.put(SPEED, sessionModel.getSpeed());
        long insert = db.insert(PLAYER_TABLE, null, cv );
        if ( insert == -1) {
            return false;
        }
        else {return true; }

    }
    public boolean deleteOne(SessionModel sessionModel){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + PLAYER_TABLE + " WHERE " + COLUMN_ID + " = " + sessionModel.getId();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()){
            return true;
        }
        else {return false;}
     }

    public List<SessionModel> getEveryone() {
        List<SessionModel> returnList = new ArrayList<>();
        // get data from database
        String queryString = "SELECT * FROM " + PLAYER_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()){
            //loop through the cursor (result set) and create customer objects and put them in the list
            do {
                 int playerID = cursor.getInt(0);
                 String playerName= cursor.getString(1);
                 int playerAge = cursor.getInt(2);
                 int timePlayed = cursor.getInt(3);
                 double ballSpeed = cursor.getDouble(4);
                 SessionModel newPlayer = new SessionModel(playerID, playerName, playerAge, true, timePlayed, ballSpeed);
                 returnList.add(newPlayer);
            } while (cursor.moveToNext());

        }
        else {
            //dont add anything to the list
        }
        //remember to close cursor and database
        cursor.close();
        db.close();
        return returnList;
    }
}
