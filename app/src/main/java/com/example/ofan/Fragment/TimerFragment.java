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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;

public class TimerFragment extends Fragment {
    private String UUID_TIMER_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    private String UUID_TIMER_CHARACTERISTICS = "00002a14-0000-1000-8000-00805f9b34fb";
    private View mView;
    private ImageButton btnAddTimer;
    private TimeItem timeItem;
    private int hour, minutes;
    private ArrayList<TimeItem> timeItems = new ArrayList<>();
    private RecyclerView rv_timer;
    private TimerAdapter timerAdapter;
    private BleDatabase bleDatabase;
    private ArrayList<BleDevice> devices;
    private BleDevice mDevice;
    private TimerDatabase timerDatabase;
    private TimeItem selectedTime;
    private Calendar c;
    private int timeLeft, hourLeft, minuteLeft;
    private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
        @Override
        public void onLongClick(int position) {
            openWarningDialog(Gravity.CENTER, position);
        }
    };
    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onChecked(int position, boolean checked) {
//            if(mDevice == null) {
//                return;
//            }
            c = Calendar.getInstance();
            selectedTime = timeItems.get(position);
            selectedTime.setChecked(checked);
            for (int i = 0; i < timeItems.size(); i++) {
                if (selectedTime.isChecked() && i != position) {
                    timeItems.get(i).setChecked(false);
                    timerAdapter.notifyItemChanged(i);
                }
            }
            timerDatabase.saveTimer(timeItems);
            if (checked) {
                if (c.get(Calendar.HOUR_OF_DAY) < selectedTime.getHour()) {
                    timeLeft = ((selectedTime.getHour() - c.get(Calendar.HOUR_OF_DAY)) * 60 + selectedTime.getMinute() - c.get(Calendar.MINUTE));
                } else if (c.get(Calendar.HOUR_OF_DAY) > selectedTime.getHour()) {
                    timeLeft = ((selectedTime.getHour() + 24 - c.get(Calendar.HOUR_OF_DAY)) * 60 + selectedTime.getMinute() - c.get(Calendar.MINUTE));
                } else {
                    if (selectedTime.getMinute() > c.get(Calendar.MINUTE)) {
                        timeLeft = (selectedTime.getMinute() - c.get(Calendar.MINUTE));
                    } else {
                        timeLeft = ((selectedTime.getHour() + 24 - c.get(Calendar.HOUR_OF_DAY)) * 60 + selectedTime.getMinute() - c.get(Calendar.MINUTE));
                    }
                }
                hourLeft = timeLeft / 60;
                minuteLeft = timeLeft % 60;
//                BleManager.getInstance().write(mDevice, UUID_TIMER_SERVICE, UUID_TIMER_CHARACTERISTICS, ByteUtils.hexStr2Bytes(String.valueOf(timeLeft)) ,writeCallBack);
                Log.d("oam-time", String.valueOf(hourLeft) + String.valueOf(minuteLeft));
            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_timer, container, false);
        initView();
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView() {
        timerDatabase = new TimerDatabase(getContext());
        timeItems = timerDatabase.getTimerList();
        timerAdapter = new TimerAdapter(getContext(), timeItems, mOnClickListener, mOnLongClickListener);

        btnAddTimer = mView.findViewById(R.id.btnAddTimer);
        rv_timer = mView.findViewById(R.id.rv_timer);
        rv_timer.setAdapter(timerAdapter);
        rv_timer.setLayoutManager(new LinearLayoutManager(getContext()));
        if(timeItems.size() != 0) {
            timerAdapter.notifyDataSetChanged();
        }
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                timeItems.remove(position);
                saveTimeList();
            }
        });

        itemTouchHelper.attachToRecyclerView(rv_timer);

        btnAddTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        hour = hourOfDay;
                        minutes = minute;
                        timeItem = new TimeItem(hour, minutes);
                        timeItems.add(timeItem);
                        saveTimeList();
                    }
                };

                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), onTimeSetListener, hour, minutes, true);
                timePickerDialog.show();
            }
        });
        bleDatabase = new BleDatabase(getContext());
        devices = bleDatabase.getBleList();
        if(devices.size() != 0) {
            mDevice = devices.get(0);
        }
        else {
            mDevice = null;
            for(int i = 0; i < timeItems.size(); i++) {
                if(timeItems.get(i).isChecked())
                {
                    timeItems.get(i).setChecked(false);
                }
            }
            saveTimeList();
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

    private void saveTimeList() {
        timerDatabase.saveTimer(timeItems);
        timerAdapter.notifyDataSetChanged();
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
                timeItems.remove(position);
                timerAdapter.notifyDataSetChanged();
                timerDatabase.saveTimer(timeItems);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}