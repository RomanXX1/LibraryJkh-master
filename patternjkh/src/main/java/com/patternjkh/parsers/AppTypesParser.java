package com.patternjkh.parsers;

import com.patternjkh.DB;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AppTypesParser extends DefaultHandler {

    private String id = "", type = "";
    private DB db;

    public AppTypesParser(DB db) {
        this.db = db;
        this.db.open();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (localName.toLowerCase().equals("row")) {
            id = attributes.getValue("id").toString();
            type = attributes.getValue("name");
            db.add_type_app(Integer.valueOf(id), type);
        }
    }
}
