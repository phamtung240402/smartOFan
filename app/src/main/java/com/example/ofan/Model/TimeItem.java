package com.example.ofan.Model;

public class TimeItem {
    private int hour;
    private int minute;
    private boolean checked;
    public TimeItem(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        checked = false;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}
