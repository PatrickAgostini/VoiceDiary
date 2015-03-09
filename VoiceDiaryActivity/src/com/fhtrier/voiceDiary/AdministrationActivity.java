package com.fhtrier.voiceDiary;

import java.util.Locale;

import org.holoeverywhere.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;

public class AdministrationActivity extends SherlockPreferenceActivity{

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getSupportActionBar().setIcon(R.drawable.actionbar_logo);
		this.getSupportActionBar().setTitle("");

		this.addPreferencesFromResource(R.xml.activity_doctor_domain);

		findPreference("logout").setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				Intent intent = new Intent(AdministrationActivity.this, VoiceDiaryActivity.class);
				AdministrationActivity.this.startActivity(intent);
				AdministrationActivity.this.finish();
				return true;
			}
		});


		findPreference("synchronize").setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				if(AdministrationActivity.this.checkWifi())
				{
					Cursor c = MyApplication.getSqLiteDatabase().rawQuery("SELECT `id_user` FROM `user` WHERE `session_id` NOT NULL;", null);
					if(c.moveToFirst())
					{
						new SyncThread(AdministrationActivity.this);
					}
				}
				else
				{
					Toast toast = Toast.makeText(AdministrationActivity.this, R.string.synchronize_wifi_off, Toast.LENGTH_LONG);
					toast.show();	
				}

				return true;
			}
		});


		findPreference("user_list").setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				new DialogUserList(AdministrationActivity.this).show();
				return true;
			}
		});

		findPreference("new_password").setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				new DialogAddMasterPassword(AdministrationActivity.this).show();
				return true;
			}
		});

		findPreference("eduroam").setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				new DialogEduroam(AdministrationActivity.this).show();
				return true;
			}
		});

		findPreference("language").setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				String languageToLoad = (String)newValue;

				Locale locale;
				if (languageToLoad.equals("auto"))
				{ 
					locale = new Locale(System.getProperty("user.language"));
				}
				else
				{
					locale = new Locale(languageToLoad);
				}

				Locale.setDefault(locale);
				Configuration config = new Configuration();
				config.locale = locale;

				AdministrationActivity.this.getBaseContext().getResources().updateConfiguration(config, null);

				Intent intent = getIntent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				AdministrationActivity.this.startActivity(intent);
				AdministrationActivity.this.finish();
				return true;
			}
		});
		
		findPreference("mediaPlayer").setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				new DialogMediaPlayer(AdministrationActivity.this).show();
				return true;
			}
		});
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(AdministrationActivity.this,VoiceDiaryActivity.class);
		startActivity(intent);
		AdministrationActivity.this.finish();		
	}

	public void Message(int i)
	{
		if(i == 0)
		{
			Toast toast = Toast.makeText(this, R.string.synchronize_ok, Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		if(i == 1)
		{
			Toast toast = Toast.makeText(this, R.string.synchronize_not_ok, Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		if(i == 2)
		{
			Toast toast = Toast.makeText(this, R.string.synchronize_time_out, Toast.LENGTH_LONG);
			toast.show();
			return;
		}
	}

	public boolean checkWifi()
	{
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

}
