package com.hcmhmt.todoapp.Classes;

import androidx.constraintlayout.widget.ConstraintLayout;

public class Todo {

    private String title;
    private String date;
    private String time;
    private String status;
    private String category;
    private String tag;
    private String color;
    private String timestamp;
    private int alarmId;
    private Boolean expanded;

    public Todo() {
    }

    public Todo(String title, String date, String time, String status, String category, String tag, String color, String timestamp,int alarmId) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.status = status;
        this.category = category;
        this.tag = tag;
        this.color = color;
        this.timestamp = timestamp;
        this.timestamp = timestamp;
        this.expanded = false;
        this.alarmId = alarmId;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }
}
