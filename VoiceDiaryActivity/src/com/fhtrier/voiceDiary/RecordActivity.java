package com.fhtrier.voiceDiary;

import java.text.DecimalFormat;
import org.holoeverywhere.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * @author HO-Audio
 *
 */
/**
 * @author HO-Audio
 *
 */
public class RecordActivity extends SherlockActivity
{
	private RecordingThread recThread;
	private RecordAssistant recAssistant;
	private final DecimalFormat decimalFormat = new DecimalFormat("0.0");
	private AlertDialog dialog;
	public int frequency;
	PowerManager.WakeLock wl;
	
	public short[] noiseBuffer;

	NoiseMeterDialog noiseMeterDialog;

	public int State; 
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);

		this.getSupportActionBar().setIcon(R.drawable.actionbar_logo);
		this.getSupportActionBar().setTitle("");

		SliderView sv = (SliderView)this.findViewById(R.id.frequencySlider);
		sv.setGreenArea(Values.MAX_FREQUENCY_DEVITATION * 2);

		Cursor c = MyApplication.getSqLiteDatabase().rawQuery("SELECT frequency FROM user", null);
		c.moveToFirst();
		frequency = c.getInt(0);
		c.close();

		this.getApplicationContext();
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getName());
		dialog = this.createStartDialog();
		dialog.show();
	}

	void start()
	{
		super.onResume();
		if(this.State==0)
		{
			this.startRecording();
			wl.acquire();
		}else
		{
			this.noiseMeterDialog.startRecording();
			wl.acquire();
		}
	}

	@Override
	public void onPause()
	{
		if(this.State==0)
		{
			this.stopRecording();
			if (wl.isHeld())
			{
				wl.release();
			}
			if (dialog.isShowing())
			{
				dialog.cancel();
			}
			this.finish();
		}else
		{
			this.noiseMeterDialog.stopRecording();
			if (wl.isHeld())
			{
				wl.release();
			}
			if (dialog.isShowing())
			{
				dialog.cancel();
			}
			this.finish();
		}
		super.onPause();
	}

	void startRecording()
	{
		if (recThread != null)
		{
			recThread.interrupt();
			recThread = null;
		}

		recAssistant = new RecordAssistant(this);
		recThread = new RecordingThread(recAssistant);
	}

	private void stopRecording()
	{
		if (recThread != null)
		{
			recThread.interrupt();
			recThread = null;
		}
	}

	public void recordingError()
	{
		Toast toast = Toast.makeText(this.getApplicationContext(), this.getString(R.string.recording_error), Toast.LENGTH_LONG);
		toast.show();
		this.onBackPressed();
	}

	@Override
	public void onBackPressed()
	{
		this.finish();
	}

	public void recordingStarted()
	{
		TextView tv = (TextView)findViewById(R.id.recording_state);
		tv.setText(getString(R.string.recording_started));
	}

	public void recordingStopped()
	{
		TextView tv = (TextView)findViewById(R.id.recording_state);
		tv.setText(getString(R.string.recording_stopped));
		this.startActivity(new Intent(this, QuestionActivity.class));
		this.finish();
	}

	public void restTime(long restTime)
	{
		TextView tv = (TextView)findViewById(R.id.recording_time);
		tv.setText(decimalFormat.format(restTime / 1000F) + " sec.");
	}

	public void frequency(int frequency)
	{
		SliderView sliderView = (SliderView)findViewById(R.id.frequencySlider);
		sliderView.setPosition(frequency - this.frequency);
		TextView textView = (TextView)findViewById(R.id.frequencyText);
		textView.setText(frequency + " HZ");
	}

	public int[][] getUnscaledAmplitude(byte[] eightBitByteArray, int nbChannels)
	{
		int[][] toReturn = new int[nbChannels][eightBitByteArray.length / (2 * nbChannels)];
		int index = 0;

		for (int audioByte = 0; audioByte < eightBitByteArray.length;)
		{
			for (int channel = 0; channel < nbChannels; channel++)
			{
				// Do the byte to sample conversion.
				int low = (int) eightBitByteArray[audioByte];
				audioByte++;
				int high = (int) eightBitByteArray[audioByte];
				audioByte++;
				int sample = (high << 8) + (low & 0x00ff);

				toReturn[channel][index] = sample;
			}
			index++;
		}

		return toReturn;
	}

	public boolean frequencyCheck(int frequency)
	{
		return frequency <= this.frequency + Values.MAX_FREQUENCY_DEVITATION && frequency >= this.frequency - Values.MAX_FREQUENCY_DEVITATION;
	}

	private AlertDialog createStartDialog()
	{
		AlertDialog.Builder builder =  new AlertDialog.Builder(this);
		builder.setTitle(this.getString(R.string.record_dialog_title));
		builder.setMessage(this.getString(R.string.record_dialog_message));
		builder.setCancelable(false);
		builder.setPositiveButton(this.getString(R.string.record_dialog_ok), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				RecordActivity.this.State = 1;
				RecordActivity.this.noiseMeterDialog = new NoiseMeterDialog(RecordActivity.this, RecordActivity.this);
				RecordActivity.this.noiseMeterDialog.show();
				noiseMeterDialog.startRecording();
				//RecordActivity.this.start();
				RecordActivity.this.dialog.dismiss();
			}
		});
		builder.setNegativeButton(this.getString(R.string.record_dialog_back), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				RecordActivity.this.finish();
			}
		});
		return builder.create();
	}

}
