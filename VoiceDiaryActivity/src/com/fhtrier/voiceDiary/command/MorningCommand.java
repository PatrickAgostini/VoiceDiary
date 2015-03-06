package com.fhtrier.voiceDiary.command;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fhtrier.voiceDiary.MyApplication;

public class MorningCommand implements Command
{
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US); 

    @Override
    public boolean askQuestionType(SQLiteDatabase sqLiteDatabase)
    {
        Cursor c = MyApplication.getSqLiteDatabase().rawQuery("SELECT `date` FROM `protocolentry` WHERE `id_protocolentry` = (SELECT MAX(`p`.`id_protocolentry`) FROM `protocolentry` AS `p`, `user` AS `u` WHERE `u`.`session_id` NOT NULL AND `p`.`id_user` = `u`.`id_user`) GROUP BY `date`", null);
        if (c.getCount() == 0)
        {
            return true;
        }
        c.moveToFirst();
        Date lastEntryDate = null;
        try
        {
            lastEntryDate = dateFormat.parse(c.getString(0));
        }
        catch (ParseException e)
        {
            return false;
        }
        
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());
        
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);

        Log.d("test", dateFormat.format(new Date(today.getTimeInMillis())));
        Log.d("test", dateFormat.format(lastEntryDate));
        
        return (today.getTimeInMillis() > lastEntryDate.getTime());
    }
}
