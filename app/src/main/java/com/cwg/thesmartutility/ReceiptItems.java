package com.cwg.thesmartutility;

public class ReceiptItems {
    String rTrans, rMeter, rToken, rAmount, rCharge, rVat, rTariff, rUnits, rVendedAmount, rDate, rTime;

    // constructor

    public ReceiptItems(String rTrans, String rMeter, String rToken, String rAmount, String rCharge, String rVat, String rTariff, String rUnits, String rVendedAmount, String rDate, String rTime) {
        this.rTrans = rTrans;
        this.rMeter = rMeter;
        this.rToken = rToken;
        this.rAmount = rAmount;
        this.rCharge = rCharge;
        this.rVat = rVat;
        this.rTariff = rTariff;
        this.rUnits = rUnits;
        this.rVendedAmount = rVendedAmount;
        this.rDate = rDate;
        this.rTime = rTime;
    }

    // getter and setter

    public String getrTrans() {
        return rTrans;
    }

    public void setrTrans(String rTrans) {
        this.rTrans = rTrans;
    }

    public String getrMeter() {
        return rMeter;
    }

    public void setrMeter(String rMeter) {
        this.rMeter = rMeter;
    }

    public String getrToken() {
        return rToken;
    }

    public void setrToken(String rToken) {
        this.rToken = rToken;
    }

    public String getrAmount() {
        return rAmount;
    }

    public void setrAmount(String rAmount) {
        this.rAmount = rAmount;
    }

    public String getrCharge() {
        return rCharge;
    }

    public void setrCharge(String rCharge) {
        this.rCharge = rCharge;
    }

    public String getrVat() {
        return rVat;
    }

    public void setrVat(String rVat) {
        this.rVat = rVat;
    }

    public String getrTariff() {
        return rTariff;
    }

    public void setrTariff(String rTariff) {
        this.rTariff = rTariff;
    }

    public String getrUnits() {
        return rUnits;
    }

    public void setrUnits(String rUnits) {
        this.rUnits = rUnits;
    }

    public String getrVendedAmount() {
        return rVendedAmount;
    }

    public void setrVendedAmount(String rVendedAmount) {
        this.rVendedAmount = rVendedAmount;
    }

    public String getrDate() {
        return rDate;
    }

    public void setrDate(String rDate) {
        this.rDate = rDate;
    }

    public String getrTime() {
        return rTime;
    }

    public void setrTime(String rTime) {
        this.rTime = rTime;
    }
}
