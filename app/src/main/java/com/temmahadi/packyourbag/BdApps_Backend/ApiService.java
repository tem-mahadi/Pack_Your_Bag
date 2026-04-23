package com.temmahadi.packyourbag.BdApps_Backend;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("send_otp.php")
    Call<MobileNumberRequest> sendMobileNumber(@Field("mobile_number") String mobileNumber);

    @FormUrlEncoded
    @POST("verify_otp.php")
    Call<OTPRequest> verifyOTP(
            @Field("referenceNo") String referenceNo,
            @Field("otp") String otp
    );

    @POST("unsubscribe.php")
    Call<UnsubscribeResponse> unsubscribe(@retrofit2.http.Body UnsubscribeRequest request);
}
