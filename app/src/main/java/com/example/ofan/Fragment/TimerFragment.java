package com.example.ofan.Fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.ofan.Database.BleDatabase;
import com.example.ofan.Database.TimerDatabase;
import com.example.ofan.Interface.OnClickListener;
import com.example.ofan.Interface.OnLongClickListener;
import com.example.ofan.Model.TimeItem;
import com.example.ofan.R;
import com.example.ofan.adapter.TimerAdapter;
import com.example.ofan.utils.ByteUtils;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.callback.BleWriteCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;

public class TimerFragment extends Fragment {
    public static final String SERVICE_HOUR = "180F";
    public static final String UUID_SERVICE_HOUR = String.valueOf(ByteUtils.parseUUID(SERVICE_HOUR));
    public static final String CHARACTERISTIC_HOUR = "2A19";
    public static final String UUID_Characteristic_HOUR = String.valueOf(ByteUtils.parseUUID(CHARACTERISTIC_HOUR));
    private View mView;
    private BleDatabase bleDatabase;
    private ArrayList<BleDevice> devices;
    private BleDevice mDevice;
    private AppCompatButton btnTao;
    private int selectedHour = 0;
    private int selectedMinute = 0;
    private long timeLeft = 0;
    private int hourLeft = 0;
    private int minuteLeft = 0;
    private Calendar c;

    private TextView txtCurrentHour, txtCurrentMinute, txtCurrentSecond;

    private boolean mTimeRunning;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_timer, container, false);
        c = Calendar.getInstance();
        initView();
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView() {
        btnTao = mView.findViewById(R.id.btnTao);
        txtCurrentHour = mView.findViewById(R.id.txtHour);
        txtCurrentMinute = mView.findViewById(R.id.txtMinute);
        txtCurrentSecond = mView.findViewById(R.id.txtSecond);
        bleDatabase = new BleDatabase(getContext());
        devices = bleDatabase.getBleList();
        btnTao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnTao.getText().toString().trim().contentEquals("Tạo")) {
                    setTimer();
                } else {
                    countDownTimer.cancel();
                    timeLeft = 0;
                    setCountDownText();
                    btnTao.setText("Tạo");
                }
            }
        });
        if (devices.size() != 0) {
            mDevice = devices.get(0);
        } else {
            mDevice = null;
            return;
        }
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

    private void setTimer() {
        View viewDialog = getLayoutInflater().inflate(R.layout.layout_set_timer, null);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(viewDialog);
        bottomSheetDialog.show();
        bottomSheetDialog.setCancelable(false);

        ImageButton btnBack = viewDialog.findViewById(R.id.btnBack);
        TextView txtHour = viewDialog.findViewById(R.id.txtHour);
        TextView txtMinute = viewDialog.findViewById(R.id.txtMinute);
        SeekBar seekbarHour = viewDialog.findViewById(R.id.seekbarHour);
        SeekBar seekbarMinute = viewDialog.findViewById(R.id.seekbarMinute);
        AppCompatButton btnHuy = viewDialog.findViewById(R.id.btnHuy);
        AppCompatButton btnTao = viewDialog.findViewById(R.id.btnTao);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        seekbarHour.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtHour.setText(String.format("%02d", progress));
                selectedHour = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekbarMinute.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtMinute.setText(String.format("%02d", progress));
                selectedMinute = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        btnTao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(c.get(Calendar.HOUR_OF_DAY) < selectedHour) {
                        timeLeft = (selectedHour - c.get(Calendar.HOUR_OF_DAY)) * 60 + selectedMinute - c.get(Calendar.MINUTE);
                    } else if(c.get(Calendar.HOUR_OF_DAY) > selectedHour) {
                        timeLeft = (selectedHour + 24 - c.get(Calendar.HOUR_OF_DAY)) * 60 + selectedMinute - c.get(Calendar.MINUTE);
                    } else {
                        if (selectedMinute > c.get(Calendar.MINUTE)) {
                            timeLeft = (selectedMinute - c.get(Calendar.MINUTE));
                        } else {
                            timeLeft = (selectedHour + 24 - c.get(Calendar.HOUR_OF_DAY)) * 60 + selectedMinute - c.get(Calendar.MINUTE);
                        }
                    }
                    startTimer();
                    bottomSheetDialog.dismiss();
                    if(mDevice != null) {
                        BleManager.getInstance().write(mDevice, UUID_SERVICE_HOUR, UUID_Characteristic_HOUR,
                                ByteUtils.hexStr2Bytes(String.valueOf(hourLeft)), writeCallBack);
                        BleManager.getInstance().write(mDevice, UUID_SERVICE_MINUTE, UUID_Characteristic_MINUTE,
                                ByteUtils.hexStr2Bytes(String.valueOf(minuteLeft)), writeCallBack);

                    }
                }
            });
    }

    private void startTimer() {
        countDownTimer.start();
        mTimeRunning = true;
        btnTao.setText("Hủy");
    }
    CountDownTimer countDownTimer = new CountDownTimer(timeLeft * 60 * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            timeLeft = millisUntilFinished;
            setCountDownText();
        }

        @Override
        public void onFinish() {
            mTimeRunning = false;
            btnTao.setText("Hủy");
        }
    };
    private void setCountDownText() {
        int hours = (int)timeLeft/1000 / 60 / 60;
        int minutes = (int)timeLeft/1000 % 3600 / 60;
        int seconds = (int) timeLeft/1000 % 3600 % 60;
        txtCurrentHour.setText(String.format("%02d", hours));
        txtCurrentMinute.setText(String.format("%02d", minutes));
        txtCurrentSecond.setText(String.format("%02d", seconds));
    }
}