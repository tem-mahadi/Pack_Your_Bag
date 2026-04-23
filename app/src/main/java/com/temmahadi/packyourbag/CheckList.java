package com.temmahadi.packyourbag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.temmahadi.packyourbag.Adapter.Check_list_adapter;
import com.temmahadi.packyourbag.Constants.MyConstants;
import com.temmahadi.packyourbag.Data.appData;
import com.temmahadi.packyourbag.Data.TripSession;
import com.temmahadi.packyourbag.DataBase.roomDB;
import com.temmahadi.packyourbag.Models.items;
import com.temmahadi.packyourbag.utils.PackingStatsUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CheckList extends AppCompatActivity {
    RecyclerView recyclerView;
    Check_list_adapter checkListAdapter;
    roomDB database;
    List<items> itemsList = new ArrayList<>();
    List<items> sourceItems = new ArrayList<>();
    String header, show;
    EditText txtAdd;
    View btnAdd;
    LinearLayout linearLayout;
    TextView txtProgress;
    int tripId;

    private String currentQuery = "";

    private enum SortMode {
        NONE,
        ALPHABETICAL,
        PACKED_FIRST
    }

    private SortMode currentSortMode = SortMode.NONE;

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
                currentQuery = newText == null ? "" : newText;
                applyFiltersSortAndRefresh();
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean isMySelections = MyConstants.MY_SELECTIONS.equals(header);
        boolean isMyList = MyConstants.MY_LIST_CAMEL_CASE.equals(header);

        if (isMySelections) {
            menu.findItem(R.id.btnMySelections).setVisible(false);
            menu.findItem(R.id.btnReset).setVisible(false);
            menu.findItem(R.id.btnDeleteDefault).setVisible(false);
            menu.findItem(R.id.btnCustomList).setVisible(false);
            menu.findItem(R.id.btnMarkAllPacked).setVisible(false);
            menu.findItem(R.id.btnMarkAllUnpacked).setVisible(false);
            menu.findItem(R.id.btnDeletePackedCustom).setVisible(false);
        } else if (isMyList) {
            menu.findItem(R.id.btnCustomList).setVisible(false);
            menu.findItem(R.id.btnDeleteDefault).setVisible(false);
            menu.findItem(R.id.btnReset).setVisible(false);
        } else {
            menu.findItem(R.id.btnDeletePackedCustom).setVisible(false);
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
               intent.putExtra(MyConstants.TRIP_ID_INTENT, tripId);
             startActivityForResult(intent, 101);
            return true;
        } else if (id == R.id.btnCustomList) {
             intent.putExtra(MyConstants.HEADER_SMALL, MyConstants.MY_LIST_CAMEL_CASE);
             intent.putExtra(MyConstants.SHOW_SMALL, MyConstants.TRUE_STRING);
               intent.putExtra(MyConstants.TRIP_ID_INTENT, tripId);
             startActivity(intent);
            return true;
        } else if (id == R.id.btnDeleteDefault) {
             new AlertDialog.Builder(this)
                     .setTitle("Delete default data")
                     .setMessage("Are you sure?\n\nAs this will delete the data provided by (Pack Your Bag) while installing.")
                     .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             appData.persistDataByCategory(header, true, tripId);
                             reloadFromDatabase();
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
                             appData.persistDataByCategory(header, false, tripId);
                             reloadFromDatabase();
                         }
                     }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             // Do nothing
                         }
                     }).setIcon(R.drawable.baseline_error_24).show();
            return true;
        } else if (id == R.id.btnMarkAllPacked) {
            database.mainDAO().updateCheckedByCategory(header, true, tripId);
            Toast.makeText(this, "All items marked as packed", Toast.LENGTH_SHORT).show();
            reloadFromDatabase();
            return true;
        } else if (id == R.id.btnMarkAllUnpacked) {
            database.mainDAO().updateCheckedByCategory(header, false, tripId);
            Toast.makeText(this, "All items marked as unpacked", Toast.LENGTH_SHORT).show();
            reloadFromDatabase();
            return true;
        } else if (id == R.id.btnDeletePackedCustom) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete packed custom items")
                    .setMessage("Are you sure? This will delete packed custom items from this list.")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int deleted = safeInt(database.mainDAO().deleteCheckedByCategoryAndAddedBy(header, MyConstants.USER_SMALL, tripId));
                            Toast.makeText(CheckList.this, deleted + " item(s) deleted", Toast.LENGTH_SHORT).show();
                            reloadFromDatabase();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .setIcon(R.drawable.baseline_delete_forever_24)
                    .show();
            return true;
        } else if (id == R.id.btnSortAlphabetical) {
            currentSortMode = SortMode.ALPHABETICAL;
            applyFiltersSortAndRefresh();
            Toast.makeText(this, "Sorted A-Z", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.btnSortPackedFirst) {
            currentSortMode = SortMode.PACKED_FIRST;
            applyFiltersSortAndRefresh();
            Toast.makeText(this, "Sorted by packed items first", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.btnAboutUs) {
             intent = new Intent(this, AboutUs.class);
             startActivity(intent);
            return true;
        } else if (id == R.id.btnExit) {
            SharedPreferences sharedPreferences= getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); editor.apply();
            intent = new Intent(this, SplashScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            reloadFromDatabase();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        header = intent.getStringExtra(MyConstants.HEADER_SMALL);
        show = intent.getStringExtra(MyConstants.SHOW_SMALL);
        tripId = intent.getIntExtra(MyConstants.TRIP_ID_INTENT, -1);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(header);
        }

        txtAdd = findViewById(R.id.txtAdd);
        btnAdd = findViewById(R.id.btnAdd);
        recyclerView = findViewById(R.id.recyclerView);
        linearLayout = findViewById(R.id.linearLayout);
        txtProgress = findViewById(R.id.txtProgress);

        database = roomDB.getInstance(this);
        if (tripId <= 0) {
            tripId = TripSession.getOrCreateActiveTripId(this, database);
        }
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL));

        if (MyConstants.FALSE_STRING.equals(show)) {
            linearLayout.setVisibility(View.GONE);
        }

        reloadFromDatabase();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemName = txtAdd.getText().toString().trim();
                if (!itemName.isEmpty()) {
                    addNewItem(itemName);
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
        if (safeInt(database.mainDAO().getCountByCategoryAndName(header, itemName, tripId)) > 0) {
            Toast.makeText(CheckList.this, "This item already exists in the list", Toast.LENGTH_SHORT).show();
            return;
        }

        items item = new items();
        item.setChecked(false);
        item.setItemName(itemName);
        item.setCategory(header);
        item.setAddedBy(MyConstants.USER_SMALL);
        item.setTripId(tripId);
        database.mainDAO().saveItem(item);
        currentQuery = "";
        reloadFromDatabase();
        recyclerView.scrollToPosition(checkListAdapter.getItemCount() - 1);
        txtAdd.setText("");
        Toast.makeText(CheckList.this, "Item added", Toast.LENGTH_SHORT).show();
    }

    private void reloadFromDatabase() {
        if (MyConstants.FALSE_STRING.equals(show)) {
            sourceItems = database.mainDAO().getAllSelected(true, tripId);
        } else {
            sourceItems = database.mainDAO().getAll(header, tripId);
        }
        applyFiltersSortAndRefresh();
    }

    private void applyFiltersSortAndRefresh() {
        List<items> filteredList = new ArrayList<>();
        String query = currentQuery == null ? "" : currentQuery.trim().toLowerCase(Locale.ROOT);

        for (items item : sourceItems) {
            if (query.isEmpty()) {
                filteredList.add(item);
                continue;
            }

            String name = item.getItemName() == null ? "" : item.getItemName().toLowerCase(Locale.ROOT);
            if (name.contains(query)) {
                filteredList.add(item);
            }
        }

        applySort(filteredList);
        itemsList = filteredList;
        updateRecycler(itemsList);
        updateProgress();
    }

    private void applySort(List<items> list) {
        if (currentSortMode == SortMode.NONE) {
            return;
        }

        if (currentSortMode == SortMode.ALPHABETICAL) {
            Collections.sort(list, new Comparator<items>() {
                @Override
                public int compare(items first, items second) {
                    String firstName = first.getItemName() == null ? "" : first.getItemName();
                    String secondName = second.getItemName() == null ? "" : second.getItemName();
                    return firstName.compareToIgnoreCase(secondName);
                }
            });
            return;
        }

        Collections.sort(list, new Comparator<items>() {
            @Override
            public int compare(items first, items second) {
                boolean firstChecked = Boolean.TRUE.equals(first.getChecked());
                boolean secondChecked = Boolean.TRUE.equals(second.getChecked());

                if (firstChecked != secondChecked) {
                    return secondChecked ? 1 : -1;
                }

                String firstName = first.getItemName() == null ? "" : first.getItemName();
                String secondName = second.getItemName() == null ? "" : second.getItemName();
                return firstName.compareToIgnoreCase(secondName);
            }
        });
    }

    private void updateProgress() {
        if (MyConstants.FALSE_STRING.equals(show)) {
            txtProgress.setText(getString(R.string.packed_items_count, sourceItems.size()));
            return;
        }

        int total = safeInt(database.mainDAO().getCountByCategory(header, tripId));
        int packed = safeInt(database.mainDAO().getPackedCountByCategory(header, tripId));
        int percent = PackingStatsUtils.getReadinessPercent(packed, total);
        txtProgress.setText(getString(R.string.category_progress_text, packed, total, percent));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void updateRecycler(List<items> itemsList) {
        checkListAdapter = new Check_list_adapter(CheckList.this, itemsList, database, show, tripId, this::reloadFromDatabase);
        recyclerView.setAdapter(checkListAdapter);
    }
}
