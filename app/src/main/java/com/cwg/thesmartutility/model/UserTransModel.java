package com.cwg.thesmartutility.model;

public class UserTransModel {

    String transID, meterID, amount, tariff, token,chargePer, chargeAmount, vendedAmount, units, vendedBy, email, date, time ;
    int estateID;

    // constructor
    public UserTransModel(String transID, String meterID, String amount, String tariff, String token, String chargePer, String chargeAmount, String vendedAmount, String units, String vendedBy, String email, String date, String time, int estateID) {
        this.transID = transID;
        this.meterID = meterID;
        this.amount = amount;
        this.tariff = tariff;
        this.token = token;
        this.chargePer = chargePer;
        this.chargeAmount = chargeAmount;
        this.vendedAmount = vendedAmount;
        this.units = units;
        this.vendedBy = vendedBy;
        this.email = email;
        this.date = date;
        this.time = time;
        this.estateID = estateID;
    }

    public String getTransID() {
        return transID;
    }

    public void setTransID(String transID) {
        this.transID = transID;
    }

    public String getMeterID() {
        return meterID;
    }

    public void setMeterID(String meterID) {
        this.meterID = meterID;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getChargePer() {
        return chargePer;
    }

    public void setChargePer(String chargePer) {
        this.chargePer = chargePer;
    }

    public String getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(String chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public String getVendedAmount() {
        return vendedAmount;
    }

    public void setVendedAmount(String vendedAmount) {
        this.vendedAmount = vendedAmount;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getVendedBy() {
        return vendedBy;
    }

    public void setVendedBy(String vendedBy) {
        this.vendedBy = vendedBy;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getEstateID() {
        return estateID;
    }

    public void setEstateID(int estateID) {
        this.estateID = estateID;
    }
}
