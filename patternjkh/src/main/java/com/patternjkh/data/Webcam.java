package com.patternjkh.data;

public class Webcam {

    private String address, url;

    public Webcam(String address, String url) {
        this.address = address;
        this.url = url;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
