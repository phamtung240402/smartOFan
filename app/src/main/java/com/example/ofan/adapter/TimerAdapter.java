package com.example.ofan.adapter;

import static android.os.Build.VERSION_CODES.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ofan.Interface.OnClickListener;
import com.example.ofan.Interface.OnLongClickListener;
import com.example.ofan.Model.TimeItem;
import com.example.ofan.R;

import java.util.ArrayList;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.TimerViewHolder> {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<TimeItem> timeItems;
    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLongClickListener;

    public TimerAdapter(Context context, ArrayList<TimeItem> timeItems, OnClickListener mOnClickListener, OnLongClickListener mOnLongClickListener) {
        this.context = context;
        this.timeItems = timeItems;
        this.mOnClickListener = mOnClickListener;
        this.mOnLongClickListener = mOnLongClickListener;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public TimerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(com.example.ofan.R.layout.timer_item, parent, false);
        return new TimerViewHolder(view, mOnClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TimerViewHolder holder, int position) {
        TimeItem timeItem = timeItems.get(position);
        holder.txtHour.setText(String.format("%02d",timeItem.getHour()));
        holder.txtMinute.setText(String.format("%02d",timeItem.getMinute()));
        if(timeItem.getHour() >= 0 && timeItem.getHour() < 12) {
            holder.txtAMorPM.setText("AM");
        } else {
            holder.txtAMorPM.setText("PM");
        }
        holder.btnAddAlarm.setChecked(timeItem.isChecked());
        holder.btnAddAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mOnClickListener.onChecked(position, isChecked);
            }
        });

    }

    @Override
    public int getItemCount() {
        if(timeItems.size() != 0) return timeItems.size();
        return 0;
    }

    public class TimerViewHolder extends RecyclerView.ViewHolder{
        private TextView txtHour, txtMinute, txtAMorPM;
        private SwitchCompat btnAddAlarm;

        public TimerViewHolder(@NonNull View itemView, OnClickListener onClickListener) {
            super(itemView);
            txtHour = itemView.findViewById(com.example.ofan.R.id.txtHour);
            txtMinute = itemView.findViewById(com.example.ofan.R.id.txtMinute);
            txtAMorPM = itemView.findViewById(com.example.ofan.R.id.txtAMorPM);
            btnAddAlarm = itemView.findViewById(com.example.ofan.R.id.btnAddAlarm);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnLongClickListener.onLongClick(getAdapterPosition());
                    return true;
                }
            });
        }
    }
}
