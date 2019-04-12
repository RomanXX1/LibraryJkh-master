package com.patternjkh.ui.additionalServices;

public class AdditionalService {

    private String id, name, address, descr, logo, phone, type;

    public AdditionalService(String id, String name, String address, String descr, String logo, String phone, String type) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.descr = descr;
        this.logo = logo;
        this.phone = phone;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
