package com.temmahadi.packyourbag.BdApps_Backend;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.temmahadi.packyourbag.R;

public class LandingActivity extends AppCompatActivity {

    private View ivLandingLogo, tvLandingAppName, tvLandingSubtitle;
    private View priceSection, priceGlow;
    private View feature1, feature2, feature3;
    private View buttonsSection, tvFooterNote;
    private View btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        // Hide ActionBar for immersive landing
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Bind views
        ivLandingLogo = findViewById(R.id.ivLandingLogo);
        tvLandingAppName = findViewById(R.id.tvLandingAppName);
        tvLandingSubtitle = findViewById(R.id.tvLandingSubtitle);
        priceSection = findViewById(R.id.priceSection);
        priceGlow = findViewById(R.id.priceGlow);
        feature1 = findViewById(R.id.feature1);
        feature2 = findViewById(R.id.feature2);
        feature3 = findViewById(R.id.feature3);
        buttonsSection = findViewById(R.id.buttonsSection);
        tvFooterNote = findViewById(R.id.tvFooterNote);
        btnRegister = findViewById(R.id.btnRegister);

        setupClickListeners();
        startEntranceAnimations();
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            animateButtonPress(v, this::navigateToRegistration);
        });
    }

    private void startEntranceAnimations() {
        Handler handler = new Handler(Looper.getMainLooper());

        View[] views = {
                ivLandingLogo, tvLandingAppName, tvLandingSubtitle,
                priceSection, feature1, feature2, feature3,
                buttonsSection, tvFooterNote
        };
        for (View v : views) v.setAlpha(0f);

        // Logo scales in with bounce
        handler.postDelayed(() -> {
            ivLandingLogo.setAlpha(1f);
            ivLandingLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_bounce_in));
        }, 300);

        // App name slides up
        handler.postDelayed(() -> slideUpFadeIn(tvLandingAppName, 500), 500);

        // Subtitle
        handler.postDelayed(() -> fadeInView(tvLandingSubtitle, 400), 700);

        // Price section — bounce in
        handler.postDelayed(this::animatePriceSection, 900);

        // Feature cards stagger in
        handler.postDelayed(() -> slideUpFadeIn(feature1, 400), 1200);
        handler.postDelayed(() -> slideUpFadeIn(feature2, 400), 1350);
        handler.postDelayed(() -> slideUpFadeIn(feature3, 400), 1500);

        // Buttons section
        handler.postDelayed(() -> slideUpFadeIn(buttonsSection, 500), 1700);

        // Footer
        handler.postDelayed(() -> fadeInView(tvFooterNote, 400), 1900);

        // Pulse on price glow
        handler.postDelayed(this::startPricePulse, 2100);
    }

    private void animatePriceSection() {
        priceSection.setAlpha(1f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(priceSection, "scaleX", 0.3f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(priceSection, "scaleY", 0.3f, 1.05f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(priceSection, "alpha", 0f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(700);
        set.setInterpolator(new OvershootInterpolator(1.5f));
        set.start();
    }

    private void startPricePulse() {
        ObjectAnimator pulseScaleX = ObjectAnimator.ofFloat(priceGlow, "scaleX", 1f, 1.08f, 1f);
        ObjectAnimator pulseScaleY = ObjectAnimator.ofFloat(priceGlow, "scaleY", 1f, 1.08f, 1f);
        ObjectAnimator pulseAlpha = ObjectAnimator.ofFloat(priceGlow, "alpha", 1f, 0.6f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(pulseScaleX, pulseScaleY, pulseAlpha);
        set.setDuration(2500);
        set.setStartDelay(500);
        set.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                startPricePulse();
            }
        }, 3500);
    }

    private void fadeInView(View view, long duration) {
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(duration)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    private void slideUpFadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.animate().alpha(1f).translationY(0f).setDuration(duration)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    private void animateButtonPress(View view, Runnable onComplete) {
        view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f)
                        .setDuration(100).withEndAction(onComplete).start())
                .start();
    }

    private void navigateToRegistration() {
        Intent intent = new Intent(this, MobileNumberActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
