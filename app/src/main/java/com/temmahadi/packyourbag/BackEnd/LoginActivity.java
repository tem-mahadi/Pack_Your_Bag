package com.temmahadi.packyourbag.BackEnd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.temmahadi.packyourbag.MainActivity;
import com.temmahadi.packyourbag.R;

public class LoginActivity extends AppCompatActivity {

    EditText edUsername, edPassword;
    Button btn;
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edUsername= findViewById(R.id.editTextLoginUsername);
        edPassword= findViewById(R.id.editTextLoginPassword);
        btn = findViewById(R.id.buttonLogin);
        tv= findViewById(R.id.textViewNewUser);
        SharedPreferences sharedPreferences= getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("directLogin", false)) {
            // If the user is already logged in, start MainActivity and finish LoginActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edUsername.getText().toString();
                String password = edPassword.getText().toString();
                DatabaseLogin db= new DatabaseLogin(getApplicationContext(),"healthcare",null,1);
                if(username.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please fill all the details",Toast.LENGTH_SHORT).show();
                }
                else {
                    if(db.login(username,password)==1) {
                        Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor= sharedPreferences.edit();
                        editor.putString("username",username);
                        editor.putBoolean("directLogin",true);
                        editor.apply();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                    else { Toast.makeText(getApplicationContext(), "Invalid Password", Toast.LENGTH_SHORT).show(); }
                }
            }
        });
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

    }
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_INTERVAL>System.currentTimeMillis()){
            super.onBackPressed();
        }
        else {
            Toast.makeText(this, "Tap back Button in order to exit",Toast.LENGTH_SHORT).show();
        }
        mBackPressed= System.currentTimeMillis();
    }
}