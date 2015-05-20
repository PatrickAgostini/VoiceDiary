package com.fhtrier.voiceDiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class DialogEduroamConfigUser extends AlertDialog {

	Spinner  spinner;
	EditText userID;
	EditText password;
	EditText email;    
	Context  context;
	String user2Config;
	DialogEduroam dialogEduroam;
	
public DialogEduroamConfigUser(Context context, DialogEduroam dialogEduroam) {

	super(context);
	LayoutInflater li = LayoutInflater.from(context);
	
	View promptsView = li.inflate(R.layout.dialog_eduroam_config, null);
	setView(promptsView);
	
	this.dialogEduroam = dialogEduroam;

	setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.button_config) , (new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// this will never be called
		}
	}));
	setButton(AlertDialog.BUTTON_NEGATIVE,context.getString(R.string.button_cancel) , (new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// this will never be called
		}
	}));
	return;
}

@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	
	this.spinner  = (Spinner) findViewById(R.id.eduroam_config_select_userID);
	this.userID   = (EditText) findViewById(R.id.eduroam_config_userID);
	this.email    = (EditText) findViewById(R.id.eduroam_config_email);
	this.password = (EditText) findViewById(R.id.eduroam_config_password);
	//-------------------------------------------------------------------------------------------------------------------------------------//
	// Set Spinner
	setSpinner();
	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		@Override
		public void onItemSelected(android.widget.AdapterView<?> arg0,
				View arg1, int arg2, long arg3) {
			DialogEduroamConfigUser.this.user2Config = DialogEduroamConfigUser.this.spinner.getSelectedItem().toString();
			String[] account = MyApplication.getEduroamAccount(DialogEduroamConfigUser.this.user2Config);
			DialogEduroamConfigUser.this.userID.setText(account[0]);
			DialogEduroamConfigUser.this.email.setText(account[1]);
			DialogEduroamConfigUser.this.password.setText(account[2]);
		}
		@Override
		public void onNothingSelected(android.widget.AdapterView<?> arg0) {
			Toast.makeText(DialogEduroamConfigUser.this.context, R.string.nothing_selected ,
					Toast.LENGTH_SHORT).show();
		}
	});
	//-------------------------------------------------------------------------------------------------------------------------------------//
	getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String userID   = DialogEduroamConfigUser.this.userID.getText().toString();
			String email    = DialogEduroamConfigUser.this.email.getText().toString();
			String password = DialogEduroamConfigUser.this.password.getText().toString();
			if(userID.equals("")||email.equals("")||password.equals(""))
			{
				Toast toast = Toast.makeText(DialogEduroamConfigUser.this.dialogEduroam.rootContext, R.string.missing_field, Toast.LENGTH_LONG);
				toast.show();	
			}
			else
			{	
				MyApplication.updateEduroamAccount(userID, email, password,DialogEduroamConfigUser.this.user2Config);
				DialogEduroamConfigUser.this.dialogEduroam.setSpinner();
				dismiss();
			}
		}
	});

	getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			DialogEduroamConfigUser.this.dialogEduroam.setSpinner();
			dismiss();
		}
	});
}
public void setSpinner()
{
	MyApplication.setSpinner(this.getContext(), this.spinner, MyApplication.getUsers("userID","eduroam_accounts")); 
}
}
