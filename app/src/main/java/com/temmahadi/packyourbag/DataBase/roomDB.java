package com.temmahadi.packyourbag.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.temmahadi.packyourbag.Dao.itemDao;
import com.temmahadi.packyourbag.Models.items;

@Database(entities = items.class,version = 1,exportSchema = false)
public abstract class roomDB extends RoomDatabase {
    private static roomDB database;
    private static String DATABASE_NAME = "MyDB";

    public synchronized static roomDB getInstance(Context context){
        if(database==null){
            database = Room.databaseBuilder(context.getApplicationContext(),roomDB.class,DATABASE_NAME)
                    .allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }
        return database;
    }
    public abstract itemDao mainDAO();
}
