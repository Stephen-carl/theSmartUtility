package com.cwg.thesmartutility.model;

public class ServiceModel {
    private int serviceId;
    private int estateId;
    private String typeId;
    private String duration;
    private String serviceAmount;
    private String typeName;

    public ServiceModel(int serviceId, int estateId, String typeId, String duration, String serviceAmount, String typeName) {
        this.serviceId = serviceId;
        this.estateId = estateId;
        this.typeId = typeId;
        this.duration = duration;
        this.serviceAmount = serviceAmount;
        this.typeName = typeName;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getEstateId() {
        return estateId;
    }

    public void setEstateId(int estateId) {
        this.estateId = estateId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getServiceAmount() {
        return serviceAmount;
    }

    public void setServiceAmount(String serviceAmount) {
        this.serviceAmount = serviceAmount;
    }
    public String getTypeName() {
        return typeName;
    }
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
