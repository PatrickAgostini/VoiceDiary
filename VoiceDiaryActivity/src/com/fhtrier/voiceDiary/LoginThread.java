package com.fhtrier.voiceDiary;

import java.util.Locale;
import java.text.SimpleDateFormat;

public class LoginThread extends Thread
{
	public  String user;
	public  String password;

	private final LoginActivity loginActivity;

	@SuppressWarnings("unused")
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

	public LoginThread(final String user, final String password, final LoginActivity loginActivity)
	{
		this.user          = user;
		this.password      = password;
		this.loginActivity = loginActivity;
		this.start();
	}

	@Override
	public void run()
	{
		String[] user = MyApplication.getUser(this.user);

		if(user==null)
		{
			loginActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					loginActivity.localData(LoginActivity.USERNAME_ERROR);
				}
			});
			return;
		}
		if(user[1].equals(this.password))
		{
			MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user` SET `session_id` = '%s' WHERE `id_user` = '%s';", "OfflineLogin", user[0]));
			MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user` SET `offline_login` = '%d' WHERE `id_user` = '%s';", 1, user[0]));
			loginActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					loginActivity.login(true);
				}
			});
		}
		else
		{
			loginActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					loginActivity.localData(LoginActivity.PASSWORD_ERROR);
				}
			});
		}		
	}
}
