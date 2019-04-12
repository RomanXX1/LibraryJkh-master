package com.patternjkh.data;

public class CounterMytishi {
    public String ident, units, name, uniqueName, factoryNum, periodDate, value, isSent, nameExtended, values, sendError;
    public int typeId;

    public CounterMytishi(String ident, String units, String name, String uniqueName, int typeId, String factoryNum, String periodDate, String value, String isSent, String nameExtended, String values, String sendError) {
        this.ident = ident;
        this.units = units;
        this.name = name;
        this.uniqueName = uniqueName;
        this.typeId = typeId;
        this.factoryNum = factoryNum;
        this.periodDate = periodDate;
        this.value = value;
        this.isSent = isSent;
        this.nameExtended = nameExtended;
        this.values = values;
        this.sendError = sendError;
    }

    public CounterMytishi(String ident, String units, String name, String uniqueName, int typeId, String factoryNum, String periodDate, String value, String isSent, String nameExtended, String sendError) {
        this.ident = ident;
        this.units = units;
        this.name = name;
        this.uniqueName = uniqueName;
        this.typeId = typeId;
        this.factoryNum = factoryNum;
        this.periodDate = periodDate;
        this.value = value;
        this.isSent = isSent;
        this.nameExtended = nameExtended;
        this.sendError = sendError;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getFactoryNum() {
        return factoryNum;
    }

    public void setFactoryNum(String factoryNum) {
        this.factoryNum = factoryNum;
    }

    public String getPeriodDate() {
        return periodDate;
    }

    public void setPeriodDate(String periodDate) {
        this.periodDate = periodDate;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getIsSent() {
        return isSent;
    }

    public void setIsSent(String isSent) {
        this.isSent = isSent;
    }

    public String getNameExtended() {
        return nameExtended;
    }

    public void setNameExtended(String nameExtended) {
        this.nameExtended = nameExtended;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getSendError() {
        return sendError;
    }

    public void setSendError(String sendError) {
        this.sendError = sendError;
    }
}
