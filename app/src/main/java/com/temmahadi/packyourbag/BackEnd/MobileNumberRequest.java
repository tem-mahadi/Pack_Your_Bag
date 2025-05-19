package com.temmahadi.packyourbag.BackEnd;
public class MobileNumberRequest {
    private String mobileNumber;
    private String referenceNo;

    public MobileNumberRequest(String mobileNumber, String referenceNo) {
        this.mobileNumber = mobileNumber;
        this.referenceNo = referenceNo;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    // Constructor with parameter
    public MobileNumberRequest(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    // Default constructor (optional, but useful for serialization)
    public MobileNumberRequest() {}

    // Getter and Setter methods
    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}


