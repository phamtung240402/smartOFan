package com.example.ofan.Database;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ofan.Model.TimeItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TimerDatabase {
    private SharedPreferences timerDatabase;
    private Gson gson;

    public TimerDatabase(Context context) {
        timerDatabase = context.getSharedPreferences("timerDatabase",Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveTimer(ArrayList<TimeItem> timerList) {
        SharedPreferences.Editor editor = timerDatabase.edit();
        editor.putString("timer", gson.toJson(timerList));
        editor.apply();
    }

    public ArrayList<TimeItem> getTimerList() {
        String timerString = timerDatabase.getString("timer", null);
        Type timerListType = new TypeToken<ArrayList<TimeItem>>(){}.getType();
        ArrayList<TimeItem> timerList = gson.fromJson(timerString,timerListType);

        if(timerList != null) return timerList;
        else return new ArrayList<>();

    }
}
