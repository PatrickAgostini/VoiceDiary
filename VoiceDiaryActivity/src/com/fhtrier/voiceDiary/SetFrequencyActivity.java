package com.fhtrier.voiceDiary;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.holoeverywhere.app.AlertDialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.fhtrier.voiceDiary.NoiseMeterDialog.NoiseMeterDialogListener;

public class SetFrequencyActivity extends SherlockActivity implements NoiseMeterDialogListener
{
    private static final int WAIT_TIME = 3;

    private RecordingThread recThread;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");
    private Timer timer;

    private AlertDialog completeDialog;
    private AlertDialog startDialog;
    
    NoiseMeterDialog noiseMeterDialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_frequency);

        this.getSupportActionBar().setIcon(R.drawable.actionbar_logo);
        this.getSupportActionBar().setTitle("");

        completeDialog = this.createCompleteDialog();

        String caller = this.getIntent().getStringExtra("caller");
        if (caller != null && VoiceDiaryActivity.class.getName().equals(caller))
        {
            startDialog = this.createFirstStartDialog();
        }
        else
        {
            startDialog = this.createStartDialog();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (!this.completeDialog.isShowing())
        {
            startDialog.show();
        }
    }

    @Override
    public void onPause()
    {
        this.stopRecording();
        super.onPause();

        if (timer != null)
        {
            timer.cancel();
        }
    }

    private void showStartMessage()
    {
        Toast.makeText(this, this.getString(R.string.set_frequency_show_waittime, SetFrequencyActivity.WAIT_TIME), SetFrequencyActivity.WAIT_TIME * 1000).show();

        TextView tv = (TextView) findViewById(R.id.recording_time);
        tv.setText("");
        tv = (TextView) findViewById(R.id.frequencyText);
        tv.setText("");
        timer = new Timer();
        timer.schedule(new TimerTask()
        {

            @Override
            public void run()
            {
                SetFrequencyActivity.this.startRecording();
            }
        }, SetFrequencyActivity.WAIT_TIME * 1000);
    }

    private void startRecording()
    {
        if (recThread != null)
        {
            recThread.interrupt();
            recThread = null;
        }
        recThread = new RecordingThread(new SetFrequencyAssistant(this));
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

    public void recordingStarted()
    {
        TextView tv = (TextView) findViewById(R.id.recording_state);
        tv.setText(getString(R.string.recording_started));
    }

    public void recordingStopped()
    {
        TextView tv = (TextView) findViewById(R.id.recording_state);
        tv.setText(getString(R.string.recording_stopped));
    }

    public void restTime(long restTime)
    {
        TextView tv = (TextView) findViewById(R.id.recording_time);
        tv.setText(decimalFormat.format(restTime / 1000F) + " sec.");
    }

    public void frequency(int frequency)
    {
        TextView textView = (TextView) findViewById(R.id.frequencyText);
        textView.setText(frequency + " HZ");

        textView.setTextColor(Color.GREEN);
    }

    public void resultFrequency(int frequency)
    {
        MyApplication.getSqLiteDatabase().execSQL("UPDATE user SET frequency = " + frequency);

        completeDialog.setMessage(this.getString(R.string.set_frequency_end_dialog_message, frequency));
        completeDialog.show();
    }

    @Override
    public void onBackPressed()
    {
        String caller = this.getIntent().getStringExtra("caller");

        if (caller == null || VoiceDiaryActivity.class.getName().equals(caller))
        {
            this.finish();
        }
        else
        {
            SetFrequencyActivity.this.startActivity(new Intent(SetFrequencyActivity.this, VoiceDiaryActivity.class));
            SetFrequencyActivity.this.finish();
        }
    }

    private AlertDialog createCompleteDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.set_frequency_end_dialog_title));
        builder.setCancelable(true);
        builder.setPositiveButton(this.getString(R.string.set_frequency_dialog_ok), new OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //SetFrequencyActivity.this.onBackPressed();
                SetFrequencyActivity.this.startActivity(new Intent(SetFrequencyActivity.this, VoiceDiaryActivity.class));
                SetFrequencyActivity.this.finish();
            }
        });

        builder.setNegativeButton(this.getString(R.string.set_frequency_end_dialog_repeat), new OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            	SetFrequencyActivity.this.noiseMeterDialog = new NoiseMeterDialog(SetFrequencyActivity.this);
            	SetFrequencyActivity.this.noiseMeterDialog.show();
            }
        });

        return builder.create();
    }

    private AlertDialog createStartDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.set_frequency_start_dialog_title));
        builder.setMessage(this.getString(R.string.set_frequency_start_dialog_message));
        builder.setCancelable(false);
        builder.setPositiveButton(this.getString(R.string.set_frequency_dialog_ok), new OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            	SetFrequencyActivity.this.noiseMeterDialog = new NoiseMeterDialog(SetFrequencyActivity.this);
            	SetFrequencyActivity.this.noiseMeterDialog.show();
            }
        });

        builder.setNegativeButton(this.getString(R.string.set_frequency_dialog_back), new OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                SetFrequencyActivity.this.onBackPressed();
            }
        });

        return builder.create();
    }

    private AlertDialog createFirstStartDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.set_frequency_start_dialog_title));
        builder.setMessage(this.getString(R.string.set_frequency_start_dialog_start_message));
        builder.setCancelable(true);
        builder.setPositiveButton(this.getString(R.string.set_frequency_dialog_ok), new OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //SetFrequencyActivity.this.showStartMessage();
            	SetFrequencyActivity.this.noiseMeterDialog = new NoiseMeterDialog(SetFrequencyActivity.this);
            	SetFrequencyActivity.this.noiseMeterDialog.show();
            }
        });

        builder.setNegativeButton(this.getString(R.string.set_frequency_dialog_later), new OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                SetFrequencyActivity.this.finish();
            }
        });

        return builder.create();
    }
    
    public void onProceed(short[] ringBuffer)
    {
    	SetFrequencyActivity.this.showStartMessage();

    }
    public void onAbort()
    {
    	
    }
}