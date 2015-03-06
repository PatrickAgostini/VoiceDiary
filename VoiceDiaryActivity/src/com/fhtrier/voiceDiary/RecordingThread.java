package com.fhtrier.voiceDiary;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class RecordingThread extends Thread
{
	private static final String TAG = RecordingThread.class.getName();

	private final IRecordingAssistant recordingAssistant;

	public RecordingThread(IRecordingAssistant recordingAssistant)
	{
		this.recordingAssistant = recordingAssistant;
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		this.start();
	}

	@Override
	public void run()
	{
		try
		{
			int l = 0;
			short[] array = new short[Values.BUFFER_SIZE];

			int bufferSize = AudioRecord.getMinBufferSize(Values.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
			AudioRecord audioRecord = new AudioRecord(AudioSource.MIC, Values.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			
			while(audioRecord.getState()==AudioRecord.STATE_UNINITIALIZED)
			{
				audioRecord.release();
				audioRecord = new AudioRecord(AudioSource.MIC, Values.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			}
			audioRecord.startRecording();

			if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
			{
				throw new Exception("AudioRecord init failed");
			}

			recordingAssistant.recordingStarted();

			while(recordingAssistant.isPrerecord())
			{
				if (this.isInterrupted())
				{
					audioRecord.stop();
					audioRecord.release();
					recordingAssistant.recordingInterrupt();
					return;
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
				recordingAssistant.noiseRecArrayPre(array);
			}

			while (recordingAssistant.isRecording())
			{
				if (this.isInterrupted())
				{
					audioRecord.stop();
					audioRecord.release();
					recordingAssistant.recordingInterrupt();
					return;
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

				recordingAssistant.newInputArray(array);
			}

			while(recordingAssistant.isPostrecord())
			{
				if (this.isInterrupted())
				{
					audioRecord.stop();
					audioRecord.release();
					recordingAssistant.recordingInterrupt();
					return;
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

				recordingAssistant.noiseRecArrayPost(array);
			}

			audioRecord.stop();
			audioRecord.release();
			recordingAssistant.recordingStopped();
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error", e);
			recordingAssistant.recordingError(e);
		}
	}

}