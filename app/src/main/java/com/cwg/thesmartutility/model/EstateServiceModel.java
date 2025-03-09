package com.cwg.thesmartutility.model;

public class EstateServiceModel {

    private String serviceID;
    private String serviceMeter;
    private String serviceAmount;
    private String servicePaymentDate;
    private String serviceExpiryDate;

    public EstateServiceModel(String serviceID, String serviceMeter, String serviceAmount, String servicePaymentDate, String serviceExpiryDate) {
        this.serviceID = serviceID;
        this.serviceMeter = serviceMeter;
        this.serviceAmount = serviceAmount;
        this.servicePaymentDate = servicePaymentDate;
        this.serviceExpiryDate = serviceExpiryDate;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getServiceMeter() {
        return serviceMeter;
    }

    public void setServiceMeter(String serviceMeter) {
        this.serviceMeter = serviceMeter;
    }

    public String getServiceAmount() {
        return serviceAmount;
    }

    public void setServiceAmount(String serviceAmount) {
        this.serviceAmount = serviceAmount;
    }

    public String getServicePaymentDate() {
        return servicePaymentDate;
    }

    public void setServicePaymentDate(String servicePaymentDate) {
        this.servicePaymentDate = servicePaymentDate;
    }

    public String getServiceExpiryDate() {
        return serviceExpiryDate;
    }

    public void setServiceExpiryDate(String serviceExpiryDate) {
        this.serviceExpiryDate = serviceExpiryDate;
    }
}
