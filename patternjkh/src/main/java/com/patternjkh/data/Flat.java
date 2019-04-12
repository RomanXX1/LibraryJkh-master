package com.patternjkh.data;

public class Flat {

    String name;
    String id;
    String name_sort;

    public Flat(String name, String id, String name_sort) {
        super();
        this.name = name;
        this.id = id;
        this.name_sort = name_sort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName_sort() {
        return name_sort;
    }

    public void setName_sort(String name_sort) {
        this.name_sort = name_sort;
    }

    @Override
    public String toString() {
        return name;
    }
}
