package com.fhtrier.voiceDiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.View;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class DialogUserList extends AlertDialog {
	EditText editText;
	Spinner  userID;
	Context  context;
	public DialogUserList(Context context) {		
		super(context);
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.dialog_user_list, null);
		setView(promptsView);
		setTitle(R.string.user_list);
		this.context = context;
		
		setButton(AlertDialog.BUTTON_NEUTRAL, context.getResources().getString(R.string.button_ok) , (new DialogInterface.OnClickListener() {
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
		this.userID     = (Spinner) findViewById(R.id.user_list_userID);
		this.editText    = (EditText)findViewById(R.id.user_list_password);
		this.editText.setKeyListener(null);
		//------------------------------------------------------------------------------------------------------------------------------------//
		MyApplication.setSpinner(this.getContext(), this.userID,MyApplication.getUsers("user", "registredUsers"));
		this.userID.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> arg0,
					View arg1, int arg2, long arg3) {
				String selected_val = userID.getSelectedItem().toString();
				if(!selected_val.equals(" "))
				{
				String[] user = MyApplication.getUser(selected_val);	 
				String str      = DialogUserList.this.context.getString(R.string.show_data_login_string) + " " + selected_val + "\n" + DialogUserList.this.context.getString(R.string.show_data_password_string) + " " + user[1];
				DialogUserList.this.editText.setText(str);
				}
			}
			@Override
			public void onNothingSelected(android.widget.AdapterView<?> arg0) {
				Toast.makeText(DialogUserList.this.context, R.string.nothing_selected ,
						Toast.LENGTH_SHORT).show();
			}
		});
		getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
}
