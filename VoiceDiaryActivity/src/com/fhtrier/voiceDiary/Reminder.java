package com.fhtrier.voiceDiary;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

public class Reminder extends AlertDialog{
	
	private boolean checked = false;
	
	int startH;
	int startM;
	int stopH;
	int stopM;
	int Interval;
	
	
	public Reminder(Context context) {		
		super(context);
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.wecker, null);
		setView(promptsView);
		setTitle(R.string.reminder);

		setButton(AlertDialog.BUTTON_NEGATIVE, context.getResources().getString(R.string.button_ok) , (new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// this will never be called
			}
		}));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		ArrayAdapter<String> hours   = new ArrayAdapter<String>(this.getContext(), R.layout.spinner_item, Values.hours);
		ArrayAdapter<String> minutes   = new ArrayAdapter<String>(this.getContext(), R.layout.spinner_item, Values.minutes);
		ArrayAdapter<String> intervals   = new ArrayAdapter<String>(this.getContext(), R.layout.spinner_item, Values.intervals);
		
		final Spinner startHours     = (Spinner) findViewById(R.id.startHours);
		final Spinner startMinutes     = (Spinner) findViewById(R.id.startMinutes);
		final Spinner stopHours     = (Spinner) findViewById(R.id.stopHours);
		final Spinner stopMinutes     = (Spinner) findViewById(R.id.stopMinutes);
		final Spinner repeatIntervals     = (Spinner) findViewById(R.id.repeatIntervals);
		
		startHours.setAdapter(hours);
		startMinutes.setAdapter(minutes);
		stopHours.setAdapter(hours);
		stopMinutes.setAdapter(minutes);
		repeatIntervals.setAdapter(intervals);
		
		startHours.setSelection(MyApplication.startH-1);
		startMinutes.setSelection(MyApplication.startM-1);
		stopHours.setSelection(MyApplication.stopH-1);
		stopMinutes.setSelection(MyApplication.stopM-1);
		repeatIntervals.setSelection(MyApplication.interval-1);
		
		((RadioButton) findViewById(R.id.timerOnOff)).setChecked(MyApplication.weckerOnOff);
		((RadioButton) findViewById(R.id.timerOnOff)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!MyApplication.weckerOnOff)
				{
					MyApplication.weckerOnOff = true;
				}else
				{
					MyApplication.weckerOnOff = false;
				}
				((RadioButton) findViewById(R.id.timerOnOff)).setChecked(MyApplication.weckerOnOff);
			}
		});
		
		getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(MyApplication.weckerOnOff)
				{
					try
					{
						MyApplication.startH   = Integer.parseInt(startHours.getSelectedItem().toString());
						MyApplication.startM   = Integer.parseInt(startMinutes.getSelectedItem().toString());
						MyApplication.stopH    = Integer.parseInt(stopHours.getSelectedItem().toString());
						MyApplication.stopM    = Integer.parseInt(stopMinutes.getSelectedItem().toString());
						MyApplication.interval = Integer.parseInt(repeatIntervals.getSelectedItem().toString());

					}
					catch(NumberFormatException nfe){
						MyApplication.startH   = 0;
						MyApplication.startM   = 0;
						MyApplication.stopH    = 0;
						MyApplication.stopM    = 0;
						MyApplication.interval = 1;
					}
					
					Calendar c = Calendar.getInstance();
					c.set(c.get(1), c.get(4), c.get(5), Reminder.this.stopH, Reminder.this.stopM, 0);
				    long endTime = c.getTimeInMillis();
					c.set(c.get(1), c.get(4), c.get(5), Reminder.this.startH, Reminder.this.startM, 0);
				    long startTime = c.getTimeInMillis();
				
					
					Intent mIntent = new Intent(Reminder.this.getContext(), NotifyService.class);
					mIntent.putExtra("endTime", startTime+endTime);
					mIntent.putExtra("startTime", startTime+endTime);
					
					PendingIntent pintent = PendingIntent.getService(Reminder.this.getContext(), 0, mIntent, 0);
					
					AlarmManager alarmManager = (AlarmManager)Reminder.this.getContext().getSystemService(Reminder.this.getContext().ALARM_SERVICE);				
					alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime, (long)6*1000, pintent);	
				}
				else
				{
					
				}
				dismiss();
			}
		});
	}	
}
