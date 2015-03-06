package com.fhtrier.voiceDiary.command;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SmokerCommand implements Command
{

    @Override
    public boolean askQuestionType(SQLiteDatabase sqLiteDatabase)
    {
        Cursor smoker = sqLiteDatabase.rawQuery("SELECT `smoker` FROM `user` WHERE `session_id` IS NOT NULL;", null);
        smoker.moveToFirst();
        boolean value = Boolean.valueOf(smoker.getString(0));
        smoker.close();
        return value;
    }
}
