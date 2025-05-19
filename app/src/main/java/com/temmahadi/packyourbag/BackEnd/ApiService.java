package com.temmahadi.packyourbag.BackEnd;

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
    Call<OTPRequest> verifyOTP(@Field("Otp") String user_mobile, @Field("referenceNo") String RefNo);
}

