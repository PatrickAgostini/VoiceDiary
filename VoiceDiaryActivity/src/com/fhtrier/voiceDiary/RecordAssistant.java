package com.fhtrier.voiceDiary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class RecordAssistant implements IRecordingAssistant
{
	private static final long RECORDINGTIME = 3000;
	private static final long PRERECORD =  3000;
	private static final long POSTRECORD = 3000;

	private static final int FREQUENCY_CHECKNUMBER = 3;

	RecordActivity recordActivity;
	
	private int counter = 0;
	private int counterPre = 0;
	private int counterPost = 0;
	private int arraySizeCounter = 0;
	private ArrayList<short[]> arrayList = new ArrayList<short[]>();
	private ArrayList<Integer> frequencyList = new ArrayList<Integer>();

	public RecordAssistant(RecordActivity recordActivity)
	{
		this.recordActivity = recordActivity;
	}

	@Override
	public void recordingStarted()
	{
		recordActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				recordActivity.recordingStarted();
				recordActivity.restTime(RECORDINGTIME);
			}
		});
	}

	@Override
	public void recordingStopped()
	{
		SQLiteDatabase sqLiteDatabase = MyApplication.getSqLiteDatabase();

		Cursor c = sqLiteDatabase.rawQuery("SELECT id_user FROM user WHERE `offline_login` NOT NULL;", null);
		c.moveToFirst();
		String userid = c.getString(0);
		c.close();

		c = sqLiteDatabase.rawQuery(String.format("SELECT `next_protocolentry_id` FROM `user_protocolenty` WHERE `id_user` = '%s';", userid), null);
		c.moveToFirst();
		int entryid = c.getInt(0);
		c.close();

		recordActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				recordActivity.recordingStopped();
			}
		});

		c = sqLiteDatabase.rawQuery("SELECT `id_protocolentry` FROM `record` WHERE `id_protocolentry` = '" + entryid + "';", null);
		if (c.getCount() == 1)
		{
			sqLiteDatabase.execSQL("DELETE FROM `record` WHERE `id_protocolentry` = '" + entryid + "';");
		}
		c.close();

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();

		try
		{
			bOut.write(this.getWaveHeader(Values.SAMPLE_RATE, 16, 1, arrayList.size() * Values.BUFFER_SIZE * 2));

			byte[] b = new byte[this.recordActivity.noiseBuffer.length * 2];
			for (int k = 0; k <this.recordActivity.noiseBuffer.length; ++k)
			{
				b[2 * k] = (byte) (this.recordActivity.noiseBuffer[k] & 0x00FF);
				b[2 * k + 1] = (byte) ((this.recordActivity.noiseBuffer[k] >> 8) & 0x00FF);
			}
			bOut.write(b);
			for (int i = 0; i < arrayList.size(); ++i)
			{
				short[] array = arrayList.get(i);

				//byte[] b = new byte[array.length * 2];
				b = new byte[array.length * 2];

				for (int k = 0; k < array.length; ++k)
				{
					b[2 * k] = (byte) (array[k] & 0x00FF);
					b[2 * k + 1] = (byte) ((array[k] >> 8) & 0x00FF);
				}

				bOut.write(b);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try
			{
				bOut.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		byte[] byteArray = bOut.toByteArray();

		String sql = "INSERT INTO `record` VALUES(?, ?, ?, ?, ?);";
		SQLiteStatement insertStmt = sqLiteDatabase.compileStatement(sql);

		insertStmt.clearBindings();
		insertStmt.bindString(1, userid);
		insertStmt.bindLong(2, entryid);
		insertStmt.bindString(3, "REC_" + entryid + ".wav");
		insertStmt.bindLong(4, byteArray.length);
		insertStmt.bindBlob(5, byteArray);
		insertStmt.executeInsert();

	}

	@Override
	public void recordingError(Exception e)
	{
		recordActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				recordActivity.recordingError();
			}
		});
	}
	
	@Override
	public void recordingInterrupt()
	{
	}
	@Override
	public boolean isRecording()
	{
		return RECORDINGTIME - (long) (1000D / Values.SAMPLE_RATE * counter) > 0;
	}
	public boolean isPrerecord()
	{
		return PRERECORD - (long) (1000D / Values.SAMPLE_RATE * counterPre) > 0;
	}
	public boolean isPostrecord()
	{
		return POSTRECORD - (long) (1000D / Values.SAMPLE_RATE * counterPost) > 0;
	}
	public void noiseRecArrayPre(short[] array)
	{
		arrayList.add(array.clone());
		arraySizeCounter += array.length;
		counterPre          += arraySizeCounter;
		
		recordActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				recordActivity.frequency(RecordAssistant.this.recordActivity.frequency);
			}
		});
	}
	public void noiseRecArrayPost(short[] array)
	{
		arrayList.add(array.clone());
		arraySizeCounter += array.length;
		counterPost      += arraySizeCounter;		
		recordActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				recordActivity.frequency(RecordAssistant.this.recordActivity.frequency);
			}
		});
	}
	@Override
	public void newInputArray(short[] array)
	{
		long start = System.currentTimeMillis();
		frequencyList.add(AudioAnalyzer.getFrequency(array));
		Log.i(this.getClass().getName(), "Analyse = " + String.valueOf(System.currentTimeMillis() - start));

		arrayList.add(array.clone());

		arraySizeCounter += array.length;
		
		if (frequencyList.size() != FREQUENCY_CHECKNUMBER)
		{
			return;
		}

		Collections.sort(frequencyList);

		final int frequency = frequencyList.get((frequencyList.size() / 2));

		if (recordActivity.frequencyCheck(frequency))
		{
			counter += arraySizeCounter;
		}
		else
		{
			counter = 0;
			arrayList.clear();
		}

		frequencyList.clear();
		arraySizeCounter = 0;

		final long restTime = Math.max(RECORDINGTIME - (long) (1000D / Values.SAMPLE_RATE * counter), 0);

		recordActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				recordActivity.restTime(restTime);
				recordActivity.frequency(frequency);
			}
		});
	}

	private byte[] getWaveHeader(int sampleRate, int bitrate, int channels, int totalAudioLength)
	{
		byte[] header = new byte[44];

		int totalDataLen = header.length - 8 + totalAudioLength;
		int byterate = ((sampleRate * bitrate) / 8);

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (sampleRate & 0xff);
		header[25] = (byte) ((sampleRate >> 8) & 0xff);
		header[26] = (byte) ((sampleRate >> 16) & 0xff);
		header[27] = (byte) ((sampleRate >> 24) & 0xff);
		header[28] = (byte) (byterate & 0xff);
		header[29] = (byte) ((byterate >> 8) & 0xff);
		header[30] = (byte) ((byterate >> 16) & 0xff);
		header[31] = (byte) ((byterate >> 24) & 0xff);
		header[32] = (byte) (bitrate / 8); // block align
		header[33] = 0;
		header[34] = (byte) bitrate; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLength & 0xff);
		header[41] = (byte) ((totalAudioLength >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLength >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLength >> 24) & 0xff);

		return header;
	}

}
