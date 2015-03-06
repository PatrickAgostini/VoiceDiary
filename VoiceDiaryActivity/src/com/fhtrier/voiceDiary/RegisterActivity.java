package com.fhtrier.voiceDiary;
import org.holoeverywhere.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;

public class RegisterActivity extends SherlockActivity
{
	public static final int SUCCESSFULLY = 0;
	public static final int ERROR_LOGIN_NAME = 1;
	public static final int ERROR_CONNECTION = 2;
	public static final int ERROR_PASSWORD = 3;
	public static final int USER_DUPLICATE = 4;
	
	public String userID;
	public String password;
	
	private RadioGroup gender;
    private EditText   patientId;
	private RadioGroup smoker;
	private Button 	   register;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_register);

		this.getSupportActionBar().setIcon(R.drawable.actionbar_logo);
		this.getSupportActionBar().setTitle("");

        patientId = (EditText) findViewById(R.id.voice_diary_register_patient_id);
        
		gender = (RadioGroup) findViewById(R.id.voice_diary_register_gender);
		smoker = (RadioGroup) findViewById(R.id.voice_diary_register_smoker);
		
		register = (Button) findViewById(R.id.voice_diary_register_button);

		
		OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				RegisterActivity.this.checkInputCorrect();
			}
		};

		gender.setOnCheckedChangeListener(onCheckedChangeListener);
		smoker.setOnCheckedChangeListener(onCheckedChangeListener);
		
		register.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				register.setEnabled(false);

				RegisterActivity.this.userID       = RegisterActivity.this.patientId.getText().toString(); 
				RegisterActivity.this.password     = MyApplication.createPassword();
				final boolean tmp_male             = ((RadioButton)RegisterActivity.this.gender.getChildAt(0)).isChecked();
				final boolean tmp_smoker  		   = ((RadioButton)RegisterActivity.this.smoker.getChildAt(0)).isChecked();
				
				new RegisterThread(RegisterActivity.this, RegisterActivity.this.userID, RegisterActivity.this.password, tmp_male,tmp_smoker);
			}
		});
	}

	private void checkInputCorrect()
	{
		if (gender.getCheckedRadioButtonId() == -1)
		{
			register.setEnabled(false);
			return;
		}
		if (smoker.getCheckedRadioButtonId() == -1)
		{
			register.setEnabled(false);
			return;
		}

		register.setEnabled(true);
	}

	public void register(int info)
	{
		if (info == SUCCESSFULLY)
		{
			Toast toast = Toast.makeText(this.getApplicationContext(), this.getText(R.string.register_successful), Toast.LENGTH_SHORT);
			toast.show();

			AlertDialog dialog = createCompleteDialog();
			dialog.show();
		}

		checkInputCorrect();
	}

	private AlertDialog createCompleteDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.show_data_Title));        
        builder
     				.setMessage(this.getString(R.string.show_data_login_string) + "           " + 
     							RegisterActivity.this.userID + "\n" + this.getString(R.string.show_data_password_string) +
     							"    " + RegisterActivity.this.password)
     				.setCancelable(false)
     				.setPositiveButton("OK",new DialogInterface.OnClickListener() {
     					public void onClick(DialogInterface dialog,int id) {
     				    	Intent intent = new Intent(RegisterActivity.this, VoiceDiaryActivity.class);
     						RegisterActivity.this.startActivity(intent);
     						RegisterActivity.this.finish();
     					}
     				  });

        return builder.create();
    }


	
	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	public void onBackPressed()
	{
		this.startActivity(new Intent(this, LoginActivity.class));
		this.finish();
	}
}
