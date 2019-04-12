package com.patternjkh.data;

import com.patternjkh.utils.StringUtils;

public class Application {

    public int close, isRead, isReadCons, isAnswered;
    public String client, id_client, tema, date, adress, PhoneNumber, type_app, number, text, owner;
    public boolean isUpdated;

    public Application(String _number, String _text, String _owner, int _close, int _isRead,
                       int _isAnswered, String _client, String _id_client, String _tema, String _date,
                       String adress, String _phone, Boolean isUpdated, String _type_app, int isReadCons) {

        number = _number;
        text = _text;
        owner = _owner;
        close = _close;
        isRead = _isRead;
        isAnswered = _isAnswered;
        client = _client;
        id_client = _id_client;
        this.isUpdated = isUpdated;

        // Добавленные поля
        tema = _tema;
        date = _date;

        this.adress = adress;

        PhoneNumber = _phone;

        type_app = _type_app;
        this.isReadCons = isReadCons;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getNumberInt() {
        return StringUtils.convertStringToInteger(number);
    }

}
