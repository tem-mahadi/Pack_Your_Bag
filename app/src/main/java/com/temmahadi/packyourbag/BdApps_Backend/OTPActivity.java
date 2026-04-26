package com.temmahadi.packyourbag.BdApps_Backend;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.temmahadi.packyourbag.MainActivity;
import com.temmahadi.packyourbag.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPActivity extends AppCompatActivity {

    private EditText etOTP1, etOTP2, etOTP3, etOTP4, etOTP5, etOTP6;
    private Button btnSubmitOTP;
    private ProgressBar progressBar;
    private TextView textMobileNumber;
    private String mobileNumber, referenceNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mobileNumber = getIntent().getStringExtra("mobile_number");
        referenceNo = getIntent().getStringExtra("referenceNo");

        textMobileNumber = findViewById(R.id.textMobileNumber);
        etOTP1 = findViewById(R.id.tvEnterOTP1);
        etOTP2 = findViewById(R.id.tvEnterOTP2);
        etOTP3 = findViewById(R.id.tvEnterOTP3);
        etOTP4 = findViewById(R.id.tvEnterOTP4);
        etOTP5 = findViewById(R.id.tvEnterOTP5);
        etOTP6 = findViewById(R.id.tvEnterOTP6);
        btnSubmitOTP = findViewById(R.id.btnSubmitOTP);
        progressBar = findViewById(R.id.progressBar2);

        textMobileNumber.setText("+88-" + mobileNumber);

        setOTPInputs();

        btnSubmitOTP.setOnClickListener(v -> {
            String otp = etOTP1.getText().toString() + etOTP2.getText().toString()
                    + etOTP3.getText().toString() + etOTP4.getText().toString()
                    + etOTP5.getText().toString() + etOTP6.getText().toString();

            if (otp.length() == 6) {
                verifyOTP(otp);
            } else {
                Toast.makeText(this, "Please enter the complete 6-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void verifyOTP(String otp) {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmitOTP.setEnabled(false);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<OTPRequest> call = apiService.verifyOTP(otp, referenceNo);

        call.enqueue(new Callback<OTPRequest>() {
            @Override
            public void onResponse(Call<OTPRequest> call, Response<OTPRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().getsubscriptionStatus();
                    Log.d("OTPActivity", "Status: " + status);

                    if ("S1000".equalsIgnoreCase(status) || 
                        "INITIAL CHARGING PENDING".equalsIgnoreCase(status) || 
                        "REGISTERED".equalsIgnoreCase(status)) {
                        goToMainActivity();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnSubmitOTP.setEnabled(true);
                        Toast.makeText(OTPActivity.this, "OTP Failed - Status: " + status, Toast.LENGTH_LONG).show();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnSubmitOTP.setEnabled(true);
                    Toast.makeText(OTPActivity.this, "Verification failed. Try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OTPRequest> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSubmitOTP.setEnabled(true);
                Toast.makeText(OTPActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMainActivity() {
        progressBar.setVisibility(View.GONE);
        SubscriptionManager.completeUserLogin(OTPActivity.this, mobileNumber);

        Intent intent = new Intent(OTPActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void setOTPInputs() {
        setupOTPJump(etOTP1, etOTP2);
        setupOTPJump(etOTP2, etOTP3);
        setupOTPJump(etOTP3, etOTP4);
        setupOTPJump(etOTP4, etOTP5);
        setupOTPJump(etOTP5, etOTP6);
    }

    private void setupOTPJump(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    next.requestFocus();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
