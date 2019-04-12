package com.patternjkh.data;

public class CounterHistoryItem {
    public String period, value, isSent, sendError;

    public CounterHistoryItem(String period, String value, String isSent, String sendError) {
        this.period = period;
        this.value = value;
        this.isSent = isSent;
        this.sendError = sendError;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getIsSent() {
        return isSent;
    }

    public void setIsSent(String isSent) {
        this.isSent = isSent;
    }

    public String getSendError() {
        return sendError;
    }

    public void setSendError(String sendError) {
        this.sendError = sendError;
    }
}
