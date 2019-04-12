package com.patternjkh.data;

public class Counter {

    public int num_month;
    public int year;
    public String name, ed_izm, uniq_num, prev, value, diff, ident, isSent;
    private int mTypeId;
    private String mCounterNameExtended;
    public String serialNumber;

    public Counter(int num_month, int year, String name, String ed_izm, String uniq_num, String prev, String value, String diff, int typeId, String ident, String serialNumber, String isSent) {
        this.num_month = num_month;
        this.year      = year;
        this.name      = name;
        this.ed_izm    = ed_izm;
        this.uniq_num  = uniq_num;
        this.prev      = prev;
        this.value     = value;
        this.diff      = diff;
        mTypeId = typeId;
        this.ident     = ident;
        this.serialNumber = serialNumber;
        this.isSent = isSent;
    }

    public int getNum_month() {
        return num_month;
    }

    public int getYear() {
        return year;
    }

    public void setTypeId(int typeId) {
        mTypeId = typeId;
    }

    public int getTypeId() {
        return mTypeId;
    }

    public String getCounterNameExtended() {
        return mCounterNameExtended;
    }

    public void setCounterNameExtended(String counterNameExtended) {
        mCounterNameExtended = counterNameExtended;
    }
}
