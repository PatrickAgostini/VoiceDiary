package com.fhtrier.voiceDiary;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import android.os.Process;
import android.support.v4.app.NotificationCompat;

public class NotifyService extends Service{

	long endTime;
	
	@Override
	public void onCreate() {


	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			Notify();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// If we get killed, after returning from here, restart
		
		this.endTime = intent.getLongExtra("endTime", System.currentTimeMillis());
		
		return START_STICKY;
	}
	
	@SuppressLint("NewApi") public void Notify() throws IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(getResources().getString(R.string.notification_title))
		        .setContentText(getResources().getString(R.string.notification_text));
		mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000});
		mBuilder.setLights(Color.RED, 3000, 3000);
		Intent resultIntent = new Intent(this, VoiceDiaryActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(VoiceDiaryActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, mBuilder.getNotification());
		if(System.currentTimeMillis()>=this.endTime)
		{
			endService();
		}
	}
	
	public void endService()
	{
		AlarmManager aManager = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);         
		Intent intent = new Intent(this, NotifyService.class);      
		PendingIntent pIntent = PendingIntent.getService(this, 0, intent, 0);         
		aManager.cancel(pIntent);
	}
}
