package com.example.ofan.Activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.ofan.Database.BleDatabase;
import com.example.ofan.MainActivity;
import com.example.ofan.R;
import com.example.ofan.adapter.CommonRecyclerViewAdapter;
import com.example.ofan.adapter.ScanDeviceAdapter;
import com.example.ofan.utils.ByteUtils;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.callback.BleConnectCallback;
import com.ficat.easyble.gatt.callback.BleWriteCallback;
import com.ficat.easyble.scan.BleScanCallback;

import java.util.ArrayList;

public class DeviceActivity extends AppCompatActivity {
    public static final String SERVICE_BUTTON_OPTIONS = "00EE";
    public static final String UUID_SERVICE_BUTTON_OPTIONS = String.valueOf(ByteUtils.parseUUID(SERVICE_BUTTON_OPTIONS));
    public static final String CHARACTERISTIC_BUTTON_OPTIONS = "EE01";
    public static final String UUID_Characteristic_BUTTON_OPTIONS = String.valueOf(ByteUtils.parseUUID(CHARACTERISTIC_BUTTON_OPTIONS));
    public static final String HEX_WRITE_ONOFF = "00";
    private RecyclerView rv_availableDevices;
    private ScanDeviceAdapter adapter;
    private ArrayList<BleDevice> deviceList = new ArrayList<>();
    private ArrayList<BleDevice> connectedDevices = new ArrayList<>();
    private BleManager manager;
    private BleDevice mDevice;
    String[] permissions = new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_ADMIN};
    int requestCodePermission ;
    private BleDatabase bleDatabase;
    private ImageButton btnBack, btnRefresh;
    Thread thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        initBleManager();
        initView();
        showDeviceByRv();
    }
    
    private void initView() {
        rv_availableDevices = findViewById(R.id.rv_availableDevices);
        btnRefresh = findViewById(R.id.btnRefresh);
        bleDatabase = new BleDatabase(this);
        deviceList.clear();
        bleDatabase.saveDevice(connectedDevices);
        btnBack = findViewById(R.id.btnBack);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!manager.isScanning()) {
                            if (!BleManager.isBluetoothOn()) {
                                BleManager.toggleBluetooth(true);
                            }
                            if(!isGpsOn()) {
                                Toast.makeText(DeviceActivity.this, "Hãy bật GPS", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(!hasPermissions(DeviceActivity.this,permissions)){
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                {
                                    requestPermissions(permissions,requestCodePermission);
                                }
                            }
                            BleManager.getInstance().disconnectAll();
                            deviceList.clear();
                            adapter.notifyDataSetChanged();
                            connectedDevices.clear();
                            bleDatabase.saveDevice(connectedDevices);
                            mDevice = null;
                            startScan();
                        }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showDeviceByRv() {
        rv_availableDevices.setLayoutManager(new LinearLayoutManager(this));
        rv_availableDevices.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 3;
            }
        });
        SparseArray<int[]> res = new SparseArray<>();
        res.put(R.layout.ble_item,new int[]{R.id.txt_itemName,R.id.txt_itemAddress});
        adapter = new ScanDeviceAdapter(this, deviceList, res);
        adapter.setOnItemClickListener(new CommonRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                manager.stopScan();
                BleDevice device = deviceList.get(position);
                BleManager.getInstance().connect(device.address, bleConnectCallback);
            }
        });

        adapter.setOnItemLongClickListener(new CommonRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                BleDevice device = deviceList.get(position);
                if(BleManager.getInstance().isConnected(device.address)) {
                    BleManager.getInstance().disconnect(device.address);
                    Toast.makeText(DeviceActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    mDevice = null;
                }
                else {
                    Toast.makeText(DeviceActivity.this, "cant", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rv_availableDevices.setAdapter(adapter);
    }

    private void initBleManager() {
        if(!BleManager.supportBle(this)) {
            return;
        }
        BleManager.toggleBluetooth(true);

        BleManager.ScanOptions scanOptions = BleManager.ScanOptions
                .newInstance()
                .scanPeriod(5000)
                .scanDeviceName(null);

        BleManager.ConnectOptions connectOptions = BleManager.ConnectOptions
                .newInstance()
                .connectTimeout(12000);

        manager = BleManager
                .getInstance()
                .setScanOptions(scanOptions)
                .setConnectionOptions(connectOptions)
                .setLog(true,"oam")
                .init(this.getApplication());
    }

    private boolean hasPermissions(Context context,String[] permissions){
        for(String permission:permissions){
            if(ActivityCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startScan() {
        manager.startScan(new BleScanCallback() {
            @Override
            public void onLeScan(BleDevice device, int rssi, byte[] scanRecord) {
                for (BleDevice d : deviceList) {
                    if (device.address.equals(d.address)) {
                        return;
                    }
                }
                deviceList.add(device);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onStart(boolean startScanSuccess, String info) {
                Log.e(TAG, "start scan = " + startScanSuccess + "   info: " + info);
                if (startScanSuccess) {
                    Log.d("oam", "start");
                }
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "scan finish");
                Log.d("oam", "start");
                //bleDatabase.saveDevice(deviceList);
            }
        });
    }

    private boolean isGpsOn() {
        LocationManager locationManager
                = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
            DeviceActivity.this.mDevice = device;
            connectedDevices.add(mDevice);
            bleDatabase.saveDevice(connectedDevices);
            Toast.makeText(DeviceActivity.this, "Kết nối thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeviceActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onDisconnected(String info, int status, BleDevice device) {
            BleManager.getInstance().write(device, UUID_SERVICE_BUTTON_OPTIONS, UUID_Characteristic_BUTTON_OPTIONS,
                    ByteUtils.hexStr2Bytes(HEX_WRITE_ONOFF), writeCallBack);
            connectedDevices.clear();
            bleDatabase.saveDevice(connectedDevices);
            Log.d("oam", "disconnected");
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("connect fail : " + info);
            Toast.makeText(DeviceActivity.this, "Kết nối thất bại", Toast.LENGTH_SHORT).show();
        }
    };
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