
package com.example.wordcounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "WordCounter.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "saved_texts";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "text_content TEXT, " +
                "word_count INTEGER, " +
                "character_count INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertText(String text, int words, int characters) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("text_content", text);
        values.put("word_count", words);
        values.put("character_count", characters);

        return db.insert(TABLE_NAME, null, values);
    }

    public Cursor getAllTexts() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC",
                null);
    }

    public int updateText(int id, String text,
                          int words, int characters) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("text_content", text);
        values.put("word_count", words);
        values.put("character_count", characters);

        return db.update(TABLE_NAME, values, "id=?",
                new String[]{String.valueOf(id)});
    }

    public int deleteText(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, "id=?",
                new String[]{String.valueOf(id)});
    }
}
