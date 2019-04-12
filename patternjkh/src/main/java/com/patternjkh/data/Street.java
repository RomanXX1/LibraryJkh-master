package com.patternjkh.data;

public class Street {

    String name;

    public Street(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
