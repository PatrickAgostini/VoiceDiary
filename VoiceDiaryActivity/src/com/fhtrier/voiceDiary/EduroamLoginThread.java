package com.fhtrier.voiceDiary;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.app.AlertDialog;

public class EduroamLoginThread extends Thread {

	public final int Wifi_Not_Exist = 0;
	public final int Wifi_Connection_Timedout = 1;
	public final int Wifi_Connected = 2;
	public final int Wifi_TimeOut  = 3;
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	private String  email;
	private String  password;
	public  Context context;

	public  Boolean pwdCheck;
	public  Boolean connection;
	public  Boolean wifiExists;

	ProgressDialog  progress = null;

	DialogEduroam   dialogEduroam;
	AlertDialog     alertDialog;
	Activity        mainActivity;
	/////////////////////////////////////////////////////////////////////////////////////////////
	private static final String INT_PRIVATE_KEY = "private_key";
	private static final String INT_PHASE2 = "phase2";
	private static final String INT_PASSWORD = "password";
	private static final String INT_IDENTITY = "identity";
	private static final String INT_EAP = "eap";
	private static final String INT_CLIENT_CERT = "client_cert";
	private static final String INT_CA_CERT = "ca_cert";
	private static final String INT_ANONYMOUS_IDENTITY = "anonymous_identity";
	final String INT_ENTERPRISEFIELD_NAME = "android.net.wifi.WifiConfiguration$EnterpriseField";
	/////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressLint("SimpleDateFormat")
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public EduroamLoginThread(String email, String password, Context context, DialogEduroam dialogEduroam){
		this.mainActivity    = (Activity) dialogEduroam.rootContext;
		this.email        	 = email;
		this.password        = password;  
		this.context         = context;
		this.dialogEduroam   = dialogEduroam;
		this.progress        = ProgressDialog.show(this.context, this.context.getString(R.string.progress_dialog_connecting), this.context.getString(R.string.progress_connecting), true);	
	}

	public void run()
	{
		connect();
	}

	public void connect(){

		WifiConfiguration selectedConfig = connectToAP(this.email, this.password);
		WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);
		wifiManager.addNetwork(selectedConfig);

		//Find Eduroam
		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		this.wifiExists = checkWifi(list, "\"" + "eduroam" + "\"");
		if(this.wifiExists){
			wifiLogin(wifiManager,list);
			checkConnection();
		}
		else
		{		
			this.mainActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					MyApplication.showWifiError(context, MyApplication.Wifi_Not_Exist);
					EduroamLoginThread.this.dialogEduroam.setConnected();
				}
			});
		}
		this.progress.dismiss();
	}

	public void wifiLogin(WifiManager wifiManager, List<WifiConfiguration> list){
		for( WifiConfiguration i : list ) 
		{
			if(i.SSID != null && i.SSID.equals("\"" + "eduroam" + "\"")) {
				wifiManager.disconnect();
				wifiManager.enableNetwork(i.networkId, true);
				wifiManager.reconnect();
			}
		}
	}

	public Boolean checkWifi(List<WifiConfiguration> list, String name){
		for( WifiConfiguration i : list ) {
			if(i.SSID != null && i.SSID.equals("\"" + "eduroam" + "\"")) {
				return true;	
			}
		}
		return false;
	}

	public void checkConnection()
	{      
		ConnectivityManager connManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		long endTime = System.currentTimeMillis() + 60*1000;

		while (System.currentTimeMillis() < endTime) 
		{
			mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);		
			if(mWifi.isConnected()) 
			{	
				this.connection = true;
				beginService();
				//this.progress.dismiss();
				this.mainActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						MyApplication.showWifiError(context, MyApplication.Wifi_Connected);
						EduroamLoginThread.this.dialogEduroam.setConnected();
					}});
				return;
			}
		}
		//this.progress.dismiss();
		this.mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {MyApplication.showWifiError(context, MyApplication.Wifi_TimeOut);}});
		return;
	}
	public void beginService()
	{
		Intent service = new Intent(this.context, EduroamLogoutService.class);
		this.context.startService(service);
	}

	@SuppressWarnings("rawtypes")
	public WifiConfiguration connectToAP(String userName, String password)
	{	final String INT_ENTERPRISEFIELD_NAME = "android.net.wifi.WifiConfiguration$EnterpriseField";
	/********************************Configuration Strings****************************************************/
	final String ENTERPRISE_EAP ="TTLS";
	final String ENTERPRISE_CLIENT_CERT = null;
	final String ENTERPRISE_PRIV_KEY =null;
	final String ENTERPRISE_PHASE2 = "auth=PAP";
	final String ENTERPRISE_ANON_IDENT = "";
	final String ENTERPRISE_CA_CERT ="";
	final String ENTERPRISE_PASSWORD = password;
	final String ENTERPRISE_USER     = userName;
	/********************************Configuration Strings****************************************************/

	/*Create a WifiConfig*/
	WifiConfiguration selectedConfig = new WifiConfiguration();

	/*AP Name*/
	selectedConfig.SSID = "\"eduroam\"";

	/*Priority*/
	selectedConfig.priority = 40;

	/*Enable Hidden SSID*/
	selectedConfig.hiddenSSID = false;

	/*Key Mgmnt*/
	selectedConfig.allowedKeyManagement.clear();
	selectedConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
	selectedConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);

	/*Group Ciphers*/
	selectedConfig.allowedGroupCiphers.clear();
	selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
	selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

	/*Pairwise ciphers*/
	selectedConfig.allowedPairwiseCiphers.clear();
	selectedConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	selectedConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

	/*Protocols*/
	selectedConfig.allowedProtocols.clear();
	selectedConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	selectedConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

	try {
		Class[] wcClasses = WifiConfiguration.class.getClasses();
		Class wcEnterpriseField = null;
		for (Class wcClass : wcClasses)
			if (wcClass.getName().equals(INT_ENTERPRISEFIELD_NAME)) 
			{
				wcEnterpriseField = wcClass;
				break;
			}
		boolean noEnterpriseFieldType = false; 
		if(wcEnterpriseField == null)
			noEnterpriseFieldType = true; 

		Field wcefAnonymousId = null, wcefCaCert = null, wcefClientCert = null, wcefEap = null, wcefIdentity = null, wcefPassword = null, wcefPhase2 = null, wcefPrivateKey = null;
		Field[] wcefFields = WifiConfiguration.class.getFields();
		for (Field wcefField : wcefFields) 
		{
			if (wcefField.getName().equals(INT_ANONYMOUS_IDENTITY))
				wcefAnonymousId = wcefField;
			else if (wcefField.getName().equals(INT_CA_CERT))
				wcefCaCert = wcefField;
			else if (wcefField.getName().equals(INT_CLIENT_CERT))
				wcefClientCert = wcefField;
			else if (wcefField.getName().equals(INT_EAP))
				wcefEap = wcefField;
			else if (wcefField.getName().equals(INT_IDENTITY))
				wcefIdentity = wcefField;
			else if (wcefField.getName().equals(INT_PASSWORD))
				wcefPassword = wcefField;
			else if (wcefField.getName().equals(INT_PHASE2))
				wcefPhase2 = wcefField;
			else if (wcefField.getName().equals(INT_PRIVATE_KEY))
				wcefPrivateKey = wcefField;
		}
		Method wcefSetValue = null;
		if(!noEnterpriseFieldType){
			for(Method m: wcEnterpriseField.getMethods())
				if(m.getName().trim().equals("setValue"))
					wcefSetValue = m;
		}
		/*EAP Identity*/
		if(!noEnterpriseFieldType)
		{
			wcefSetValue.invoke(wcefIdentity.get(selectedConfig), ENTERPRISE_USER);
		}
		else
		{
			wcefIdentity.set(selectedConfig, ENTERPRISE_USER);
		}       
		/*EAP Password*/
		if(!noEnterpriseFieldType)
		{
			wcefSetValue.invoke(wcefPassword.get(selectedConfig), ENTERPRISE_PASSWORD);
		}
		else
		{
			wcefPassword.set(selectedConfig, ENTERPRISE_PASSWORD);
		}               
		/*EAp Client certificate*/
		if(!noEnterpriseFieldType)
		{
			wcefSetValue.invoke(wcefClientCert.get(selectedConfig), ENTERPRISE_CLIENT_CERT);
		}
		else
		{
			wcefClientCert.set(selectedConfig, ENTERPRISE_CLIENT_CERT);
		}               
		/*EAP Method*/
		if(!noEnterpriseFieldType)
		{
			wcefSetValue.invoke(wcefEap.get(selectedConfig), ENTERPRISE_EAP);
		}
		else
		{
			wcefEap.set(selectedConfig, ENTERPRISE_EAP);
		}
		/*EAP Phase 2 Authentication*/
		if(!noEnterpriseFieldType)
		{
			wcefSetValue.invoke(wcefPhase2.get(selectedConfig), ENTERPRISE_PHASE2);
		}
		else
		{
			wcefPhase2.set(selectedConfig, ENTERPRISE_PHASE2);
		}
		/*EAP Anonymous Identity*/
		if(!noEnterpriseFieldType)
		{
			wcefSetValue.invoke(wcefAnonymousId.get(selectedConfig), ENTERPRISE_ANON_IDENT);
		}
		else
		{
			wcefAnonymousId.set(selectedConfig, ENTERPRISE_ANON_IDENT);
		}
		/*EAP CA Certificate*/
		if(!noEnterpriseFieldType)
		{
			wcefSetValue.invoke(wcefCaCert.get(selectedConfig), ENTERPRISE_CA_CERT);
		}
		else
		{
			wcefCaCert.set(selectedConfig, ENTERPRISE_CA_CERT);
		}               
		/*EAP Private key*/
		if(!noEnterpriseFieldType)
		{
			wcefSetValue.invoke(wcefPrivateKey.get(selectedConfig), ENTERPRISE_PRIV_KEY);
		}
		else
		{
			wcefPrivateKey.set(selectedConfig, ENTERPRISE_PRIV_KEY);
		}               

	} catch (Exception e)
	{
		e.printStackTrace();
	}
	return selectedConfig;
	}

}
