package com.temmahadi.packyourbag;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.temmahadi.packyourbag.BdApps_Backend.LandingActivity;
import com.temmahadi.packyourbag.BdApps_Backend.SubscriptionManager;

/**
 * Splash Screen
 * Checks subscription status and navigates accordingly:
 *  - Subscribed → MainActivity (app dashboard)
 *  - Not subscribed → LandingActivity (pricing + register)
 */
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Safe hide — NoActionBar theme may not have an ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Animate icon: scale up + fade in
        View centerContent = findViewById(R.id.centerContent);

        centerContent.setAlpha(0f);
        centerContent.setScaleX(0.85f);
        centerContent.setScaleY(0.85f);

        centerContent.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(700)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();

        new Handler().postDelayed(this::checkSubscriptionAndNavigate, 1600);
    }

    private void checkSubscriptionAndNavigate() {
        try {
            if (SubscriptionManager.isSubscribed(this)) {
                navigateToMain();
            } else {
                navigateToLanding();
            }
        } catch (Exception e) {
            e.printStackTrace();
            navigateToLanding();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void navigateToLanding() {
        Intent intent = new Intent(this, LandingActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}