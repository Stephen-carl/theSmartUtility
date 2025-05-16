package com.cwg.thesmartutility.model;

public class UnverifiedModel {
    private String pay_id, amount, trans_ref, payment_type, duration, customerID, brand;

    public UnverifiedModel(String pay_id, String amount, String trans_ref, String payment_type, String duration, String customerID, String brand) {
        this.pay_id = pay_id;
        this.amount = amount;
        this.trans_ref = trans_ref;
        this.payment_type = payment_type;
        this.duration = duration;
        this.customerID = customerID;
        this.brand = brand;
    }

    public String getPay_id() {
        return pay_id;
    }

    public void setPay_id(String pay_id) {
        this.pay_id = pay_id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTrans_ref() {
        return trans_ref;
    }

    public void setTrans_ref(String trans_ref) {
        this.trans_ref = trans_ref;
    }

    public String getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(String payment_type) {
        this.payment_type = payment_type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
