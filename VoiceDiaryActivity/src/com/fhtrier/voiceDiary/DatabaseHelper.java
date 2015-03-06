package com.fhtrier.voiceDiary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "userdata2.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TAG = DatabaseHelper.class.getName();

    private SQLiteDatabase sqliteDatabase;
    
    private Context context;

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase)
    {
        this.sqliteDatabase = sqliteDatabase;
        try
        {	          	
        	sqliteDatabase.execSQL("CREATE TABLE `eduroam_accounts` ( " +
             		"`userID` VARCHAR(200), " +
             		"`email` VARCHAR(200), " +
             		"`password` VARCHAR(200), " +
             		"PRIMARY KEY (`userID`)" +
             		");"); 
        	
        	sqliteDatabase.execSQL("CREATE TABLE `phone_info` ( " +
             		"`phone_name` VARCHAR(200) " +
             		");");
        	
        	sqliteDatabase.execSQL("CREATE TABLE `admin_passwords` ( " +
             		"`password` VARCHAR(200) " +
             		");");
        	
        	sqliteDatabase.execSQL("CREATE TABLE `registredUsers` ( " +
             		"`user` VARCHAR(200) , " +
             		"`password` VARCHAR(200) , " +
             		"`date` DATE NOT NULL,"+
             		"PRIMARY KEY (`user`)" +
             		");"); 
        	
        	sqliteDatabase.execSQL("CREATE TABLE `session` ( " +
             		"`id_user` VARCHAR(20) NOT NULL, " +
             		"`session_id` VARCHAR(200) NULL, " +
             		"`date` DATE NOT NULL, " +
             		"`updated` TINYINT(1) NOT NULL, " +
             		"PRIMARY KEY (`id_user`)" +
             		");");
        	
            sqliteDatabase.execSQL("CREATE TABLE `user` ( " +
            		"`id_user` VARCHAR(20) NOT NULL, " +
            		"`session_id` VARCHAR(200) NULL, " +
            		"`frequency` INT(11) NULL, " +
                    "`smoker` TINYINT(1) NOT NULL, " +
                    "`male` TINYINT(1) NOT NULL, " +
                    "`last_upload` DATETIME NULL, " +
                    "`offline_login` TINYINT(1) ,"+
                    "`offline_registration` TINYINT(1) ,"+
            		"PRIMARY KEY (`id_user`)" +
            		");");
            
            sqliteDatabase.execSQL("CREATE TABLE `questionnaire` (" +
            		"`id_questionnaire` INT(11) NOT NULL, " +
            		"`date` DATE NOT NULL, " +
            		"`language` VARCHAR(2) NOT NULL, " +
            		"`description` VARCHAR(60) NULL DEFAULT NULL, " +
            		"PRIMARY KEY (`id_questionnaire`)" +
            		");");
            
            sqliteDatabase.execSQL("CREATE TABLE `question` ( " +
            		"`id_question` INT(11) NOT NULL, " +
            		"`fk_questionnaire` INT(11) NOT NULL, " +
            		"`type` VARCHAR(2) NULL DEFAULT NULL, " +
            		"`question_text` VARCHAR(200) NOT NULL, " +
            		"PRIMARY KEY (`id_question`), " +
            		"CONSTRAINT `fk_question_questionnaire` FOREIGN KEY (`fk_questionnaire`) REFERENCES `questionnaire` (`id_questionnaire`)" +
            		");");
            
            sqliteDatabase.execSQL("CREATE TABLE `answer` ( " +
            		"`id_answer` INT(11) NOT NULL, " +
            		"`fk_question` INT(11) NOT NULL, " +
            		"`answer_text` VARCHAR(60) NOT NULL, " +
            		"`fk_next_question` INT(11) NULL DEFAULT NULL, " +
            		"PRIMARY KEY (`id_answer`), " +
            		"CONSTRAINT `fk_answer_question` FOREIGN KEY (`fk_question`) REFERENCES `question` (`id_question`), " +
            		"CONSTRAINT `fk_answer_next_question` FOREIGN KEY (`fk_next_question`) REFERENCES `question` (`id_question`)" +
            		");");
            
            sqliteDatabase.execSQL("CREATE TABLE `protocolentry` ( " +
            		"`id_user` VARCHAR(20) NOT NULL, " +
            		"`id_protocolentry` INT(11) NOT NULL, " +
            		"`date` DATETIME NOT NULL, " +
            		"PRIMARY KEY (`id_user`, `id_protocolentry`), " +
            		"CONSTRAINT `fk_prtocolentry_user` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`)" +
            		");");
            
            sqliteDatabase.execSQL("CREATE TABLE `record` ( " +
            		"`id_user` VARCHAR(20) NOT NULL, " +
            		"`id_protocolentry` INT(11) NOT NULL, " +
            		"`filename` VARCHAR(40) NOT NULL, " +
            		"`filesize` INT(11) NOT NULL, " +
            		"`wave` BLOB NOT NULL, " +
            		"PRIMARY KEY (`id_user`, `id_protocolentry`), " +
            		"CONSTRAINT `fk_record_protocolentry` FOREIGN KEY (`id_user`, `id_protocolentry`) REFERENCES `protocolentry` (`id_user`, `id_protocolentry`)" +
            		");");
            
            sqliteDatabase.execSQL("CREATE TABLE `rel_protocolentry_answer` ( " +
            		"`id_user` VARCHAR(20) NOT NULL, " +
            		"`id_protocolentry` INT(11) NOT NULL, " +
            		"`id_answer` INT(11) NOT NULL, " +
            		"PRIMARY KEY (`id_user`, `id_protocolentry`, `id_answer`), " +
            		"CONSTRAINT `fk_1` FOREIGN KEY (`id_user`, `id_protocolentry`) REFERENCES `protocolentry` (`id_user`, `id_protocolentry`), " +
            		"CONSTRAINT `fk_2` FOREIGN KEY (`id_answer`) REFERENCES `answer` (`id_answer`)" +
            		");");
            
            sqliteDatabase.execSQL("CREATE TABLE `user_protocolenty` ( " +
            		"`id_user` VARCHAR(20) NOT NULL, " +
            		"`next_protocolentry_id` INT(11) NOT NULL, " +
            		"`last_updated_protocolenty` INT(11) NOT NULL DEFAULT 0, " +
            		"PRIMARY KEY (`id_user`), " +
            		"CONSTRAINT `fk_user_protocolenty_user` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`)" +
            		");");
            
            sqliteDatabase.execSQL("CREATE TABLE `analyzedata` (" +
            		"`id_user` VARCHAR(20) NOT NULL, " +
            		"`param1` FLOAT(20,2) NULL, " +
            		"`param2` FLOAT(20,2) NULL, " +
            		"`param3` FLOAT(20,2) NULL, " +
            		"`param4` FLOAT(20,2) NULL, " +
            		"`param5` FLOAT(20,2) NULL, " +
            		"`param6` FLOAT(20,2) NULL, " +
            		"`date` DATETIME NOT NULL," +
            		"CONSTRAINT `fk_analyzedata_user` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`)" +
            		");");
                        
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open("data.d"), "ISO-8859-1"));
            
            String s;
            while ((s = reader.readLine()) != null)
            {
                try
                {
                    sqliteDatabase.execSQL(s);
                }
                catch (SQLiteException e)
                {
                    Log.e(TAG, s);
                    Log.e(TAG, e.toString());
                }
            }
            reader.close();
        }
        catch (SQLiteException e)
        {
            Log.e(TAG, "onCreate Database failed");
        }
        catch (IOException e)
        {
            Log.e(TAG, "Read file failed");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, int oldVersion, int newVersion)
    {
        this.sqliteDatabase = sqliteDatabase;
    }

    public SQLiteDatabase getDatabase()
    {
        if (this.sqliteDatabase == null)
        {
            sqliteDatabase = this.getWritableDatabase();
        }

        return sqliteDatabase;
    }
}
