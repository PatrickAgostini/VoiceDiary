package com.fhtrier.voiceDiary.command;

import android.database.sqlite.SQLiteDatabase;

public interface Command
{
    public boolean askQuestionType(SQLiteDatabase sqLiteDatabase);
}
