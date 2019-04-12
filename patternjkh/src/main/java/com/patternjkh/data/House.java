package com.patternjkh.data;

public class House {

    String name;
    String fias;

    public House(String name, String fias) {
        super();
        this.name = name;
        this.fias = fias;
    }

    public String getName() {
        return name;
    }

    public String getFias() {
        return fias;
    }

    @Override
    public String toString() {
        return name;
    }
}
