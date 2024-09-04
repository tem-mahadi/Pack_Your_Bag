package com.temmahadi.packyourbag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.temmahadi.packyourbag.Adapter.Check_list_adapter;
import com.temmahadi.packyourbag.Constants.MyConstants;
import com.temmahadi.packyourbag.Data.appData;
import com.temmahadi.packyourbag.DataBase.roomDB;
import com.temmahadi.packyourbag.Models.items;
import com.temmahadi.packyourbag.R;

import java.util.ArrayList;
import java.util.List;

public class CheckList extends AppCompatActivity {
    RecyclerView recyclerView;
    Check_list_adapter checkListAdapter;
    roomDB database;
    List<items> itemsList = new ArrayList<>();
    String header, show;
    EditText txtAdd;
    Button btnAdd;
    LinearLayout linearLayout;

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_one, menu);

        MenuItem menuItem = menu.findItem(R.id.btnSearch);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<items> filteredList = new ArrayList<>();
                for (items item : itemsList) {
                    if (item.getItemName().toLowerCase().startsWith(newText.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
                updateRecycler(filteredList);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (MyConstants.MY_SELECTIONS.equals(header)) {
            menu.findItem(R.id.btnMySelections).setVisible(false);
            menu.findItem(R.id.btnReset).setVisible(false);
            menu.findItem(R.id.btnDeleteDefault).setVisible(false);
        } else if (MyConstants.MY_LIST_CAMEL_CASE.equals(header)) {
            menu.findItem(R.id.btnCustomList).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         Intent intent = new Intent(this, CheckList.class);
         appData appData = new appData(database, this);

        int id = item.getItemId();

        if (id == R.id.btnMySelections) {
             intent.putExtra(MyConstants.HEADER_SMALL, MyConstants.MY_SELECTIONS);
             intent.putExtra(MyConstants.SHOW_SMALL, MyConstants.FALSE_STRING);
             startActivityForResult(intent, 101);
            return true;
        } else if (id == R.id.btnCustomList) {
             intent.putExtra(MyConstants.HEADER_SMALL, MyConstants.MY_LIST_CAMEL_CASE);
             intent.putExtra(MyConstants.SHOW_SMALL, MyConstants.TRUE_STRING);
             startActivity(intent);
            return true;
        } else if (id == R.id.btnDeleteDefault) {
             new AlertDialog.Builder(this)
                     .setTitle("Delete default data")
                     .setMessage("Are you sure?\n\nAs this will delete the data provided by (Pack Your Bag) while installing.")
                     .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             appData.persistDataByCategory(header, true);
                             itemsList = database.mainDAO().getAll(header);
                             updateRecycler(itemsList);
                         }
                     }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             // Do nothing
                         }
                     }).setIcon(R.drawable.baseline_error_24).show();
            return true;
        } else if (id == R.id.btnReset) {
             new AlertDialog.Builder(this)
                     .setTitle("Reset to default")
                     .setMessage("Are you sure? This will load the default data provided by (Pack Your Bag) and will delete the custom data you have added in (" + header + " )")
                     .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             appData.persistDataByCategory(header, false);
                             itemsList = database.mainDAO().getAll(header);
                             updateRecycler(itemsList);
                         }
                     }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             // Do nothing
                         }
                     }).setIcon(R.drawable.baseline_error_24).show();
            return true;
        } else if (id == R.id.btnAboutUs) {
             intent = new Intent(this, AboutUs.class);
             startActivity(intent);
            return true;
        } else if (id == R.id.btnExit) {
             this.finishAffinity();
             Toast.makeText(this, "Pack your bag\nExit completed.", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            itemsList = database.mainDAO().getAll(header);
            updateRecycler(itemsList);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        header = intent.getStringExtra(MyConstants.HEADER_SMALL);
        show = intent.getStringExtra(MyConstants.SHOW_SMALL);
        getSupportActionBar().setTitle(header);

        txtAdd = findViewById(R.id.txtAdd);
        btnAdd = findViewById(R.id.btnAdd);
        recyclerView = findViewById(R.id.recyclerView);
        linearLayout = findViewById(R.id.linearLayout);

        database = roomDB.getInstance(this);
        if (MyConstants.FALSE_STRING.equals(show)) {
            linearLayout.setVisibility(View.GONE);
            itemsList = database.mainDAO().getAllSelected(true);
        } else {
            itemsList = database.mainDAO().getAll(header);
        }

        updateRecycler(itemsList);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemName = txtAdd.getText().toString();
                if (!itemName.isEmpty()) {
                    addNewItem(itemName);
                    Toast.makeText(CheckList.this, "Item added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CheckList.this, "Empty cannot be added", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void addNewItem(String itemName) {
        items item = new items();
        item.setChecked(false);
        item.setItemName(itemName);
        item.setCategory(header);
        item.setAddedBy(MyConstants.USER_SMALL);
        database.mainDAO().saveItem(item);
        itemsList = database.mainDAO().getAll(header);
        updateRecycler(itemsList);
        recyclerView.scrollToPosition(checkListAdapter.getItemCount() - 1);
        txtAdd.setText("");
    }

    private void updateRecycler(List<items> itemsList) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL));
        checkListAdapter = new Check_list_adapter(CheckList.this, itemsList, database, show);
        recyclerView.setAdapter(checkListAdapter);
    }
}
