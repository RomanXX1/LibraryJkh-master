package com.patternjkh.data;

public class OsvHistoryItem {

    private String date, period, pay;

    public OsvHistoryItem(String date, String period, String pay) {
        this.date = date;
        this.period = period;
        this.pay = pay;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }
}
