package com.temmahadi.packyourbag.BackEnd;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.temmahadi.packyourbag.R;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPActivity extends AppCompatActivity {
    EditText edotp;
    Button submitbtn;
    String phone,username,password;
    String ref; ProgressBar progressBar; OTPRequest otpRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        Log.d("ActivityCheck", "OTPActivity started");

        edotp= findViewById(R.id.editTextOTP);
        submitbtn= findViewById(R.id.buttonSubmit);
        progressBar= findViewById(R.id.progressBarOTP);

        phone= getIntent().getStringExtra("mobile_number");
        username= getIntent().getStringExtra("username");
        password= getIntent().getStringExtra("password");

        ref = getIntent().getStringExtra("referenceNo");

        Toast.makeText(OTPActivity.this, "\n"+ref, Toast.LENGTH_SHORT).show();

        submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = edotp.getText().toString();
                if (!otp.isEmpty()) {
                    DatabaseLogin db = new DatabaseLogin(getApplicationContext(),"healthcare",null,1);
                    db.register(username,phone,password);
                    Toast.makeText(getApplicationContext(),"Record Inserted",Toast.LENGTH_SHORT).show();
                    verifyOTPWithServer(ref, otp);
                } else {
                    Toast.makeText(OTPActivity.this, "Enter a valid OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void verifyOTPWithServer(String referenceNo, String otp) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<OTPRequest> call = apiService.verifyOTP(otp,referenceNo);
        call.enqueue(new Callback<OTPRequest>() {
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onResponse(Call<OTPRequest> call, Response<OTPRequest> response) {
                // Hide the ProgressBar when response is received
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    otpRequest= response.body();
                    if(Objects.equals(otpRequest.getsubscriptionStatus(), "S1000")) {
//                        DatabaseLogin db= new DatabaseLogin(getApplicationContext(),"healthcare",null,1);
//                        db.register(username,phone,password);
                        Toast.makeText(OTPActivity.this, "OTP Verified! Access granted.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OTPActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    Toast.makeText(OTPActivity.this, "Request has been sent", Toast.LENGTH_SHORT).show();
                    // Proceed to next step of the app
                } else {
                    Toast.makeText(OTPActivity.this, "OTP Verification Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OTPRequest> call, Throwable t) {
                // Hide the ProgressBar on failure
                progressBar.setVisibility(View.GONE);
                Log.e("OTPActivity", t.getMessage());
            }
        });
    }
}