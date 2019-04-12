package com.patternjkh.data;

public class PaysHistoryItem {

    private String date, status, pay;

    public PaysHistoryItem(String date, String pay, String status) {
        this.date = date;
        this.pay = pay;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }
}