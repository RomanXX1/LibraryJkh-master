package com.patternjkh.parsers;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.patternjkh.DB;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CountersParser extends DefaultHandler {

    private String login = "", ident="", units="", name="", uniqueNum="", factoryNum="";
    private String periodDate="", value="", isSent="", sendError="";
    private int typeId;
    private DB db;

    public CountersParser(@NonNull DB db, @NonNull String login) {
        this.db = db;
        this.db.open();
        this.login = login;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.toLowerCase().equals("meter")) {
            ident = atts.getValue("Ident");
            units = atts.getValue("Units");
            name = atts.getValue("Name");
            uniqueNum = atts.getValue("MeterUniqueNum");
            typeId = Integer.parseInt(atts.getValue("MeterTypeID"));
            factoryNum = atts.getValue("FactoryNumber");

        } else if (localName.toLowerCase().equals("metervalue")) {
            periodDate = atts.getValue("PeriodDate");
            value = atts.getValue("Value");
            isSent = atts.getValue("IsSended");
            sendError = atts.getValue("SendError");

            db.addCountMytishi(login, ident, units, name, uniqueNum, typeId, factoryNum, periodDate, value, isSent, sendError);
        }
    }
}
