package com.patternjkh.data;

public class HouseNumber {

    String number;
    String id;

    public HouseNumber(String number, String id) {
        super();
        this.number = number;
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return number;
    }
}
