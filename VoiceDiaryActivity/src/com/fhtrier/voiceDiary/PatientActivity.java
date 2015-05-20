package com.fhtrier.voiceDiary;

import java.util.Locale;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class PatientActivity  extends SherlockPreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        this.getSupportActionBar().setIcon(R.drawable.actionbar_logo);
        this.getSupportActionBar().setTitle("");
        
        this.addPreferencesFromResource(R.xml.activity_patient_domain);
   
        
        findPreference("language2").setOnPreferenceChangeListener(new OnPreferenceChangeListener()
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
                
                PatientActivity.this.getBaseContext().getResources().updateConfiguration(config, null);

                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                PatientActivity.this.startActivity(intent);
                PatientActivity.this.finish();
                return true;
            }
        });
        

        findPreference("set_frequency2").setOnPreferenceClickListener(new OnPreferenceClickListener()
        {

            @Override
            public boolean onPreferenceClick(Preference preference)
            {

                Intent intent = new Intent(PatientActivity.this, SetFrequencyActivity.class);
                intent.putExtra("caller", PatientActivity.class.getName());
                PatientActivity.this.startActivity(intent);
                //PatientActivity.this.finish();

                return true;
            }
        });
        findPreference("recordingDiary2").setOnPreferenceClickListener(new OnPreferenceClickListener()
        {

            @Override
            public boolean onPreferenceClick(Preference preference)
            {

                RecordingDiary recDiary = new RecordingDiary(PatientActivity.this);
                recDiary.show();
                return true;
            }
        });
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(PatientActivity.this,VoiceDiaryActivity.class);
		startActivity(intent);
		PatientActivity.this.finish();		
	}

}
