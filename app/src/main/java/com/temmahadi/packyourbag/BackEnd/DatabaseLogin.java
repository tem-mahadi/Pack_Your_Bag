package com.temmahadi.packyourbag.BackEnd;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseLogin extends SQLiteOpenHelper {
    public DatabaseLogin(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String qry1 = "create table users(username text,phone text, password text)";
        sqLiteDatabase.execSQL(qry1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public void register(String username, String phone,String password){
        ContentValues cv = new ContentValues();
        cv.put("username",username);
        cv.put("phone",phone);
        cv.put("password",password);
        SQLiteDatabase db = getWritableDatabase();
        db.insert("users",null,cv);
        db.close();
    }
    public int login(String username, String password){
        int result=0;
        String[] str = new String[2];
        str[0]= username;
        str[1]= password;
        SQLiteDatabase db= getReadableDatabase();
        @SuppressLint("Recycle") Cursor c = db.rawQuery("select * from users where username=? and password=?",str);
        if(c.moveToFirst()){
            result =1;
        }
        return result;
    }
}
