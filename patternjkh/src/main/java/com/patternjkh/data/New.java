package com.patternjkh.data;

public class New {

    public String id, date, name, text;
    public boolean isReaded;

    public New(String id, String date, String name, String text, boolean isReaded) {
        this.id   = id;
        this.date = date;
        this.name = name;
        this.text = text;
        this.isReaded = isReaded;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isReaded() {
        return isReaded;
    }

    public void setReaded(boolean readed) {
        isReaded = readed;
    }
}
