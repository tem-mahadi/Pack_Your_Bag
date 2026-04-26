package com.temmahadi.packyourbag.BdApps_Backend;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("send_otp.php")
    Call<MobileNumberRequest> sendMobileNumber(@Field("user_mobile") String mobileNumber);

    @FormUrlEncoded
    @POST("verify_otp.php")
    Call<OTPRequest> verifyOTP(
            @Field("Otp") String otp,
            @Field("referenceNo") String referenceNo
    );

    @POST("unsubscribe.php")
    Call<UnsubscribeResponse> unsubscribeUser(@retrofit2.http.Body UnsubscribeRequest request);
}
