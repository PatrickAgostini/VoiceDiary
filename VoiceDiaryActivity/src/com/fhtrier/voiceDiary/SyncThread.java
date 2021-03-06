package com.fhtrier.voiceDiary;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Handler;
import android.util.Log;

import com.fhtrier.voiceDiary.commands.EntryRequest;
import com.fhtrier.voiceDiary.commands.EntryResponse;
import com.fhtrier.voiceDiary.commands.LoginRequest;
import com.fhtrier.voiceDiary.commands.LoginResponse;
import com.fhtrier.voiceDiary.commands.RegisterRequest;
import com.fhtrier.voiceDiary.commands.RegisterResponse;
import com.fhtrier.voiceDiary.commands.StartEntryRequest;
import com.fhtrier.voiceDiary.commands.StartEntryResponse;

import android.app.ProgressDialog;

public class SyncThread extends Thread
{
	ProgressDialog progress = null;
	AdministrationActivity AA;

	private  String birthday;
	private  boolean job;
	private  boolean hoarseness;
	private  boolean dysphonia;
	private  boolean operation;
	private  String operation_date;
	private  String operation_diagnose;
	private  boolean therapy;
	private  String therapy_date;
	private  String therapy_diagnose;
	String sessionId;
	private  Handler handler;
	
	public SyncThread(AdministrationActivity AA)
	{		
		this.birthday           = "00.00.0000";
		this.job                = false;
		this.hoarseness         = false;
		this.dysphonia          = false;
		this.operation 		    = false;
		this.operation_date     = "00.00.0000";
		this.operation_diagnose = "none";
		this.therapy			= false;
		this.therapy_date       = "00.00.0000";
		this.therapy_diagnose   = "none";
		
		this.handler = new Handler();
		this.handler.postDelayed(runnable, 120000000);
		
		this.AA = AA;
	    this.progress = ProgressDialog.show(AA,"Synchronization", "Sending Data...", true);
		this.start();
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void run()
	{
		Socket socket = null;
		try
		{
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////             Register Past Offline Registrations             ////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			socket = new Socket();
			SocketAddress adr = new InetSocketAddress(Values.ADDRESS, Values.PORT);
			socket.connect(adr, 20000);
			ObjectOutputStream outputStream;			
			ObjectInputStream inputStream;

			String [] users2Register = MyApplication.getUsers2Register();
			if(users2Register!=null)
			{
				for(int i=0;i<users2Register.length;i++) {
					if(!socket.isClosed())
					{
						socket.close();
					}
					socket = new Socket();
					adr = new InetSocketAddress(Values.ADDRESS, Values.PORT);
					socket.connect(adr, 200000);

					// Get Users
					PatientData patData = MyApplication.getPatientData(users2Register[i]);

					//Send Stuff
					outputStream = new ObjectOutputStream(CipherStreamGen.getEncryptOutputStream(new BufferedOutputStream(socket.getOutputStream())));
					outputStream.writeObject(new RegisterRequest(MyApplication.getExternalId(patData.userID), patData.password, patData.male, this.birthday, patData.smoker, this.job, this.hoarseness, this.dysphonia, this.operation, this.operation_date, this.operation_diagnose, this.therapy, this.therapy_date, this.therapy_diagnose));
					outputStream.flush();
					
					// Receive Stuff and check if ok
					inputStream = new ObjectInputStream(CipherStreamGen.getDecryptInputStream(new BufferedInputStream(socket.getInputStream())));
					if (((RegisterResponse) inputStream.readObject()).isSuccessfully())
					{
						MyApplication.setRegistered(patData.userID);			
					}        
				}
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////                      Update Past Offline Logins             ////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			String[] users2Update = MyApplication.getUsers2Update();
			if(users2Update!=null)
			{  
				for(int j=0;j<users2Update.length;j++) 
				{
					if(!socket.isClosed())
					{
						socket.close();
					}
					socket = new Socket();
					socket.connect(adr, 20000);
					outputStream = null;
					inputStream  = null;

					String[] account = MyApplication.getUser(users2Update[j]);
					outputStream     = new ObjectOutputStream(CipherStreamGen.getEncryptOutputStream(new BufferedOutputStream(socket.getOutputStream())));
					outputStream.writeObject(new LoginRequest(MyApplication.getExternalId(account[0]), account[1]));
					outputStream.flush();
					
					inputStream                       = new ObjectInputStream(CipherStreamGen.getDecryptInputStream(new BufferedInputStream(socket.getInputStream())));
					final LoginResponse loginResponse = (LoginResponse) inputStream.readObject();
					this.sessionId                    = loginResponse.getSessionId();
					
					if (loginResponse.isSuccessfully())
					{
						
						if(!socket.isClosed())
						{
							socket.close();
						}
						socket = new Socket();
						socket.connect(adr, 20000);
						outputStream = null;
						inputStream  = null;
						// Start Entry Stuff�

						outputStream = new ObjectOutputStream(CipherStreamGen.getEncryptOutputStream(new BufferedOutputStream(socket.getOutputStream())));
						outputStream.writeObject(new StartEntryRequest(MyApplication.getExternalId(account[0]), this.sessionId));
						outputStream.flush();

						// Start Entry Stuff
						Log.d(this.getClass().getName(), "startEntryResponse");
						inputStream = new ObjectInputStream(CipherStreamGen.getDecryptInputStream(new BufferedInputStream(socket.getInputStream())));
						StartEntryResponse startEntryResponse = ((StartEntryResponse)inputStream.readObject());

						if (startEntryResponse.getDate() != null)
						{
							//Cursor entrys = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `p`.`id_protocolentry`, `p`.`date`, `r`.`wave`, `r`.`filename` ,`u`.`last_upload` FROM `user` AS `u`, `protocolentry` AS `p`, `record` AS `r` WHERE `p`.`id_user` = '%s' AND `r`.`id_user` = `p`.`id_user` AND `p`.`id_protocolentry` = `r`.`id_protocolentry` AND `p`.`date` > `u`.`last_upload`;", users2Update[j]), null);
							//Cursor dates = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `p`.`id_protocolentry`, `p`.`date`, `r`.`filename` ,`u`.`last_upload` FROM `user` AS `u`, `protocolentry` AS `p`, `record` AS `r` WHERE `p`.`id_user` = '%s' AND `r`.`id_user` = `p`.`id_user` AND `p`.`id_protocolentry` = `r`.`id_protocolentry` AND `p`.`date` > `u`.`last_upload`;", users2Update[j]), null);
							Cursor dates = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `p`.`date`,`r`.`filename` FROM `record` AS `r`,`protocolentry` AS `p`,`user` AS `u` WHERE `p`.`id_user` = '%s' AND `p`.`id_protocolentry`=`r`.`id_protocolentry` AND `p`.`id_user`=`r`.`id_user` AND `p`.`id_user` =`u`.`id_user` AND `p`.`date`>`u`.`last_upload`;", users2Update[j]), null);
							MyApplication.printCursor( MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `date` FROM `protocolentry` WHERE `id_user` = '%s';", users2Update[j]), null));
							Log.d(this.getClass().getName(), "start sync");
							//while (entrys.moveToNext())
							while(dates.moveToNext())
							{
								String date     = dates.getString(0);
								String filename = dates.getString(1);
								
								Cursor len = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT length(`r`.`wave`) FROM `user` AS `u`, `protocolentry` AS `p`, `record` AS `r` WHERE `p`.`id_user` = '%s' AND `r`.`id_user` = `p`.`id_user` AND `p`.`id_protocolentry` = `r`.`id_protocolentry` AND `p`.`date`='%s';", users2Update[j], date), null);
								len.moveToFirst();
								double leng2 = (double)len.getInt(0);
								double leng  = (double)len.getInt(0)/2;
								
								
								Cursor entrys = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT SUBSTR(`wave`,0,'%s') FROM `record` WHERE `filename`='%s' AND `id_user`='%s' ;", String.valueOf((int)leng), filename, users2Update[j]), null);
								entrys.moveToFirst();
								byte[] waveArray1 = entrys.getBlob(0);

								entrys = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT SUBSTR(`wave`,'%s','%s') FROM `record` WHERE `filename`='%s' AND `id_user`='%s' ;", String.valueOf((int)leng),String.valueOf((int)(leng2-leng)), filename, users2Update[j]), null);
								entrys.moveToFirst();
								byte[] waveArray2 = entrys.getBlob(0);
								
								ByteBuffer buf = ByteBuffer.allocate((int)leng2);
								buf.put(waveArray1);
								buf.put(waveArray2);

								byte[] waveArray = buf.array( );
								
								entrys = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `p`.`id_protocolentry`, `p`.`date`, `r`.`filename` ,`u`.`last_upload` FROM `user` AS `u`, `protocolentry` AS `p`, `record` AS `r` WHERE `p`.`id_user` = '%s' AND `r`.`id_user` = `p`.`id_user` AND `p`.`id_protocolentry` = `r`.`id_protocolentry` AND `p`.`date`='%s';", users2Update[j], date), null);
								entrys.moveToFirst();
								
								Cursor answers = MyApplication.getSqLiteDatabase().rawQuery(String.format("SELECT `id_answer` FROM `rel_protocolentry_answer` WHERE `id_user` = '%s' AND `id_protocolentry` = '%d';",  users2Update[j], entrys.getInt(0)), null);
								int[] answersArray = new int[answers.getCount()];
								for (int i = 0; i < answersArray.length; ++i)
								{
									answers.moveToNext();
									answersArray[i] = answers.getInt(0);
								}
								answers.close();
								Log.d(this.getClass().getName(), "sendEntryRequest");
								outputStream.writeObject(new EntryRequest(entrys.getString(1), answersArray, entrys.getString(2), waveArray));
								outputStream.flush();
								Log.d(this.getClass().getName(), "flush()");
								Log.d(this.getClass().getName(), "EntryResponse");
								EntryResponse entryResponse = (EntryResponse)inputStream.readObject();
								if (entryResponse.isSuccessfully())
								{
									 MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user_protocolenty` SET `last_updated_protocolenty` = '%d' WHERE `id_user` = '%s';", entrys.getInt(0),  users2Update[j]));
								}
								SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								//MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user` SET `last_upload` = '%s' WHERE `id_user` = '%s';", df.format(new Date()),  users2Update[j]));
								MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user` SET `last_upload` = '%s' WHERE `id_user` = '%s';", date,  users2Update[j]));
								entrys.close();
								len.close();
							}
							MyApplication.updateUser(users2Update[j]);
						}
					}
				}
			}      
			this.progress.dismiss();
			this.handler.removeCallbacks(runnable);

			if(!socket.isClosed())
			{
				socket.close();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			this.AA.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					AA.Message(0);
				}
			});	
		}
		catch (SQLException e)
		{
			this.progress.dismiss();
			this.handler.removeCallbacks(runnable);
			e.printStackTrace();
			this.AA.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					AA.Message(1);
				}
			});		
		}
		catch (IOException e)
		{
			this.progress.dismiss();
			this.handler.removeCallbacks(runnable);
            e.printStackTrace();
        	this.AA.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					AA.Message(1);
				}
			});	
		}
		catch (ClassNotFoundException e)
		{
			this.progress.dismiss();
			this.handler.removeCallbacks(runnable);
			e.printStackTrace();
			this.AA.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					AA.Message(1);
				}
			});		
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.progress.dismiss();
			this.handler.removeCallbacks(runnable);
			this.AA.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					AA.Message(1);
				}
			});	
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				this.progress.dismiss();
				this.handler.removeCallbacks(runnable);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}  
		this.progress.dismiss();
		this.handler.removeCallbacks(runnable);
		
	}
	private Runnable runnable = new Runnable() {
		   @Override
		   public void run() {
		      SyncThread.this.interrupt();

			  SyncThread.this.progress.dismiss();
		      SyncThread.this.AA.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						AA.Message(2);
					}
				});	
		   }
		};
}
