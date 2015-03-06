package com.fhtrier.voiceDiary;

import java.util.List;
import org.holoeverywhere.widget.Toast;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;

public class EduroamLogoutService extends Service {
	
	final int INTERVAL = 1000*15*60;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		logoutCountDown();

	};

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void logoutCountDown()
	{
		final Handler handler = new Handler();
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				logout();}};
				handler.postDelayed(runnable, INTERVAL);
	}

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
