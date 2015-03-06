package com.fhtrier.voiceDiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.view.View;

public class DialogAddMasterPassword extends AlertDialog {
	EditText password;
	Context  context;
	public DialogAddMasterPassword(Context context) {		
		super(context);
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.password_dialog, null);
		setView(promptsView);
		setTitle(R.string.master_password);
		this.context = context;
		
		setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(R.string.button_cancel) , (new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// this will never be called
			}
		}));
		setButton(AlertDialog.BUTTON_NEGATIVE, context.getResources().getString(R.string.button_add) , (new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// this will never be called
			}
		}));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//------------------------------------------------------------------------------------------------------------------------------------//
		super.onCreate(savedInstanceState);
		//------------------------------------------------------------------------------------------------------------------------------------//
		this.password    = (EditText)findViewById(R.id.get_admin_password);
		//------------------------------------------------------------------------------------------------------------------------------------//
		getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MyApplication.insertMasterPassword(DialogAddMasterPassword.this.password.getText().toString());
				dismiss();
			}
		});
		getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
}
