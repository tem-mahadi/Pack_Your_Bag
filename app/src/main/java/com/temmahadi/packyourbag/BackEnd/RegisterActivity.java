package com.temmahadi.packyourbag.BackEnd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.temmahadi.packyourbag.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText edUsername, edPhone, edPassword, edConfirm;
    Button btn;
    TextView tv; ProgressBar progressBar; MobileNumberRequest mobileNumberRequest;
    String username,password,phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edUsername = findViewById(R.id.editTextRegUsername);
        edPassword = findViewById(R.id.editTextRegPassword);
        edPhone = findViewById(R.id.editTextRegPhone);
        edConfirm = findViewById(R.id.editTextConfirmPassword);
        btn = findViewById(R.id.buttonRegister);
        tv = findViewById(R.id.textViewExUser);
        progressBar = findViewById(R.id.progressBar);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = edUsername.getText().toString();
                phone = edPhone.getText().toString();
                password = edPassword.getText().toString();
                String confirm = edConfirm.getText().toString();
                if (username.isEmpty() || phone.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill all the details", Toast.LENGTH_SHORT).show();
                } else {
                    if (password.compareTo(confirm) == 0) {
                        if(isValid(password)){
                            sendMobileNumberToServer(phone);
                        }else Toast.makeText(getApplicationContext(),"Password must contain at least 8 characters, having letter,digit and special symbol",Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Passwords didn't matched", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void sendMobileNumberToServer(String mobileNumber) {
        Log.d("PhoneUpload", "Sending phone: " + mobileNumber);
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<MobileNumberRequest> call = apiService.sendMobileNumber(mobileNumber);
        call.enqueue(new Callback<MobileNumberRequest>() {
            @Override
            public void onResponse(Call<MobileNumberRequest> call, Response<MobileNumberRequest> response) {

                // Hide the ProgressBar when response is received
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    mobileNumberRequest = response.body();
                    // Start OTP Activity
                    Intent intent = new Intent(RegisterActivity.this, OTPActivity.class);
                    intent.putExtra("username", username); // Pass mobile number to OTP Activity
                    intent.putExtra("password", password); // Pass mobile number to OTP Activity
                    intent.putExtra("mobile_number", mobileNumber); // Pass mobile number to OTP Activity
                    intent.putExtra("referenceNo", mobileNumberRequest.getReferenceNo()); // Pass referenceNo to OTP Activity
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Failed to send mobile number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MobileNumberRequest> call, Throwable t) {
                // Hide the ProgressBar on failure
                progressBar.setVisibility(View.GONE);
                Log.e("MobileNumberActivity", t.getMessage());
            }
        });
    }
    public static boolean isValid(String passwordhere) {
        int f1 = 0, f2 = 0, f3 = 0;
        if (passwordhere.length() < 8) {
            return false;
        } else {
            for (int p = 0; p < passwordhere.length(); p++) {
                if (Character.isLetter(passwordhere.charAt(p)))
                    f1 = 1;
            }

            for (int r = 0; r < passwordhere.length(); r++) {
                if (Character.isDigit(passwordhere.charAt(r)))
                    f2 = 1;
                }

                for (int s = 0; s < passwordhere.length(); s++) {
                    char c = passwordhere.charAt(s);
                    if (c >= 33 && c <= 46 || c == 64)
                        f3 = 1;
                }

                    if (f1 == 1 && f2 == 1 && f3 == 1)
                        return true;
                    return false;
                }
            }
        }

