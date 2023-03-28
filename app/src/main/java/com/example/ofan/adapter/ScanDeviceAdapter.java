package com.example.ofan.adapter;

import android.content.Context;
import android.os.Handler;
import android.util.SparseArray;
import android.widget.TextView;

import com.example.ofan.R;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.callback.BleRssiCallback;

import java.util.ArrayList;
import java.util.logging.LogRecord;

public class ScanDeviceAdapter extends CommonRecyclerViewAdapter<BleDevice> {
    private TextView tvName;
    private TextView tvAddress;
    public ScanDeviceAdapter(Context mContext, ArrayList<BleDevice> mDataList, SparseArray<int[]> mResLayoutAndViewIds) {
        super(mContext, mDataList, mResLayoutAndViewIds);
    }

    @Override
    protected void bindDataToItem(CommonRecyclerViewAdapter.MyViewholder mHolder, BleDevice bleDevice, int position) {
        tvName = (TextView) mHolder.mViews.get(R.id.txt_itemName);
        tvAddress = (TextView) mHolder.mViews.get(R.id.txt_itemAddress);
        tvName.setText(bleDevice.name);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().readRssi(bleDevice, rssiCallback);
                handler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    @Override
    public int getItemResLayoutType(int position) {
        return R.layout.ble_item;
    }
    private BleRssiCallback rssiCallback = new BleRssiCallback() {
        @Override
        public void onRssi(int rssi, BleDevice bleDevice) {
            Logger.e("read rssi success:" + rssi);
            tvAddress.setText(String.valueOf(rssi));
        }
        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("read rssi fail:" + info);
        }
    };
}
