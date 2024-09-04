package com.temmahadi.packyourbag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.temmahadi.packyourbag.Adapter.MyAdapter;
import com.temmahadi.packyourbag.Constants.MyConstants;
import com.temmahadi.packyourbag.Data.appData;
import com.temmahadi.packyourbag.DataBase.roomDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<String> titles; List<Integer> images; MyAdapter adapter;
    roomDB database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        recyclerView= findViewById(R.id.recyclerView);
        addAllTitles();
        addAllImages();
        persistAppData();
        database = roomDB.getInstance(this);
        System.out.println("----------------->"+database.mainDAO().getAllSelected(false).get(0).getItemName());
        adapter = new MyAdapter(this, titles, images, MainActivity.this);
        GridLayoutManager gridLayoutManager= new GridLayoutManager(this,2,GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

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
    private void persistAppData(){
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= prefs.edit();

        database = roomDB.getInstance(this);
        appData appdata = new appData(database);
        int last = prefs.getInt(appData.LAST_VERSION,0);
        if(!prefs.getBoolean(MyConstants.FIRST_TIME_CAMEL_CASE, false)){
            appdata.persistAlldata();
            editor.putBoolean(MyConstants.FIRST_TIME_CAMEL_CASE,true);
            editor.apply();
        } else if (last<appData.NEW_VERSION) {
            database.mainDAO().deleteAllSystemItems(MyConstants.SYSTEM_SMALL);
            appdata.persistAlldata();
            editor.putInt(appData.LAST_VERSION,appData.NEW_VERSION);
            editor.apply();
        }
    }

    private void addAllTitles(){
        titles= new ArrayList<>();
        titles.add(MyConstants.BASIC_NEEDS_CAMEL_CASE);
        titles.add(MyConstants.CLOTHING_CAMEL_CASE);
        titles.add(MyConstants.PERSONAL_CARE_CAMEL_CASE);
        titles.add(MyConstants.BABY_NEEDS_CAMEL_CASE);
        titles.add(MyConstants.HEALTH_CAMEL_CASE);
        titles.add(MyConstants.TECHNOLOGY_CAMEL_CASE);
        titles.add(MyConstants.FOOD_CAMEL_CASE);
        titles.add(MyConstants.BEACH_SUPPLIES_CAMEL_CASE);
        titles.add(MyConstants.CAR_SUPPLIES_CAMEL_CASE);
        titles.add(MyConstants.NEEDS_CAMEL_CASE);
        titles.add(MyConstants.MY_LIST_CAMEL_CASE);
        titles.add(MyConstants.MY_SELECTIONS_CAMEL_CASE);
    }

    private void addAllImages(){
        images= new ArrayList<>();
        images.add(R.drawable.p1);
        images.add(R.drawable.p2);
        images.add(R.drawable.p3);
        images.add(R.drawable.p4);
        images.add(R.drawable.p5);
        images.add(R.drawable.p6);
        images.add(R.drawable.p7);
        images.add(R.drawable.p8);
        images.add(R.drawable.p9);
        images.add(R.drawable.p10);
        images.add(R.drawable.p11);
        images.add(R.drawable.p12);
    }
}