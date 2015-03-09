package com.fhtrier.voiceDiary;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class NoiseMeterThread extends Thread {

	private static final String TAG = RecordingThread.class.getName();
	Activity activity;   
	NoiseMeterDialog noiseMeterDialog;
	public int 		 type;
	short[] 		 array;
	short[] 		 ringBuffer;
	int     		 bufferPointer;


	public NoiseMeterThread(Activity activity, NoiseMeterDialog noiseMeterDialog)
	{
		this.activity = activity ; 
		this.noiseMeterDialog = noiseMeterDialog;
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		//-- Initialize Buffers --//
		ringBuffer = new short[44100];
		bufferPointer = 0;
		//--------------------------------//
		this.start();
	}
	@Override
	public void run()
	{

		try
		{	
			int l = 0;
			this.array = new short[Values.BUFFER_SIZE];

			int bufferSize = AudioRecord.getMinBufferSize(Values.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
			AudioRecord audioRecord = new AudioRecord(AudioSource.MIC, Values.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			
			while(audioRecord.getState()==AudioRecord.STATE_UNINITIALIZED)
			{
				audioRecord.release();
				audioRecord = new AudioRecord(AudioSource.MIC, Values.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			}
			
			audioRecord.startRecording();

			while(noiseMeterDialog.Abort!=true)
			{
				if (this.isInterrupted())
				{
					audioRecord.stop();
					audioRecord.release();
					return;
				}
				
				if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
				{
					throw new Exception("AudioRecord init failed");
				}


				l = audioRecord.read(array, 0, array.length);

				if (l <= 0)
				{
					throw new Exception("AudioRecord read failed");
				}

				while (l != array.length)
				{
					l += audioRecord.read(array, l, array.length - l);
				}
				
				fillBuffer();
				
				this.activity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						noiseMeterDialog.NoiseMeterLevel(NoiseMeterThread.this.array, NoiseMeterThread.this.ringBuffer);
					}
				});
			}
			audioRecord.stop();
			audioRecord.release();
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error", e);
		}
	}
	
	public void fillBuffer()
	{
		for(int i=0;i<this.array.length;i++)
		{
			this.ringBuffer[this.bufferPointer] = this.array[i];
			this.bufferPointer = mod(this.bufferPointer+1, this.ringBuffer.length);
			
		}
	}
	private int mod(int x, int y)
	{
	    int result = x % y;
	    return result < 0? result + y : result;
	}
}
