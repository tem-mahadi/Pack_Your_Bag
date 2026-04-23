package com.temmahadi.packyourbag.BdApps_Backend;

public class OTPRequest {
    private String referenceNo;
    private String otp;
    private String subscriptionStatus;

    public OTPRequest(String referenceNo, String otp, String subscriptionStatus) {
        this.referenceNo = referenceNo;
        this.otp = otp;
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getreferenceNo() { return referenceNo; }
    public void setreferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public String getsubscriptionStatus() { return subscriptionStatus; }
    public void setsubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }
}
