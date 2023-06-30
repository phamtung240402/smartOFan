package com.example.ofan.Fragment;

import android.app.ActionBar;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
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
    public static final String SERVICE_BUTTON_OFF = "EE01";
    public static final String UUID_SERVICE_BUTTON_OFF = String.valueOf(ByteUtils.parseUUID(SERVICE_BUTTON_OFF));
    public static final String CHARACTERISTIC_BUTTON_OFF = "2222";
    public static final String UUID_CHARACTERISTIC_BUTTON_OFF = String.valueOf(ByteUtils.parseUUID(CHARACTERISTIC_BUTTON_OFF));
    public static final String SERVICE_SEEKBAR_MANUAL = "180A";
    public static final String UUID_SERVICE_SEEKBAR_MANUAL = String.valueOf(ByteUtils.parseUUID(SERVICE_SEEKBAR_MANUAL));
    public static final String CHARATERISTIC_SEEKBAR_MANUAL = "2A14";
    public static final String UUID_CHARACTERISTIC_SEEKBAR_MANUAL = String.valueOf(ByteUtils.parseUUID(CHARATERISTIC_SEEKBAR_MANUAL));
    public static final String SERVICE_SEEKBAR_AUTO = "180C";
    public static final String UUID_SERVICE_SEEKBAR_AUTO = String.valueOf(ByteUtils.parseUUID(SERVICE_SEEKBAR_AUTO));
    public static final String CHARATERISTIC_SEEKBAR_AUTO = "2A16";
    public static final String UUID_CHARACTERISTIC_SEEKBAR_AUTO = String.valueOf(ByteUtils.parseUUID(CHARATERISTIC_SEEKBAR_AUTO));
    public static final String HEX_WRITE_ONOFF = "00";
    private TextView txtShowSpeed;
    private TextView txtMode;
    private View view;
    private BleDatabase bleDatabase;
    private ArrayList<BleDevice> devices;
    private BleDevice mDevice;
    private AppCompatButton btnOff, btnManual, btnAuto;
    private SeekBar seekbar;
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
        txtMode = view.findViewById(R.id.txtMode);
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtMode.setText("");
                seekbar.setProgress(0);
                if (checkConnect()) {
                    BleManager.getInstance().write(mDevice, UUID_SERVICE_BUTTON_OFF, UUID_CHARACTERISTIC_BUTTON_OFF,
                            ByteUtils.hexStr2Bytes(HEX_WRITE_ONOFF), writeCallBack);
                    btnManual.setText("Bật");
                } else {
                    Log.d("oam-off", String.valueOf(ByteUtils.hexStr2Bytes(HEX_WRITE_ONOFF)));
                }
            }
        });

        btnManual.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             count_manual_mode++;
                                             btnManual.setText("Tốc độ");
                                             txtMode.setText("Thường");
                                             do {
                                                 if (count_manual_mode == 1) {
                                                     seekbar.setProgress(35);
                                                 } else if (count_manual_mode == 2) {
                                                     seekbar.setProgress(70);
                                                 } else {
                                                     seekbar.setProgress(99);
                                                 }
                                                 if(count_manual_mode > 3) count_manual_mode = 0;
                                             } while (count_manual_mode < 3);
                                         }
                                     }
            );

        btnAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtShowSpeed.setText(String.format("%02d", seekbar.getProgress()));
                txtMode.setText("Gió tự nhiên");
                if (checkConnect()) {
                    if(seekbar.getProgress() == 0) {
                        seekbar.setProgress(70);
                    } else {
                        BleManager.getInstance().write(mDevice, UUID_SERVICE_SEEKBAR_AUTO, UUID_CHARACTERISTIC_SEEKBAR_AUTO,
                                ByteUtils.hexStr2Bytes(String.valueOf(seekbar.getProgress())), writeCallBack);
                    }
                } else {
                    Log.d("oam-auto", "failed to send");
                }
            }
        });
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtShowSpeed.setText(String.format("%02d", progress));
                if (checkConnect()) {
                    if(txtMode.getText().toString().contentEquals("Thường")) {
                        BleManager.getInstance().write(mDevice, UUID_SERVICE_SEEKBAR_MANUAL, UUID_CHARACTERISTIC_SEEKBAR_MANUAL,
                                ByteUtils.hexStr2Bytes(String.format("%02d", progress)), writeCallBack);
                    } else if(txtMode.getText().toString().contentEquals("Gió tự nhiên")) {
                        BleManager.getInstance().write(mDevice, UUID_SERVICE_SEEKBAR_AUTO, UUID_CHARACTERISTIC_SEEKBAR_AUTO,
                                ByteUtils.hexStr2Bytes(String.format("%02d", progress)), writeCallBack);
                    } else {
                        txtMode.setText("Thường");
                        BleManager.getInstance().write(mDevice, UUID_SERVICE_SEEKBAR_MANUAL, UUID_CHARACTERISTIC_SEEKBAR_MANUAL,
                                ByteUtils.hexStr2Bytes(String.format("%02d", progress)), writeCallBack);
                    }
                } else {
                    Log.d("oam", "seekbar");
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
            Logger.e("oam", "00");

        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("write fail:" + info);
        }
    };


}