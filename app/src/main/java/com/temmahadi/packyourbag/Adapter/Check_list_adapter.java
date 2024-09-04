package com.temmahadi.packyourbag.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.temmahadi.packyourbag.Constants.MyConstants;
import com.temmahadi.packyourbag.DataBase.roomDB;
import com.temmahadi.packyourbag.Models.items;
import com.temmahadi.packyourbag.R;

import java.util.List;

public class Check_list_adapter extends RecyclerView.Adapter<checkListViewHolder>{
    Context context; List<items> list; roomDB database; String show;
    Toast message = null; // Declare the Toast variable outside onClick

    public Check_list_adapter(Context context, List<items> list, roomDB database, String show) {
        this.context = context;
        this.list = list;
        this.database = database;
        this.show = show;
        if(list.size()==0)
            Toast.makeText(context.getApplicationContext(),"Nothing to show",Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public checkListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new checkListViewHolder(LayoutInflater.from(context).inflate(R.layout.check_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull checkListViewHolder holder, int position) {
        holder.checkBox.setText(list.get(position).getItemName());
        holder.checkBox.setChecked(list.get(position).getChecked());

        if(MyConstants.FALSE_STRING.equals(show)){
            holder.deleteBtn.setVisibility(View.GONE);
            holder.layout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.border_one));
        } else {
            if(list.get(position).getChecked()){
                holder.layout.setBackgroundColor(Color.parseColor("#8e546f"));
            } else {
                holder.layout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.border_one));
            }
        }

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return;

                Boolean check = holder.checkBox.isChecked();
                database.mainDAO().checkUncheck(list.get(adapterPosition).getID(), check);

                if (MyConstants.FALSE_STRING.equals(show)) {
                    list = database.mainDAO().getAllSelected(true);
                    notifyDataSetChanged();
                } else {
                    list.get(adapterPosition).setChecked(check);
                    notifyItemChanged(adapterPosition);

                    if (message != null) {
                        message.cancel(); // Cancel the previous toast if it exists
                    }
                    if (list.get(adapterPosition).getChecked()) {
                        message = Toast.makeText(context, "(" + holder.checkBox.getText() + ") Is-Packed", Toast.LENGTH_SHORT);
                    } else {
                        message = Toast.makeText(context, "(" + holder.checkBox.getText() + ") Un-Packed", Toast.LENGTH_SHORT);
                    }
                    message.show();
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

    @Override
    public int getItemCount() {
        return list.size();
    }
}

class checkListViewHolder extends RecyclerView.ViewHolder{
    LinearLayout layout; CheckBox checkBox; Button deleteBtn;
    public checkListViewHolder(@NonNull View itemView) {
        super(itemView);
        layout= itemView.findViewById(R.id.linearLayout);
        checkBox= itemView.findViewById(R.id.checkbox);
        deleteBtn= itemView.findViewById(R.id.deleteBtn);
    }
}