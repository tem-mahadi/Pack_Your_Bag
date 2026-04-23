package com.temmahadi.packyourbag.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.temmahadi.packyourbag.Constants.MyConstants;
import com.temmahadi.packyourbag.DataBase.roomDB;
import com.temmahadi.packyourbag.Models.items;
import com.temmahadi.packyourbag.R;

import java.util.List;

public class Check_list_adapter extends RecyclerView.Adapter<checkListViewHolder>{
    Context context; List<items> list; roomDB database; String show;
    Toast message = null; // Declare the Toast variable outside onClick
    Runnable onListChanged;
    int tripId;

    public Check_list_adapter(Context context, List<items> list, roomDB database, String show, int tripId, Runnable onListChanged) {
        this.context = context;
        this.list = list;
        this.database = database;
        this.show = show;
        this.tripId = tripId;
        this.onListChanged = onListChanged;
    }

    @NonNull
    @Override
    public checkListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new checkListViewHolder(LayoutInflater.from(context).inflate(R.layout.check_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull checkListViewHolder holder, int position) {
        items currentItem = list.get(position);
        boolean isChecked = Boolean.TRUE.equals(currentItem.getChecked());

        holder.checkBox.setText(currentItem.getItemName());
        holder.checkBox.setChecked(isChecked);

        // Strikethrough + style for packed items
        applyPackedStyle(holder, isChecked);

        // Color stripe
        int stripeColor = isChecked
                ? ContextCompat.getColor(context, R.color.status_packed)
                : ContextCompat.getColor(context, R.color.status_empty);
        holder.viewStripe.setBackgroundColor(stripeColor);

        // Hide delete button in My Selections view
        if(MyConstants.FALSE_STRING.equals(show)){
            holder.deleteBtn.setVisibility(View.GONE);
        } else {
            holder.deleteBtn.setVisibility(View.VISIBLE);
        }

        // Fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(250);
        fadeIn.setStartOffset(position * 30L);
        holder.itemView.startAnimation(fadeIn);

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return;

                Boolean check = holder.checkBox.isChecked();
                database.mainDAO().checkUncheck(list.get(adapterPosition).getID(), check);

                if (MyConstants.FALSE_STRING.equals(show)) {
                    list = database.mainDAO().getAllSelected(true, tripId);
                    notifyDataSetChanged();
                } else {
                    list.get(adapterPosition).setChecked(check);
                    applyPackedStyle(holder, check);

                    // Animate stripe color
                    int newStripeColor = check
                            ? ContextCompat.getColor(context, R.color.status_packed)
                            : ContextCompat.getColor(context, R.color.status_empty);
                    holder.viewStripe.setBackgroundColor(newStripeColor);

                    if (message != null) {
                        message.cancel();
                    }
                    if (list.get(adapterPosition).getChecked()) {
                        message = Toast.makeText(context, "✅ " + holder.checkBox.getText() + " packed", Toast.LENGTH_SHORT);
                    } else {
                        message = Toast.makeText(context, "📦 " + holder.checkBox.getText() + " unpacked", Toast.LENGTH_SHORT);
                    }
                    message.show();
                }

                if (onListChanged != null) {
                    onListChanged.run();
                }
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return;

                new AlertDialog.Builder(context).setTitle("Delete ("+list.get(adapterPosition).getItemName()+")")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                database.mainDAO().delete(list.get(adapterPosition));
                                list.remove(adapterPosition);
                                notifyItemRemoved(adapterPosition);
                                if (onListChanged != null) {
                                    onListChanged.run();
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(context,"Operation is Canceled",Toast.LENGTH_SHORT).show();
                            }
                        }).setIcon(R.drawable.baseline_delete_forever_24).show();
            }
        });
    }

    private void applyPackedStyle(checkListViewHolder holder, boolean isPacked) {
        if (isPacked) {
            holder.checkBox.setPaintFlags(holder.checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.checkBox.setTextColor(ContextCompat.getColor(context, R.color.text_hint));
            if (holder.itemCard != null) {
                holder.itemCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_packed_light));
            }
        } else {
            holder.checkBox.setPaintFlags(holder.checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.checkBox.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            if (holder.itemCard != null) {
                holder.itemCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_card));
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

class checkListViewHolder extends RecyclerView.ViewHolder{
    LinearLayout layout; CheckBox checkBox; ImageButton deleteBtn;
    View viewStripe; MaterialCardView itemCard;
    public checkListViewHolder(@NonNull View itemView) {
        super(itemView);
        layout= itemView.findViewById(R.id.linearLayout);
        checkBox= itemView.findViewById(R.id.checkbox);
        deleteBtn= itemView.findViewById(R.id.deleteBtn);
        viewStripe = itemView.findViewById(R.id.viewStripe);
        itemCard = itemView.findViewById(R.id.itemCard);
    }
}