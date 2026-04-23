package com.temmahadi.packyourbag.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.temmahadi.packyourbag.R;

/**
 * A circular arc progress view that animates from 0 to the target percentage.
 * Color shifts from red/coral (low) → orange (mid) → green (high).
 */
public class CircularProgressView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private int targetPercent = 0;
    private float animatedPercent = 0f;

    private static final float STROKE_WIDTH_RATIO = 0.10f;
    private static final float START_ANGLE = -90f;
    private static final long ANIMATION_DURATION_MS = 900;

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.progress_track));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_primary));

        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
    }

    public void setProgress(int percent) {
        targetPercent = Math.max(0, Math.min(100, percent));
        animateToTarget();
    }

    private void animateToTarget() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, targetPercent);
        animator.setDuration(ANIMATION_DURATION_MS);
        animator.setInterpolator(new DecelerateInterpolator(2f));
        animator.addUpdateListener(animation -> {
            animatedPercent = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = Math.min(getWidth(), getHeight());
        float strokeWidth = size * STROKE_WIDTH_RATIO;
        float half = strokeWidth / 2f;

        trackPaint.setStrokeWidth(strokeWidth);
        arcPaint.setStrokeWidth(strokeWidth);

        arcRect.set(half, half, size - half, size - half);

        // Background track
        canvas.drawArc(arcRect, 0, 360, false, trackPaint);

        // Colored progress arc
        arcPaint.setColor(getProgressColor(animatedPercent));
        float sweepAngle = (animatedPercent / 100f) * 360f;
        canvas.drawArc(arcRect, START_ANGLE, sweepAngle, false, arcPaint);

        // Center percentage text
        float centerX = size / 2f;
        float centerY = size / 2f;
        textPaint.setTextSize(size * 0.22f);
        labelPaint.setTextSize(size * 0.10f);

        String percentText = Math.round(animatedPercent) + "%";
        float textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2f) - (size * 0.04f);
        canvas.drawText(percentText, centerX, textY, textPaint);

        // "Ready" label below
        float labelY = textY + (size * 0.14f);
        canvas.drawText("Ready", centerX, labelY, labelPaint);
    }

    private int getProgressColor(float percent) {
        if (percent < 40) {
            return ContextCompat.getColor(getContext(), R.color.progress_low);
        } else if (percent < 75) {
            return ContextCompat.getColor(getContext(), R.color.progress_mid);
        } else {
            return ContextCompat.getColor(getContext(), R.color.progress_high);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredSize = dpToPx(120);
        int width = resolveSize(desiredSize, widthMeasureSpec);
        int height = resolveSize(desiredSize, heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
