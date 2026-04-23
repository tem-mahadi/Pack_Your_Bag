package com.temmahadi.packyourbag.BdApps_Backend;

import com.google.gson.annotations.SerializedName;

public class UnsubscribeResponse {
    @SerializedName("statusCode")
    private String statusCode;
    @SerializedName("statusDetail")
    private String statusDetail;
    @SerializedName("subscriptionStatus")
    private String subscriptionStatus;
    @SerializedName("version")
    private String version;
    @SerializedName("requestId")
    private String requestId;

    public String getStatusCode() { return statusCode; }
    public String getStatusDetail() { return statusDetail; }
    public String getSubscriptionStatus() { return subscriptionStatus; }
    public String getVersion() { return version; }
    public String getRequestId() { return requestId; }

    public boolean isSuccess() { return "S1000".equals(statusCode); }
    public boolean isError() { return subscriptionStatus != null && subscriptionStatus.equals("ERROR"); }
}
