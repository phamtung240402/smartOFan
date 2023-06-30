package com.example.ofan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ofan.Activity.DeviceActivity;
import com.example.ofan.Database.BleDatabase;
import com.example.ofan.Fragment.DeviceFragment;
import com.example.ofan.Fragment.HomeFragment;
import com.example.ofan.Fragment.TimerFragment;
import com.example.ofan.adapter.ViewPagerAdapter;
import com.example.ofan.utils.ByteUtils;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.callback.BleConnectCallback;
import com.ficat.easyble.gatt.callback.BleWriteCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private BleDatabase bleDatabase;
    private ArrayList<BleDevice> devices = new ArrayList<>();
    private ArrayList<BleDevice> recentDevices = new ArrayList<>();
    private Toolbar toolbar;
    private ViewPager2 mViewPager2;
    private TextView tbTitle;
    private TextView tbCounter;
    private AppCompatImageView tbAdd;
    private ViewPagerAdapter vpAdapter;
    private BottomNavigationView mBottomNav;
    private BleDevice mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        bleDatabase = new BleDatabase(this);
        devices = bleDatabase.getBleList();
        recentDevices = bleDatabase.getRecentBleList();
        if(devices.size() == 0) {
            if(recentDevices.size() != 0) {
                Log.d("oam", String.valueOf(recentDevices.size()));
//                BleManager.getInstance().connect(recentDevices.get(0).address, bleConnectCallback);
            }
        } else {
            mDevice = devices.get(0);
        }
        mBottomNav = findViewById(R.id.mBottomNav);
        mViewPager2 = findViewById(R.id.viewPager);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        tbTitle = findViewById(R.id.tbTitle);
        tbCounter = findViewById(R.id.tbCounter);
        tbAdd = findViewById(R.id.tbAdd);
        vpAdapter = new ViewPagerAdapter(this);
        mViewPager2.setAdapter(vpAdapter);
        tbAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DeviceActivity.class));
                finish();
            }
        });

        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        mBottomNav.getMenu().findItem(R.id.bottomHome).setChecked(true);
                        tbTitle.setText("Trang chủ");
                        tbCounter.setText("Chào mừng quay trở lại");
                        break;
                    case 1:
                        mBottomNav.getMenu().findItem(R.id.bottomTimer).setChecked(true);
                        tbTitle.setText("Lập lịch");
                        tbCounter.setText("Hẹn giờ tắt");
                        break;
                    case 2:
                        mBottomNav.getMenu().findItem(R.id.bottomPair).setChecked(true);
                        tbTitle.setText("Kết nối");
                        tbCounter.setText("Thiết bi đã kết nối");
                        break;
                }
            }
        });

        mBottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottomHome:
                        mViewPager2.setCurrentItem(0);
                        break;
                    case R.id.bottomTimer:
                        mViewPager2.setCurrentItem(1);
                        break;
                    case R.id.bottomPair:
                        mViewPager2.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });
    }

    private BleConnectCallback bleConnectCallback = new BleConnectCallback() {
        @Override
        public void onStart(boolean startConnectSuccess, String info, BleDevice device) {
            Logger.e("start connecting" + startConnectSuccess + " info = " + info);
            if(!startConnectSuccess) {
                Logger.e(info);
            }
        }

        @Override
        public void onConnected(BleDevice device) {
            Toast.makeText(MainActivity.this, "Kết nối thành công", Toast.LENGTH_SHORT).show();
            devices.add(device);
            bleDatabase.saveRecentDevice(devices);
        }

        @Override
        public void onDisconnected(String info, int status, BleDevice device) {
            Log.d("oam", "disconnected");
            recentDevices.clear();
            recentDevices.add(device);
            bleDatabase.saveRecentDevice(recentDevices);
            devices.clear();
            bleDatabase.saveDevice(devices);

        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("connect fail : " + info);
            Toast.makeText(MainActivity.this, "Kết nối thất bại", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(devices.size() != 0) {
//            recentDevices.clear();
//            recentDevices.add(devices.get(0));
//            bleDatabase.saveRecentDevice(recentDevices);
//        }
//        BleManager.getInstance().disconnectAll();
//        BleManager.getInstance().destroy();
//        devices.clear();
//        bleDatabase.saveDevice(devices);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(devices.size() != 0) {
            recentDevices.clear();
            recentDevices.add(devices.get(0));
            bleDatabase.saveRecentDevice(recentDevices);
            BleManager.getInstance().disconnectAll();
        }
        devices.clear();
        bleDatabase.saveDevice(devices);
    }


    private BleWriteCallback writeCallBack = new BleWriteCallback() {
        @Override
        public void onWriteSuccess(byte[] data, BleDevice device) {
            Logger.e("write success:" + ByteUtils.bytes2HexStr(data));
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("write fail:" + info);
        }
    };
}