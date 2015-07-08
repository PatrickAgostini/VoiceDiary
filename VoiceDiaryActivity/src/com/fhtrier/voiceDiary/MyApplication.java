package com.fhtrier.voiceDiary;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.holoeverywhere.util.ArrayUtils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.ParseException;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Context;
import android.database.CursorJoiner;

@SuppressLint("SimpleDateFormat")
public class MyApplication extends Application
{
	private static DatabaseHelper databaseHelper;
	private static SQLiteDatabase sqLiteDatabase;

	public static final int Wifi_Not_Exist = 0;
	public static final int Wifi_Connection_Timedout = 1;
	public static final int Wifi_Connected = 2;
	public static final int Wifi_Logout_Successful = 3;
	public static final int Wrong_Password = 4;
	public static final int No_User = 5;
	public static final int Wifi_TimeOut = 6;
	
	
	public static boolean weckerOnOff = false;
	public static int startH = 0;
	public static int startM = 0;
	public static int stopH = 0;
	public static int stopM = 0;
	public static int interval = 1;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		databaseHelper = new DatabaseHelper(this.getApplicationContext());
		sqLiteDatabase = databaseHelper.getDatabase();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////     Database Handling     /////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	public static DatabaseHelper getDatabaseHelper()
	{
		return databaseHelper;
	}

	public static SQLiteDatabase getSqLiteDatabase()
	{
		return sqLiteDatabase;
	}

	@Override
	public void onTerminate()
	{
		if (sqLiteDatabase != null)
		{
			sqLiteDatabase.close();
		}

		if (databaseHelper != null)
		{
			databaseHelper.close();
		}
		super.onTerminate();
	}

	public static void printCursor(Cursor c)
	{
		c.moveToPosition(-1);
		int columnCount = c.getColumnCount();
		String tag = MyApplication.class.getName();
		String columnString = "";

		for (int i = 0; i < columnCount; i++)
		{
			columnString += c.getColumnName(i) + "\t | \t";
		}

		Log.i(tag, columnString);

		while (c.moveToNext())
		{
			columnString = "";

			for (int i = 0; i < columnCount; ++i)
			{
				columnString += c.getString(i) + "\t | \t";
			}

			Log.i(tag, columnString);
		}

		c.moveToPosition(-1);
		Log.i(tag, c.toString());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////     User Name Handling    /////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	public static String createPassword()
	{
		return getRandomPwd(4);
	}

	public static boolean checkPhoneName()
	{
		Cursor c = MyApplication.getSqLiteDatabase().rawQuery("SELECT * FROM `phone_info`;", null);
		return c.moveToFirst();
	}
	
	public static void setPhoneName(String name)
	{
		MyApplication.getSqLiteDatabase().rawQuery("DELETE FROM `phone_info` ;", null);
		MyApplication.getSqLiteDatabase().execSQL(String.format("INSERT INTO `phone_info` VALUES ('%s');", name));
	}

	public static void insertMasterPassword(String password)
	{
		MyApplication.getSqLiteDatabase().execSQL(String.format("INSERT INTO `admin_passwords` VALUES ('%s');", passwordEncrypt(password)));
	}

	public static String getPhoneName()
	{
		Cursor c = MyApplication.getSqLiteDatabase().rawQuery("SELECT * FROM `phone_info` ;", null);
		c.moveToFirst();
		return c.getString(c.getColumnIndex("phone_name"));
	}

	public static String getExternalId(String ID)
	{
		return getPhoneName() + "_" + ID;
	}

	public static boolean checkForAdmin()
	{
		Cursor c = MyApplication.getSqLiteDatabase().rawQuery("SELECT * FROM `administration_accounts`;", null);
		return c.moveToFirst();
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////      Password Handling    /////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	public static String[] encrypt(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] salt = new byte[8];
		new Random().nextBytes(salt);
		return encrypt(password, salt);
	}

	public static String[] encrypt(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, 80);
		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = f.generateSecret(spec).getEncoded();
		String passHash = Base64.encodeToString(hash,0);
		String saltString = Base64.encodeToString(salt,0);
		return new String[] { passHash, saltString };
	}

	public static Boolean checkPassword(String password, String hash, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String[] encrypted = encrypt(password, Base64.decode(salt,0));
		return encrypted[0].equals(hash);
	}

	public static String passwordEncrypt(String password)
	{
		byte[] data;
		String base64=null;
		try {
			data = password.getBytes("UTF-8");
			base64 = Base64.encodeToString(data, Base64.DEFAULT);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return base64;
	}

	public static String passwordDecrypt(String password)
	{
		String text = null;
		byte[] data = null;
		try {
			data = Base64.decode(password, Base64.DEFAULT);
			text = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return text;
	}

	public static String getRandomPwd(int i)
	{
		String randomStr="";
		Random rnd = new Random();
		String base="ABCDEFGHIJKLMNPQRSTUVXYZabcdefghijklmnpqrstuvwxyz123456789";

		for(int j=0;j<i;++j)
		{
			randomStr += base.charAt(rnd.nextInt(base.length()));
		}
		return randomStr;
	}

	public static void insertEduroamAccount(String userID, String email, String password)
	{
		MyApplication.getSqLiteDatabase().execSQL(String.format("INSERT INTO `eduroam_accounts` VALUES ('%s', '%s', '%s');", userID, email,MyApplication.passwordEncrypt(password)));

	}
	public static String[] getEduroamAccount(String userID)
	{
		String[] account = new String[3];
		Cursor c = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT * FROM `eduroam_accounts` WHERE `userID`='%s';",userID), null);
		if(c.moveToFirst())
		{
			account[0] = c.getString(0);
			account[1] = c.getString(1);
			account[2] = MyApplication.passwordDecrypt(c.getString(2));
			return account;
		}else
		{
			return null;
		}
	}
	public static void updateEduroamAccount(String userID,String email, String password, String user2Config)
	{
		MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `eduroam_accounts` SET `userID`='%s', `email`='%s', `password`='%s' WHERE `userID`='%s';", userID, email, MyApplication.passwordEncrypt(password), user2Config));		
	}
	public static void deleteDataBaseEntry(String Table, String Column, String columnValue)
	{
		MyApplication.getSqLiteDatabase().execSQL(String.format("DELETE FROM `eduroam_accounts` WHERE `userID`='%s';", columnValue));
	}
	
	public static Cursor getActiveUser()
	{
		return MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `frequency`, `id_user`, `last_upload` FROM `user` WHERE `offline_login`!='%d';",0), null);
	}
	
	public static String getActiveUser2()
	{
		Cursor c = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `frequency`, `id_user`, `last_upload` FROM `user` WHERE `offline_login`!='%d';",0), null);
		
		if(c.moveToFirst())
		{
			return c.getString(c.getColumnIndex("id_user"));
		}
		return null;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void showWifiError(Context context, int Problem){

		if(Problem==Wifi_Not_Exist)
		{
			Toast toast = Toast.makeText(context, R.string.wifi_not_exist, Toast.LENGTH_LONG);
			toast.show();	
			return;
		}
		if(Problem==Wifi_Connection_Timedout)
		{
			Toast toast = Toast.makeText(context, R.string.wifi_timedout, Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		if(Problem==Wifi_Connected)
		{
			Toast toast = Toast.makeText(context, R.string.wifi_connected, Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		if(Problem==Wifi_Logout_Successful)
		{
			Toast toast = Toast.makeText(context, R.string.logout_successful, Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		if(Problem==Wrong_Password)
		{
			Toast toast = Toast.makeText(context, R.string.wrong_password, Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		if(Problem==No_User)
		{
			Toast toast = Toast.makeText(context, R.string.select_user, Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		if(Problem==Wifi_TimeOut)
		{
			Toast toast = Toast.makeText(context, R.string.wifi_timedout, Toast.LENGTH_LONG);
			toast.show();
			return;
		}
	}

	public static boolean checkWifi(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static String[] getUsers(String column, String table)
	{
		String[] res = null; 
		Cursor c     = null;
		c= MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT * FROM '%s';",table), null);

		int count = c.getCount();
		if(count>0)
		{
			c.moveToFirst();
			res = new String[count];
			for(int i =0;i<count;++i)
			{
				res[i] = c.getString(c.getColumnIndex(column));
				c.moveToNext();
			}
		}
		c.close();
		return res;
	}

	public static String[] getUser(String userID)
	{
		Cursor c        = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT * FROM registredUsers WHERE `user`= '%s';", userID), null);
		if(c.moveToFirst())
		{
			String[] user = new String[2];
			user[0] = c.getString(0);
			user[1] = MyApplication.passwordDecrypt(c.getString(1));
			return user;
		}else
		{
			return null;
		}
	}

	public static String[] getUsers2Register()
	{
		Cursor users2register = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `id_user` FROM user WHERE `offline_registration`> '%d';", 0), null);
		if(users2register!=null)
		{
			String[] u2reg = new String[users2register.getCount()];
			for(int i=0;i<users2register.getCount();i++)
			{
				users2register.moveToNext();
				u2reg[i] =  users2register.getString(0);
			}
			return u2reg;
		}
		return null;		
	}
	
	public static String[] getUsers2Update()
	{
		//printCursor( MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT * FROM protocolentry ;"), null));
		//MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user` SET `last_upload`='%s' WHERE `id_user`='%s';", "2015-05-23 11:01:47", "KF031192"));		
		//printCursor( MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT * FROM user WHERE `offline_login`= '%d' ;", 0), null));
	
		Cursor users2update = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `id_user`FROM user WHERE `session_id`= '%s' ;", "OfflineLogin"), null);
		//MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user` SET `offline_login`='%d' WHERE `id_user`='%s';",1, "WJ070785"));	
		//Cursor users2update = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `id_user`FROM user WHERE `offline_login`= '%d' ;", 1), null);
		//users2update.moveToFirst();
		//String[] user = new String[1];
		//user[0] = users2update.getString(0);
		//return user;
		
		if(users2update!=null)
		{
			String[] u2upd = new String[users2update.getCount()];
			for(int i=0;i<users2update.getCount();i++)
			{
				users2update.moveToNext();
				u2upd[i] =  users2update.getString(0);
			}
			return u2upd;
		}
		return null;	
	}
	
	public static String[] getUsers2Update2()
	{
		printCursor(MyApplication.getSqLiteDatabase().rawQuery("SELECT * FROM `protocolentry`;", null));
		Cursor users2update = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `id_user`FROM user WHERE `offline_login`= '%d' and `id_user`!='Admin';", 1), null);
		if(users2update!=null)
		{
			String[] u2upd = new String[users2update.getCount()];
			for(int i=0;i<users2update.getCount();i++)
			{
				users2update.moveToNext();
				u2upd[i] =  users2update.getString(0);
			}
			return u2upd;
		}
		return null;		
	}
	
	public static void updateUser(String userID)
	{
		MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user` SET `session_id` = '%s', `offline_login` = '%d' WHERE `id_user` = '%s';", "", 0, userID));
	}

	public static PatientData getPatientData(String userID)
	{
		Cursor c = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT * FROM `user` WHERE `id_user`= '%s';", userID), null);
		if(c.moveToFirst())
		{
			String[] account = getUser(userID);
			return  new PatientData(account[0], account[1],i2b(c.getString(c.getColumnIndex("male"))),i2b(c.getString(c.getColumnIndex("smoker"))));
		}
		return null;
	}
	public static Map<String,String[]> getRecDates() throws java.text.ParseException
	{
		String userID = MyApplication.getActiveUser2();
		Cursor c = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `date` FROM `protocolentry` WHERE `id_user`= '%s';", userID), null);

		if(c.getCount()>0)
		{
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat format3 = new SimpleDateFormat("hh:mm:ss");
			
			String[] date = new String[c.getCount()];
			String[] time = new String[c.getCount()];
			int i = 0;
			while(c.moveToNext())
			{
				Date date1         = format.parse(c.getString(0));
				date[i]           = format2.format(date1);
				time[i]           = format3.format(date1);
				i++;				
			}
			
			Map<String,String[]> mDates =  new TreeMap<String,String[]>();
			List<String> mTimes = new ArrayList<String>();
			String first = date[0];
			
			for(i=0;i<c.getCount();i++)
			{
				if(first.equals(date[i]))
				{
					mTimes.add(time[i]);
				}
				else
				{
					mDates.put(first, mTimes.toArray(new String[mTimes.size()]));
					mTimes.clear();
					first = date[i];
					i--;
				}
			}
			if(mTimes.size()>0)
			{
				mDates.put(first, mTimes.toArray(new String[mTimes.size()]));
			}
			return mDates;
		}

		return null;
	}
	public static void setRegistered(String userID)
	{
		MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user` SET `offline_registration` = '%d' WHERE `id_user` = '%s';", 0, userID));
	}

	public static String[] getAllUsers()
	{
		return getUsers("user", "registredUsers");
	}

	public static boolean isPassword(String Password)
	{
		boolean correct = false;
		if(!Password.equals(passwordDecrypt(Values.adminPassword)))
		{
			Cursor c = MyApplication.getSqLiteDatabase().rawQuery("SELECT * FROM admin_passwords;", null);
			while(c.moveToNext())
			{
				if(MyApplication.passwordDecrypt(c.getString(0)).equals(Password))
				{
					correct = true;
				}
			}
			c.close();
		}else
		{
			correct = true;
		}
		return correct;
	}

	public static String getPassword(String user)
	{
		Cursor c= MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `password` FROM `registredUsers` WHERE `user`='%s';",user), null);
		c.moveToFirst();
		return MyApplication.passwordDecrypt(c.getString(c.getColumnIndex("password")));
	}

	public static void setSpinner(Context context, Spinner spinner, String[] table)
	{
		if(table!=null)
		{
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, table);
			spinner.setAdapter(adapter);
		}
	}
	
	public static void setReminderSettings(String id_user, int startH, int startM, int stopH, int stopM, int interval)
	{
		Cursor c= MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT * FROM `reminder_settings` WHERE `id_user`='%s';",id_user), null);
		if(c.moveToFirst())
		{
			MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `reminder_settings` SET `startH` = '%d', `startM` = '%d', `stopH` = '%d', `stopM` = '%d', `interval` = '%d' WHERE `id_user` = '%s';", startH, startM, stopH, stopM, interval, id_user));
			
		}else
		{
			MyApplication.getSqLiteDatabase().execSQL(String.format("INSERT INTO `reminder_settings` (%s,%d,%d,%d,%d,%d);", id_user, startH, startM, stopH, stopM, interval));
			
		}
	}
	
	public static String[] getReminderSettings(String id_user)
	{
		String[] reminderSettings = new String[5];
		Cursor c= MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT * FROM `reminder_settings` WHERE `id_user`='%s';",id_user), null);
		if(c.moveToFirst())
		{
			reminderSettings[0] = c.getString(1);
			reminderSettings[1] = c.getString(2);
			reminderSettings[2] = c.getString(3);
			reminderSettings[3] = c.getString(4);
			reminderSettings[4] = c.getString(5);
			return reminderSettings;
		}
		return null;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static boolean i2b(String boolStrValue)
	{
		return (boolStrValue.equals("true"));
	}
}
