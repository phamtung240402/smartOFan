package com.example.ofan.Fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ofan.Activity.DeviceActivity;
import com.example.ofan.Database.BleDatabase;
import com.example.ofan.R;
import com.example.ofan.adapter.CommonRecyclerViewAdapter;
import com.example.ofan.adapter.ScanDeviceAdapter;
import com.example.ofan.utils.ByteUtils;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.callback.BleWriteCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class DeviceFragment extends Fragment {
    public static final String SERVICE_BUTTON_OPTIONS = "00EE";
    public static final String UUID_SERVICE_BUTTON_OPTIONS = String.valueOf(ByteUtils.parseUUID(SERVICE_BUTTON_OPTIONS));
    public static final String CHARACTERISTIC_BUTTON_OPTIONS = "EE01";
    public static final String UUID_Characteristic_BUTTON_OPTIONS = String.valueOf(ByteUtils.parseUUID(CHARACTERISTIC_BUTTON_OPTIONS));
    public static final String HEX_WRITE_ONOFF = "00";
    private View view;
    private RecyclerView rv_connectedDevices;
    private BleDatabase bleDatabase;
    private ArrayList<BleDevice> devices;
    private ScanDeviceAdapter adapter;
    private BleDevice mDevice;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_device, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        showDeviceByRv();
    }

    private void initData() {
        rv_connectedDevices = view.findViewById(R.id.rv_connectedDevice);
        bleDatabase = new BleDatabase(getContext());
        devices = bleDatabase.getBleList();
    }

    private void showDeviceByRv() {
        rv_connectedDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_connectedDevices.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 3;
            }
        });
        SparseArray<int[]> res = new SparseArray<>();
        res.put(R.layout.ble_item,new int[]{R.id.txt_itemName,R.id.txt_itemAddress});
        Log.d("oam-size", String.valueOf(devices.size()));
        if(devices.size() != 0) {
            mDevice = devices.get(0);
        }
        else {
            devices.clear();
            mDevice = null;
        }
        adapter = new ScanDeviceAdapter(getContext(), devices, res);
        rv_connectedDevices.setAdapter(adapter);
        adapter.setOnItemLongClickListener(new CommonRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                openWarningDialog(Gravity.CENTER, position);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(devices.size() != 0) {
            adapter.notifyDataSetChanged();
            mDevice = devices.get(0);
        }
        else {
            devices.clear();
            adapter.notifyDataSetChanged();
            mDevice = null;
            return;
        }
    }

    private void openWarningDialog(int gravity, int position) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_seekbar);
        Window window = dialog.getWindow();
        if(window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttribute = window.getAttributes();
        windowAttribute.gravity = gravity;
        window.setAttributes(windowAttribute);

        if(Gravity.CENTER == gravity) {
            dialog.setCancelable(false);
        }

        AppCompatButton btnDialogNo = dialog.findViewById(R.id.btnDialogNo);
        AppCompatButton btnDialogYes = dialog.findViewById(R.id.btnDialogYes);
        TextView txtAnnouncement = dialog.findViewById(R.id.txtAnnouncement);
        txtAnnouncement.setText("Bạn có muốn xóa không?");
        btnDialogNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnDialogYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleDevice device = devices.get(position);
                BleManager.getInstance().write(mDevice, UUID_SERVICE_BUTTON_OPTIONS, UUID_Characteristic_BUTTON_OPTIONS,
                        ByteUtils.hexStr2Bytes(HEX_WRITE_ONOFF), writeCallBack);
                BleManager.getInstance().disconnect(device.address);
                Toast.makeText(getContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                devices.clear();
                adapter.notifyDataSetChanged();
                bleDatabase.saveDevice(devices);
                mDevice = null;
                dialog.dismiss();
            }
        });
        dialog.show();
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