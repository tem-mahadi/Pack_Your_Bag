package com.temmahadi.packyourbag.BdApps_Backend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.temmahadi.packyourbag.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MobileNumberActivity extends AppCompatActivity {

    private TextInputEditText etMobileNumber;
    private Button btnSendOTP;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_number);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        etMobileNumber = findViewById(R.id.etMobileNumber);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        progressBar = findViewById(R.id.progressBar);

        btnSendOTP.setOnClickListener(v -> {
            String mobileNumber = etMobileNumber.getText().toString().trim();
            if (mobileNumber.isEmpty()) {
                Toast.makeText(this, "Enter a valid mobile number", Toast.LENGTH_SHORT).show();
            } else if (mobileNumber.length() != 11) {
                Toast.makeText(this, "Enter a valid 11-digit mobile number", Toast.LENGTH_SHORT).show();
            } else {
                sendMobileNumberToServer(mobileNumber);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void sendMobileNumberToServer(String mobileNumber) {
        progressBar.setVisibility(View.VISIBLE);
        btnSendOTP.setEnabled(false);

        Log.d("MobileNumberActivity", "Sending mobile number: " + mobileNumber);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<MobileNumberRequest> call = apiService.sendMobileNumber(mobileNumber);

        call.enqueue(new Callback<MobileNumberRequest>() {
            @Override
            public void onResponse(Call<MobileNumberRequest> call, Response<MobileNumberRequest> response) {
                progressBar.setVisibility(View.GONE);
                btnSendOTP.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    MobileNumberRequest body = response.body();
                    Log.d("MobileNumberActivity", "Success! Reference No: " + body.getReferenceNo());

                    Intent intent = new Intent(MobileNumberActivity.this, OTPActivity.class);
                    intent.putExtra("mobile_number", mobileNumber);
                    intent.putExtra("referenceNo", body.getReferenceNo());
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("MobileNumberActivity", "Response not successful: " + response.code());
                    String errorMessage = "Failed to send OTP";
                    if (response.code() == 404) errorMessage = "Server endpoint not found";
                    else if (response.code() == 500) errorMessage = "Server error occurred";
                    Toast.makeText(MobileNumberActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MobileNumberRequest> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSendOTP.setEnabled(true);
                Log.e("MobileNumberActivity", "Network Error: " + t.getMessage());

                String errorMessage = "Network error occurred";
                if (t instanceof java.net.ConnectException) errorMessage = "Cannot connect to server";
                else if (t instanceof java.net.SocketTimeoutException) errorMessage = "Request timeout";
                else if (t instanceof java.net.UnknownHostException) errorMessage = "Server not found";
                Toast.makeText(MobileNumberActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
