package com.fhtrier.voiceDiary;

import java.util.List;

import org.holoeverywhere.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Spinner;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.fhtrier.voiceDiary.DialogPasswordPermission.PasswordDialogFragmentListener;

@SuppressLint("NewApi") 
public class LoginActivity  extends SherlockActivity implements PasswordDialogFragmentListener  
{
	public static final int PASSWORD_ERROR = 0;
	public static final int USERNAME_ERROR = 1;

	String user;
	String password;

	Spinner spinner;
	EditText passwordEdit;
	AlertDialog alertDialog;
	DialogPasswordPermission pwdPermissionDlg;
	DialogEduroamNewUser edNewUserDlg;
	
	boolean alertDialogOn;
	boolean isLogin;
	
	//-----------------------------------------------------------------------------------------------------------------------------------//
	// Activity Functions
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_voice_diary);
		
		LayoutInflater inflater = this.getLayoutInflater(); //(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ViewGroup viewGroup = (ViewGroup)this.findViewById(R.id.voice_diary_activity_view);
		inflater.inflate(R.layout.view_voice_diary_login, viewGroup);

		this.getSupportActionBar().setIcon(R.drawable.actionbar_logo);
		this.getSupportActionBar().setTitle("");
		this.spinner        = (Spinner) findViewById(R.id.voice_diary_login_id);
		this.passwordEdit   = (EditText)findViewById(R.id.voice_diary_login_password);
		MyApplication.setSpinner(this, this.spinner, MyApplication.getAllUsers());
		this.spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> arg0,
					View arg1, int arg2, long arg3) {
				LoginActivity.this.user = spinner.getSelectedItem().toString();

			}
			@Override
			public void onNothingSelected(android.widget.AdapterView<?> arg0) {
			}
		});
		final Button button = (Button)findViewById(R.id.voice_diary_login_button);
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final String password       = LoginActivity.this.passwordEdit.getText().toString();
				if(MyApplication.isPassword(password))
				{
					Intent intent = new Intent(LoginActivity.this, VoiceDiaryActivity.class);
					intent.putExtra("admin", true);
					LoginActivity.this.startActivity(intent);
					LoginActivity.this.finish();

				}else
				{
					LoginActivity.this.password = password;
					button.setEnabled(false);
					new LoginThread(user, password, LoginActivity.this);
				}
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if(!this.isLogin&&!this.alertDialogOn)
		{
			if(this.alertDialog!=null)
			{
				this.alertDialog.dismiss();
			}
			this.alertDialog = createCompleteDialog();
			this.alertDialog.show();
		}
	}

	@Override
	public void onBackPressed() {	
		this.isLogin = false;
		if(this.alertDialogOn)
		{
			exitAppDialog();
		}
		else
		{
			this.alertDialog.dismiss();
			this.alertDialog = createCompleteDialog();
			this.alertDialog.show();
		}
	}
	//-----------------------------------------------------------------------------------------------------------------------------------//
	//-----------------------------------------------------------------------------------------------------------------------------------//
	// Useful Stuff
	public void login(boolean successfully)
	{       
		if (successfully)
		{
			Toast toast = Toast.makeText(this.getApplicationContext(), R.string.login_successful, Toast.LENGTH_SHORT);
			toast.show();
			Intent intent = new Intent(this, VoiceDiaryActivity.class);
			this.startActivity(intent);
			this.finish();
		}
		else
		{
			Toast toast = Toast.makeText(this.getApplicationContext(), R.string.login_error, Toast.LENGTH_SHORT);
			toast.show();
		}
		((Button)findViewById(R.id.voice_diary_login_button)).setEnabled(true);
	}

	public void localData(int info){
		if (info == PASSWORD_ERROR){
			Toast toast = Toast.makeText(getApplicationContext(), this.getString(R.string.login_password_error), Toast.LENGTH_LONG);
			toast.show();
		}else if (info == USERNAME_ERROR){
			Toast toast = Toast.makeText(getApplicationContext(), this.getString(R.string.login_username_error), Toast.LENGTH_LONG);
			toast.show();
		}
		((Button)findViewById(R.id.voice_diary_login_button)).setEnabled(true);
	}

	//-----------------------------------------------------------------------------------------------------------------------------------//
	// CUSTOM Alert Dialogs
	//-----------------------------------------------------------------------------------------------------------------------------------//
	// Exit Application Dialog
	public void exitAppDialog()
	{
		new AlertDialog.Builder(this)
		.setTitle(R.string.exit)
		.setMessage(R.string.exit_app_question)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				LoginActivity.this.logout();
				LoginActivity.this.finish();
			}
		})
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
			}
		})
		.setIcon(android.R.drawable.ic_dialog_alert)
		.show();
	}
	//-----------------------------------------------------------------------------------------------------------------------------------//
	// Login or Register Dialog
	public AlertDialog createCompleteDialog()
	{
		AlertDialog.Builder builder =  new AlertDialog.Builder(this);
		TextView message = new TextView(this);
		message.setText("\n"+this.getString(R.string.login_or_register)+"\n");
		message.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
		message.setGravity(Gravity.CENTER_HORIZONTAL);
		builder.setCustomTitle(message);
		builder.setCancelable(false);
		builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK && 
						event.getAction() == KeyEvent.ACTION_UP && 
						!event.isCanceled()) {
					LoginActivity.this.onBackPressed();
					return true;
				}
				return false;
			}
		});
		this.alertDialogOn=true;

		builder.setPositiveButton(this.getString(R.string.register), new android.content.DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				LoginActivity.this.showPasswordDialog();	
				LoginActivity.this.alertDialogOn = false;
			}
		});

		builder.setNegativeButton(this.getString(R.string.login), new android.content.DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				LoginActivity.this.alertDialogOn = false;
				LoginActivity.this.isLogin       = true;
				if(MyApplication.getAllUsers()!=null)
				{
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoginActivity.this, android.R.layout.simple_spinner_item, MyApplication.getAllUsers());
					LoginActivity.this.spinner.setAdapter(adapter);
				}
			}
		});
		return builder.create();
	}
	//-----------------------------------------------------------------------------------------------------------------------------------//
	// Password Interface
	@Override
	public void onReturnValue(String Password) {
		if(MyApplication.isPassword(Password))
		{
			Intent intent = new Intent(this, RegisterActivity.class);
			intent.putExtra("caller", VoiceDiaryActivity.class.getName());
			this.startActivity(intent);
			this.finish();
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
		onResume();
	}
	// Start Interface
	@SuppressLint("NewApi") 
	public void showPasswordDialog()
	{
		DialogFragment pwdPermissionDlg = new DialogPasswordPermission();
		pwdPermissionDlg.show(getFragmentManager(), "dialog");

	}
	//-----------------------------------------------------------------------------------------------------------------------------------//
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
}
