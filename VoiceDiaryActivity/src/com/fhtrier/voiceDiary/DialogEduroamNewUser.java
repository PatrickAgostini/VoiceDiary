package com.fhtrier.voiceDiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DialogEduroamNewUser extends AlertDialog{
	
	Context  context;
	
	EditText userID;
	EditText email;
	EditText password;
	DialogEduroam dialogEduroam;
	
	public DialogEduroamNewUser(Context context, DialogEduroam dialogEduroam){
		
		super(context);
		// Init Layout
		LayoutInflater li = LayoutInflater.from(context);		
		View promptsView = li.inflate(R.layout.dialog_eduroam_add_user, null);
		setView(promptsView);
		setTitle(R.string.eduroam_add_user_title);
		
		this.dialogEduroam = dialogEduroam;
		
		//---------------------------------------------------------------------------------------------------------------------------//
		// Add Button
		setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.button_add) , (new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ....
			}
		}));
		//---------------------------------------------------------------------------------------------------------------------------//		
		// Cancel Button
		setButton(AlertDialog.BUTTON_NEGATIVE,context.getString(R.string.button_cancel) , (new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ....
			}
		}));
		//---------------------------------------------------------------------------------------------------------------------------//
		return;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//---------------------------------------------------------------------------------------------------------------------------//		
		// Get Items
		this.userID   = (EditText) findViewById(R.id.eduroam_add_user_ID);
		this.password = (EditText) findViewById(R.id.eduroam_add_user_password);
		this.email    = (EditText) findViewById(R.id.eduroam_add_user_email);
		//---------------------------------------------------------------------------------------------------------------------------//		
		// Define onClick Action
		// Add Button
		getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//-------------------------------------------------------------------------------------------------------------------//
				String userID   = DialogEduroamNewUser.this.userID.getText().toString();
				String email    = DialogEduroamNewUser.this.email.getText().toString();
				String password = DialogEduroamNewUser.this.password.getText().toString();
				//-------------------------------------------------------------------------------------------------------------------//
				if(userID.equals("")||email.equals("")||password.equals(""))
				{
					Toast toast = Toast.makeText(DialogEduroamNewUser.this.dialogEduroam.rootContext, R.string.missing_field, Toast.LENGTH_LONG);
					toast.show();	
				}
				else
				{
					MyApplication.insertEduroamAccount(userID, email, password);
					DialogEduroamNewUser.this.dialogEduroam.setSpinner();
					dismiss();		
				}
			}
		});
		//---------------------------------------------------------------------------------------------------------------------------//		
		// Cancel Button
		getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

}
