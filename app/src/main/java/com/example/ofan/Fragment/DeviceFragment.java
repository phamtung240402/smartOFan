package com.example.ofan.Fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class DeviceFragment extends Fragment {
    private View view;
    private RecyclerView rv_connectedDevices;
    private ImageButton btnAddDevices;
    private BleDatabase bleDatabase;
    private ArrayList<BleDevice> devices;
    private ScanDeviceAdapter adapter;
    private BleDevice mDevice;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_device, container, false);
        initData();
        showDeviceByRv();
        return view;
    }


    private void initData() {
        rv_connectedDevices = view.findViewById(R.id.rv_connectedDevice);
        btnAddDevices = view.findViewById(R.id.btnAddDevice);
        bleDatabase = new BleDatabase(getContext());
        devices = bleDatabase.getBleList();

        btnAddDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DeviceActivity.class);
                startActivity(intent);
            }
        });
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
        adapter = new ScanDeviceAdapter(getContext(), devices, res);
        rv_connectedDevices.setAdapter(adapter);
        adapter.setOnItemLongClickListener(new CommonRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                openWarningDialog(Gravity.CENTER, position);
            }
        });

        if(devices.size() != 0) {
            adapter.notifyDataSetChanged();
            mDevice = devices.get(0);
        }
        else {
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
}