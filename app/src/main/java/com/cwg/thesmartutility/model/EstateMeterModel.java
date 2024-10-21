package com.cwg.thesmartutility.model;

public class EstateMeterModel {
    String meterID, customerIDe, tariff, brand, vendStatus, email, userName;
    int estateID;

    public EstateMeterModel(String meterID, String customerIDe, String tariff, String brand, String vendStatus, String email, String userName, int estateID) {
        this.meterID = meterID;
        this.customerIDe = customerIDe;
        this.tariff = tariff;
        this.brand = brand;
        this.vendStatus = vendStatus;
        this.email = email;
        this.userName = userName;
        this.estateID = estateID;
    }

    public String getMeterID() {
        return meterID;
    }

    public void setMeterID(String meterID) {
        this.meterID = meterID;
    }

    public String getCustomerIDe() {
        return customerIDe;
    }

    public void setCustomerIDe(String customerIDe) {
        this.customerIDe = customerIDe;
    }

    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVendStatus() {
        return vendStatus;
    }

    public void setVendStatus(String vendStatus) {
        this.vendStatus = vendStatus;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getEstateID() {
        return estateID;
    }

    public void setEstateID(int estateID) {
        this.estateID = estateID;
    }
}
