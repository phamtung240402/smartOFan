package com.example.ofan.Fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ofan.Database.BleDatabase;
import com.example.ofan.R;
import com.example.ofan.utils.ByteUtils;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.callback.BleWriteCallback;

import java.util.ArrayList;


public class HomeFragment extends Fragment {
    public static final String UUID_SERVICE_BUTTON_OPTIONS = "000000ee-0000-1000-8000-00805f9b34fb";
    public static final String UUID_Characteristic_BUTTON_OPTIONS = "0000ee01-0000-1000-8000-00805f9b34fb";
    public static final String UUID_SERVICE_TEMPERATURE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARACTERISTIC_TEMPERARTURE = "00002a19-0000-1000-8000-00805f9b34fb";
    public static final String UUID_SERVICE_SEEKBAR = "0000180c-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARACTERISTIC_SEEKBAR = "00002a16-0000-1000-8000-00805f9b34fb";
    public static final String HEX_WRITE_AUTO = "00";
    public static final String HEX_WRITE_ONOFF = "00";

    private TextView txtShowSpeed;
    private Animation rotation;
    private View view;
    private BleDatabase bleDatabase;
    private ArrayList<BleDevice> devices;
    private BleDevice mDevice;
    private AppCompatButton btnOff, btnManual, btnAuto;
    private SeekBar seekbar;
    private LinearLayout ln_seekbar;
    private ImageView imgRotation;
    private int count_manual_mode = 1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        initView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView() {
        bleDatabase = new BleDatabase(getContext());
        devices = bleDatabase.getBleList();

        txtShowSpeed = view.findViewById(R.id.txtShowSpeed);
        btnOff = view.findViewById(R.id.btnOff);
        btnManual = view.findViewById(R.id.btnManual);
        btnAuto = view.findViewById(R.id.btnAuto);
        seekbar = view.findViewById(R.id.seekbar);
        imgRotation= view.findViewById(R.id.imgRotation);
        ln_seekbar = view.findViewById(R.id.ln_seekbar);

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekbar.setProgress(0);
                if (checkConnect()) {
                    BleManager.getInstance().write(mDevice, UUID_SERVICE_BUTTON_OPTIONS, UUID_Characteristic_BUTTON_OPTIONS,
                            ByteUtils.hexStr2Bytes(HEX_WRITE_ONOFF), writeCallBack);
                } else {
                    Log.d("oam-off", String.valueOf(ByteUtils.hexStr2Bytes(HEX_WRITE_ONOFF)));
                }
            }
        });

        btnManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                do {
                    count_manual_mode++;
                    if (count_manual_mode == 1) {
                        seekbar.setProgress(35);
                        break;
                    } else if (count_manual_mode == 2) {
                        seekbar.setProgress(70);
                        break;
                    } else {
                        seekbar.setProgress(100);
                        break;
                    }
                }
                while (count_manual_mode <= 4);
                    if (count_manual_mode == 3) count_manual_mode = 0;
            } catch (Exception ex) {
                    Log.d("oam-manual", ex.getMessage());
                }
            }
        });

        btnAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkConnect()) {
                    BleManager.getInstance().write(mDevice, UUID_SERVICE_TEMPERATURE, UUID_CHARACTERISTIC_TEMPERARTURE, ByteUtils.hexStr2Bytes(HEX_WRITE_AUTO) ,writeCallBack);
                    BleManager.getInstance().write(mDevice, UUID_SERVICE_SEEKBAR, UUID_CHARACTERISTIC_SEEKBAR,
                            ByteUtils.hexStr2Bytes(String.format("%02d", seekbar.getProgress())), writeCallBack);
                    txtShowSpeed.setText(String.format("%02d", seekbar.getProgress()));
                } else {
                    Log.d("oam-auto", String.valueOf(ByteUtils.hexStr2Bytes(HEX_WRITE_AUTO)));
                }
            }
        });
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(checkConnect()) {
                        BleManager.getInstance().write(mDevice, UUID_SERVICE_SEEKBAR, UUID_CHARACTERISTIC_SEEKBAR,
                                ByteUtils.hexStr2Bytes(String.format("%02d", progress)), writeCallBack);
                    txtShowSpeed.setText(String.format("%02d", progress));
                }
                else {
                    Log.d("oam-seekbar", String.valueOf(ByteUtils.hexStr2Bytes(String.format("%02d", progress))));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(checkConnect()) {
                    if(seekBar.getProgress() < 20) {
                        openWarningDialog(Gravity.CENTER, seekBar);
                    }
                }
            }
        });

        if(devices.size() != 0) {
            mDevice = devices.get(0);
        }
        else {
            txtShowSpeed.setText("00");
            mDevice = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void openWarningDialog(int gravity, SeekBar seekBar) {
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

        btnDialogNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(20);
                dialog.dismiss();
            }
        });

        btnDialogYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private boolean checkConnect() {
        if(mDevice == null) {
            return false;
        }
        return true;
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