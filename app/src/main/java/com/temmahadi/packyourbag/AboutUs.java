package com.temmahadi.packyourbag;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AboutUs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}