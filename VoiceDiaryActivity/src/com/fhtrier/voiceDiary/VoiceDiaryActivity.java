package com.fhtrier.voiceDiary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.holoeverywhere.widget.Toast;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.fhtrier.voiceDiary.DialogPasswordPermission.PasswordDialogFragmentListener;

public class VoiceDiaryActivity extends SherlockActivity  implements PasswordDialogFragmentListener 
{
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public boolean isAdmin;
	public String  userID;
	DialogPasswordPermission pwdPermissionDlg;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_voice_diary);

		LayoutInflater inflater = this.getLayoutInflater();

		this.isAdmin = this.getIntent().getBooleanExtra("admin", false);

		if(!this.isAdmin)
		{
			ViewGroup viewGroup = (ViewGroup) this.findViewById(R.id.voice_diary_activity_view);
			inflater.inflate(R.layout.view_voice_diary_home_user, viewGroup);

			((Button) findViewById(R.id.voice_diary_home_record)).setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(VoiceDiaryActivity.this, RecordActivity.class); 
					VoiceDiaryActivity.this.startActivity(intent);
				}
			});


			this.getSupportActionBar().setIcon(R.drawable.actionbar_logo);
			this.getSupportActionBar().setTitle("");


			((Button) findViewById(R.id.voice_diary_home_patient_domain)).setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(VoiceDiaryActivity.this, PatientActivity.class); 
					VoiceDiaryActivity.this.startActivity(intent);
					VoiceDiaryActivity.this.finish();
				}
			});

			((Button) findViewById(R.id.voice_diary_home_exit)).setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					showPasswordDialog();
					//VoiceDiaryActivity.this.logout();
					//VoiceDiaryActivity.this.finish();
				}
			});
		}
		else
		{
			Intent intent = new Intent(VoiceDiaryActivity.this, AdministrationActivity.class); 
			VoiceDiaryActivity.this.startActivity(intent);
			VoiceDiaryActivity.this.finish();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if(!MyApplication.checkPhoneName())
		{
			DialogPhoneName phoneNameDialog = new DialogPhoneName(this, this);
			phoneNameDialog.show();
		}else
		{
			if(!this.isAdmin)
			{
				Cursor c = MyApplication.getActiveUser();
				MyApplication.printCursor(MyApplication.getSqLiteDatabase().rawQuery("SELECT * FROM `user`;", null));
				MyApplication.printCursor(c);
				if (!c.moveToFirst())
				{
					Intent intent = new Intent(this, LoginActivity.class); 
					this.startActivity(intent);
					c.close();
					this.finish();
					return;
				}
				c.moveToFirst();
				if (c.isNull(0))
				{
					Intent intent = new Intent(this, SetFrequencyActivity.class);
					intent.putExtra("caller", VoiceDiaryActivity.class.getName());
					this.startActivity(intent);
					c.close();
					this.finish();
					return;
				}
				this.userID = c.getString(c.getColumnIndex("id_user"));
				this.getSupportActionBar().setTitle("\t" + this.userID);      
				if (!c.isNull(2))
				{
					try
					{
						((TextView) this.findViewById(R.id.upload)).setText(this.getString(R.string.upload, df.parse(c.getString(2))));
					}
					catch (ParseException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				Cursor maxDate = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT MAX(`date`) AS `date` FROM `protocolentry` WHERE `id_user` = '%s';", c.getString(1)), null);
				if (maxDate.moveToFirst())
				{
					if (!maxDate.isNull(0))
					{
						try
						{
							((TextView) this.findViewById(R.id.entry)).setText(this.getString(R.string.entry, df.parse(maxDate.getString(0))));
						}
						catch (ParseException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				maxDate.close();
				c.close();
			}
		}
	}
	public void logout(){
		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		for( WifiConfiguration i : list ) {
			wifiManager.disconnect();
			wifiManager.removeNetwork(i.networkId);}
		ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		while(mWifi.isConnected()) {
			mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);			
		}				
		Toast toast = Toast.makeText(getApplicationContext(), R.string.logout_successful, Toast.LENGTH_LONG);
		toast.show();
	}

	public void startAdmin()
	{
		Intent intent = new Intent(VoiceDiaryActivity.this, AdministrationActivity.class);
		VoiceDiaryActivity.this.startActivity(intent);
		VoiceDiaryActivity.this.finish();
	}
	public boolean i2b(Double intValue)
	{
		return (intValue != 0);
	}

	@SuppressLint("NewApi") public void showPasswordDialog()
	{
		DialogFragment pwdPermissionDlg = new DialogPasswordPermission();
		pwdPermissionDlg.show(getFragmentManager(), "dialog");

	}

	@Override
	public void onReturnValue(String Password) {
		if(MyApplication.isPassword(Password))
		{
			MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user` SET `offline_login`='%d' WHERE `id_user`='%s';", 0,this.userID));	
			
			this.userID = null;
			Intent intent = new Intent(VoiceDiaryActivity.this, VoiceDiaryActivity.class);
			VoiceDiaryActivity.this.startActivity(intent);
			VoiceDiaryActivity.this.finish();
		}
		else
		{
			Toast toast = Toast.makeText(getApplicationContext(), this.getString(R.string.wrong_password), Toast.LENGTH_LONG);
			toast.show();
			showPasswordDialog();
		}
	}
	@Override
	public void onAbort() {
		//onResume();
	}
}
