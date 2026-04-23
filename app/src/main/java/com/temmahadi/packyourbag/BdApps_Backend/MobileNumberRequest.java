package com.temmahadi.packyourbag.BdApps_Backend;

public class MobileNumberRequest {
    private String mobileNumber;
    private String referenceNo;

    public MobileNumberRequest(String mobileNumber, String referenceNo) {
        this.mobileNumber = mobileNumber;
        this.referenceNo = referenceNo;
    }

    public MobileNumberRequest(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public MobileNumberRequest() {}

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
}
