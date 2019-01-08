package ru.korostylev.notepad.notepad;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "myDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // создаем таблицу с полями
        sqLiteDatabase.execSQL("create table notes ("
                + "id integer primary key autoincrement,"
                + "title text,"
                + "content text" + ");");
        // создаем таблицу с полями
        sqLiteDatabase.execSQL("create table cases ("
                + "id integer primary key autoincrement,"
                + "title text,"
                + "time text,"
                + "content text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
