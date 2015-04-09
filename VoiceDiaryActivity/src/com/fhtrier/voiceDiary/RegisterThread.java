package com.fhtrier.voiceDiary;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;

public class RegisterThread extends Thread
{
	private final RegisterActivity registerActivity;
	public double index;
	
	String  user;
	String  password;
	boolean male;
	boolean smoker;
	
	@SuppressLint("SimpleDateFormat")
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public RegisterThread(RegisterActivity registerActivity, String user, String password, boolean male,boolean smoker)
	{
		this.registerActivity = registerActivity;
		this.user             = user;
		this.password         = password;
		this.male             = male;
		this.smoker           = smoker;
		this.start();
	}
	@Override
	public void run()
	{
			MyApplication.getSqLiteDatabase().execSQL(String.format("INSERT INTO `user` VALUES ('%s', '%s', NULL, '%s', '%s', '%d', '%d', '%d');", this.user, "OfflineLogin", this.smoker, this.male, 0, 1 , 1));
			MyApplication.getSqLiteDatabase().execSQL(String.format("INSERT INTO `user_protocolenty` VALUES ('%s', '0', '-1');", this.user));
			MyApplication.getSqLiteDatabase().execSQL(String.format("INSERT INTO `registredUsers` VALUES ('%s', '%s', '%s');", this.user, MyApplication.passwordEncrypt(this.password), df.format(new Date())));
			MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user` SET `session_id` = '%s' WHERE `id_user` = '%s';", "OfflineLogin", this.user));
			registerActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					registerActivity.register(RegisterActivity.SUCCESSFULLY);
				}
			});
	}
}
