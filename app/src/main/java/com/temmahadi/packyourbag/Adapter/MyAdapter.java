package com.temmahadi.packyourbag.Adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.temmahadi.packyourbag.CheckList;
import com.temmahadi.packyourbag.Constants.MyConstants;
import com.temmahadi.packyourbag.DataBase.roomDB;
import com.temmahadi.packyourbag.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.myViewHolder> {
    List<String> titles; List<Integer> images; LayoutInflater inflater; Activity activity;
    int tripId;
    roomDB database;

    private static final float PRESS_SCALE = 0.94f;
    private static final long PRESS_DURATION_MS = 100;
    private static final long RELEASE_DURATION_MS = 180;

    public MyAdapter(Context context, List<String> titles, List<Integer> images, Activity activity, int tripId, roomDB database) {
        this.titles = titles;
        this.images = images;
        this.activity = activity;
        this.tripId = tripId;
        this.database = database;
        this.inflater= LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= inflater.inflate(R.layout.main_item,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        String categoryName = titles.get(position);
        holder.title.setText(categoryName);
        holder.img.setImageResource(images.get(position));

        // Unique subtle accent tint per category
        applyCategoryAccent(holder, position);

        // Per-category progress
        boolean isMySelections = MyConstants.MY_SELECTIONS.equals(categoryName);
        if (isMySelections) {
            // My Selections shows total packed count
            int packed = safeInt(database.mainDAO().getPackedCount(tripId));
            int total = safeInt(database.mainDAO().getItemsCount(tripId));
            holder.txtCategoryCount.setText(packed + "/" + total);
            int percent = total > 0 ? Math.round((packed * 100f) / total) : 0;
            holder.progressCategory.setProgress(percent);
            tintProgressBar(holder.progressCategory, percent);
        } else {
            int total = safeInt(database.mainDAO().getCountByCategory(categoryName, tripId));
            int packed = safeInt(database.mainDAO().getPackedCountByCategory(categoryName, tripId));
            holder.txtCategoryCount.setText(packed + "/" + total);
            int percent = total > 0 ? Math.round((packed * 100f) / total) : 0;
            holder.progressCategory.setProgress(percent);
            tintProgressBar(holder.progressCategory, percent);
        }

        // Scale animation on press
        holder.cardView.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    animateScale(holder.cardView, PRESS_SCALE, PRESS_DURATION_MS);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animateScale(holder.cardView, 1f, RELEASE_DURATION_MS);
                    break;
            }
            return false;
        });

        holder.cardView.setOnClickListener(view -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            Intent intent = new Intent(view.getContext(), CheckList.class);
            intent.putExtra(MyConstants.HEADER_SMALL, titles.get(adapterPosition));
            intent.putExtra(MyConstants.TRIP_ID_INTENT, tripId);
            if (MyConstants.MY_SELECTIONS.equals(titles.get(adapterPosition))) {
                intent.putExtra(MyConstants.SHOW_SMALL, MyConstants.FALSE_STRING);
            } else {
                intent.putExtra(MyConstants.SHOW_SMALL, MyConstants.TRUE_STRING);
            }
            view.getContext().startActivity(intent);
        });
    }

    private void tintProgressBar(ProgressBar bar, int percent) {
        int color;
        if (percent >= 100) {
            color = ContextCompat.getColor(bar.getContext(), R.color.status_packed);
        } else if (percent > 0) {
            color = ContextCompat.getColor(bar.getContext(), R.color.status_partial);
        } else {
            color = ContextCompat.getColor(bar.getContext(), R.color.status_empty);
        }
        bar.setProgressTintList(ColorStateList.valueOf(color));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void animateScale(View view, float targetScale, long duration) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", targetScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", targetScale);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(duration);
        if (targetScale == 1f) {
            set.setInterpolator(new OvershootInterpolator(2f));
        }
        set.start();
    }

    private static final int[] CATEGORY_TINTS = {
            Color.parseColor("#FFF0E8"),  // Basic Needs — warm peach
            Color.parseColor("#EDE7F6"),  // Clothing — soft lavender
            Color.parseColor("#FCE4EC"),  // Personal Care — light rose
            Color.parseColor("#FFF8E1"),  // Baby Needs — soft gold
            Color.parseColor("#E8F5E9"),  // Health — mint green
            Color.parseColor("#E3F2FD"),  // Technology — ice blue
            Color.parseColor("#FFF3E0"),  // Food — warm cream
            Color.parseColor("#E0F7FA"),  // Beach — aqua
            Color.parseColor("#EFEBE9"),  // Car Supplies — warm grey
            Color.parseColor("#F3E5F5"),  // Needs — soft purple
            Color.parseColor("#E8EAF6"),  // My List — periwinkle
            Color.parseColor("#FBE9E7"),  // My Selections — soft coral
    };

    private void applyCategoryAccent(myViewHolder holder, int position) {
        int tint = CATEGORY_TINTS[position % CATEGORY_TINTS.length];
        holder.cardView.setCardBackgroundColor(tint);
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{
        TextView title; ImageView img; LinearLayout linearLayout;
        MaterialCardView cardView; TextView txtCategoryCount; ProgressBar progressCategory;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            title= itemView.findViewById(R.id.title);
            img= itemView.findViewById(R.id.img);
            linearLayout= itemView.findViewById(R.id.linearLayout);
            cardView = itemView.findViewById(R.id.cardView);
            txtCategoryCount = itemView.findViewById(R.id.txtCategoryCount);
            progressCategory = itemView.findViewById(R.id.progressCategory);
        }
    }
}
