package com.patternjkh.data;

public class Saldo {

    public int num_month;
    public int year;
    public String usluga, start, plus, minus, end, id;

    public Saldo(int num_month, int year, String name, String start, String plus, String minus, String end, String id) {

        this.num_month = num_month;
        this.year      = year;
        this.usluga    = name;
        this.start     = start;
        this.plus      = plus;
        this.minus     = minus;
        this.end       = end;
        this.id        = id;

    }

    public int getNum_month() {
        return num_month;
    }

    public int getYear() {
        return year;
    }

}
