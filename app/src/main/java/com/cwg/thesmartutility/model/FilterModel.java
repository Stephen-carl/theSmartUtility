package com.cwg.thesmartutility.model;

public class FilterModel {
    String meterName, meterNumber, blockNo, flatNo;

    public FilterModel(String meterName, String meterNumber, String blockNo, String flatNo) {
        this.meterName = meterName;
        this.meterNumber = meterNumber;
        this.blockNo = blockNo;
        this.flatNo = flatNo;
    }

    public String getBlockNo() {
        return blockNo;
    }

    public void setBlockNo(String blockNo) {
        this.blockNo = blockNo;
    }

    public String getFlatNo() {
        return flatNo;
    }

    public void setFlatNo(String flatNo) {
        this.flatNo = flatNo;
    }

    public String getMeterName() {
        return meterName;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterName(String meterName) {
        this.meterName = meterName;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }
}
