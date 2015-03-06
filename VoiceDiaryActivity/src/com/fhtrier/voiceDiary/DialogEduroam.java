package com.fhtrier.voiceDiary;

import org.holoeverywhere.widget.Toast;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class DialogEduroam extends AlertDialog {

	Button   addUser;
	Button   configUser;
	Button   deleteUser;
	Spinner  userID;
	String[] selectedAccount = null;
	TextView connected;
	Context  rootContext;
	EduroamLoginThread ELT;

	public DialogEduroam(Context context) {

		super(context);
		LayoutInflater li = LayoutInflater.from(context);

		View promptsView = li.inflate(R.layout.dialog_eduroam, null);
		setView(promptsView);
		setTitle(R.string.eduroam_title);

		this.addUser     = (Button)  promptsView.findViewById(R.id.eduroam_add_user);
		this.configUser  = (Button)  promptsView.findViewById(R.id.eduroam_config_user);
		this.deleteUser	 = (Button)  promptsView.findViewById(R.id.eduroam_delete);
		this.userID      = (Spinner) promptsView.findViewById(R.id.eduroam_users_spinner);
		this.connected 	 = (TextView)  promptsView.findViewById(R.id.eduroam_connected);
		this.rootContext = context;
		//----------------------------------------------------------------------------------------//
		// Set Click Listener for Buttons
		// add Button
		addUser.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new DialogEduroamNewUser(DialogEduroam.this.getContext(), DialogEduroam.this).show();
			}
		});
		// Configuration Button
		configUser.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(DialogEduroam.this.selectedAccount!=null)
				{
					if(!DialogEduroam.this.selectedAccount[0].equals(" "))
					{
						new DialogEduroamConfigUser(DialogEduroam.this.getContext(), DialogEduroam.this).show();
						return;
					}
				}

				Toast.makeText(DialogEduroam.this.getContext(), R.string.nothing_selected , Toast.LENGTH_SHORT).show();
			}
		});
		// Connect Button		
		deleteUser.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if(DialogEduroam.this.selectedAccount!=null)
				{
					if(!DialogEduroam.this.selectedAccount[0].equals(" "))
					{
						deleteUserDialog(DialogEduroam.this.selectedAccount[0]);
						return;
					}
				}
				Toast.makeText(DialogEduroam.this.getContext(), R.string.nothing_selected , Toast.LENGTH_SHORT).show();
			}
		});
		//----------------------------------------------------------------------------------------//
		setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.button_connect) , (new DialogInterface.OnClickListener() {
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
		//----------------------------------------------------------------------------------------//
		return;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//----------------------------------------------------------------------------------------//
		super.onCreate(savedInstanceState);
		//----------------------------------------------------------------------------------------//
		// Set User ID Spinner
		setSpinner();
		setConnected();
		this.userID.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> arg0,
					View arg1, int arg2, long arg3) {

				String userID = DialogEduroam.this.userID.getSelectedItem().toString();

				if(!userID.equals(" "))
				{
					DialogEduroam.this.selectedAccount = MyApplication.getEduroamAccount(userID);
				}
			}
			@Override
			public void onNothingSelected(android.widget.AdapterView<?> arg0) {
				Toast.makeText(DialogEduroam.this.getContext(), R.string.nothing_selected , Toast.LENGTH_SHORT).show();
			}
		});

		getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(DialogEduroam.this.selectedAccount==null||DialogEduroam.this.selectedAccount[0].equals(" "))
				{
					MyApplication.showWifiError(DialogEduroam.this.getContext(), MyApplication.No_User);
				}else
				{
					DialogEduroam.this.ELT = new EduroamLoginThread(DialogEduroam.this.selectedAccount[1], DialogEduroam.this.selectedAccount[2], DialogEduroam.this.getContext(), DialogEduroam.this);
					DialogEduroam.this.ELT.start();
				}
			}
		});

		getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	//-------------------------------------------------------------------------------------------------------------------------------------//
	public void setConnected()
	{
		if(MyApplication.checkWifi(DialogEduroam.this.getContext()))
		{
			this.connected.setText(R.string.status_connected);
			this.connected.setTextColor(Color.GREEN);
		}
		else
		{
			this.connected.setText(R.string.status_not_connected);
			this.connected.setTextColor(Color.RED);
		}
	}
	public void beginService()
	{
		Intent service = new Intent(this.getContext(), EduroamLogoutService.class);
		this.getContext().startService(service);
	}
	public void deleteUserDialog(final String userID)
	{
		new AlertDialog.Builder(this.getContext())
		.setTitle(R.string.delete_userID_title)
		.setMessage(R.string.delete_userID_question)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				MyApplication.deleteDataBaseEntry("eduroam_accounts", "userID", userID);
				setSpinner();
			}
		})
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				// do nothing
			}
		})
		.setIcon(android.R.drawable.ic_dialog_alert)
		.show();
	}
	public void setSpinner()
	{
		String[] account = MyApplication.getUsers("userID","eduroam_accounts");
		if(account==null){
			account = new String[1];
			account[0] = " ";
		}
		MyApplication.setSpinner(this.getContext(), userID, account); 
		this.selectedAccount = MyApplication.getEduroamAccount(account[0]); 
	}
	//-------------------------------------------------------------------------------------------------------------------------------------//
}
