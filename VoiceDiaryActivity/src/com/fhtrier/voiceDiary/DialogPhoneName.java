package com.fhtrier.voiceDiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.View;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class DialogPhoneName extends AlertDialog {
	Spinner            phoneSettings_letters;
	Spinner            phoneSettings_numbers;
	String             openSettingsLetter = null;
	String             openSettingsNumber = null;
	Context context;
	VoiceDiaryActivity vda;

	public DialogPhoneName(Context context,VoiceDiaryActivity vda) {
		super(context);
		this.context = context;
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.activity_open, null);
		setTitle(context.getString(R.string.open_phone_name));
		setView(promptsView);
		setCancelable(false);
		this.vda = vda;
		setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.button_set) , (new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// this will never be called
			}
		}));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.phoneSettings_letters     = (Spinner) findViewById(R.id.open_spinner_letters);
		this.phoneSettings_numbers     = (Spinner) findViewById(R.id.open_spinner_numbers);

		
		    
		ArrayAdapter<String> numbers   = new ArrayAdapter<String>(this.getContext(), R.layout.open_spinner_item, Values.numbers);
		ArrayAdapter<String> letters   = new ArrayAdapter<String>(this.getContext(), R.layout.open_spinner_item, Values.letters);
		this.phoneSettings_letters.setAdapter(letters);
		this.phoneSettings_numbers.setAdapter(numbers);

		this.phoneSettings_letters.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> arg0,
					View arg1, int arg2, long arg3) {
				DialogPhoneName.this.openSettingsLetter = DialogPhoneName.this.phoneSettings_letters.getSelectedItem().toString();;
			}
			@Override
			public void onNothingSelected(android.widget.AdapterView<?> arg0) {
				Toast.makeText(DialogPhoneName.this.context, R.string.nothing_selected ,
						Toast.LENGTH_SHORT).show();
			}
		});

		this.phoneSettings_numbers.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> arg0,
					View arg1, int arg2, long arg3) {
				DialogPhoneName.this.openSettingsNumber = DialogPhoneName.this.phoneSettings_numbers.getSelectedItem().toString();
			}
			@Override
			public void onNothingSelected(android.widget.AdapterView<?> arg0) {
				Toast.makeText(DialogPhoneName.this.context, R.string.nothing_selected ,
						Toast.LENGTH_SHORT).show();
			}
		});
		
		getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(DialogPhoneName.this.openSettingsLetter==null||DialogPhoneName.this.openSettingsNumber==null)
				{
					Toast toast = Toast.makeText(DialogPhoneName.this.context, R.string.select_value, Toast.LENGTH_LONG);
					toast.show();
				}else
				{
					MyApplication.setPhoneName(DialogPhoneName.this.openSettingsLetter+DialogPhoneName.this.openSettingsNumber);
					 DialogPhoneName.this.vda.onResume();
					dismiss();
				}

			}
		});
	}

}