package com.example.ofan.Database;

import android.content.Context;
import android.content.SharedPreferences;

import com.ficat.easyble.BleDevice;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class BleDatabase {
    private SharedPreferences BleDatabase;
    private Gson gson;

    public BleDatabase(Context context) {
        BleDatabase = context.getSharedPreferences("BleDatabase",Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveDevice(ArrayList<BleDevice> deviceList) {
        SharedPreferences.Editor editor = BleDatabase.edit();
        editor.putString("ble", gson.toJson(deviceList));
        editor.apply();
    }

    public ArrayList<BleDevice> getBleList() {
        String deviceString = BleDatabase.getString("ble", null);
        Type BleListType = new TypeToken<ArrayList<BleDevice>>(){}.getType();
        ArrayList<BleDevice> deviceList = gson.fromJson(deviceString,BleListType);

        if(deviceList != null) return deviceList;
        else return new ArrayList<>();
    }

    public void saveRecentDevice(ArrayList<BleDevice> deviceList) {
        SharedPreferences.Editor editor = BleDatabase.edit();
        editor.putString("recentble", gson.toJson(deviceList));
        editor.apply();
    }

    public ArrayList<BleDevice> getRecentBleList() {
        String deviceString = BleDatabase.getString("recentble", null);
        Type BleListType = new TypeToken<ArrayList<BleDevice>>(){}.getType();
        ArrayList<BleDevice> deviceList = gson.fromJson(deviceString,BleListType);

        if(deviceList != null) return deviceList;
        else return new ArrayList<>();
    }
}
