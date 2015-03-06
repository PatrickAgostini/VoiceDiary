package com.fhtrier.voiceDiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

public class NoiseMeterDialog extends AlertDialog {

	Context context;
	RecordActivity recordActivity;
	NoiseMeterThread noiseMeterThread;

	LayoutInflater li;
	View promptsView ;

	Button startButton;

	double  z_1=0;

	boolean Abort=false;
	boolean levelAcceptance = false;

	short[] ringBuffer;

	public NoiseMeterDialog(Context context,RecordActivity recordActivity) {
		super(context);
		this.context = context;
		this.recordActivity = recordActivity;
		this.li = LayoutInflater.from(context);
		this.promptsView = li.inflate(R.layout.activity_noise_meter, null);
		setView(promptsView);
		this.setTitle(R.string.noise_meter);

		setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.button_start) , (new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// this will never be called
			}
		}));
		setButton(AlertDialog.BUTTON_NEGATIVE,context.getString(R.string.button_cancel) , (new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// this will never be called
			}
		}));
		return;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.startButton = getButton(AlertDialog.BUTTON_POSITIVE);

		getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {				
				if(NoiseMeterDialog.this.levelAcceptance)
				{
					NoiseMeterDialog.this.Abort = true;
					NoiseMeterDialog.this.recordActivity.State=0;
					NoiseMeterDialog.this.recordActivity.noiseBuffer = NoiseMeterDialog.this.ringBuffer;
					NoiseMeterDialog.this.recordActivity.start();
					dismiss();
				}else
				{
					Toast.makeText(NoiseMeterDialog.this.getContext(), R.string.noise_level_too_high, Toast.LENGTH_SHORT).show();
				}
			}});
		getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NoiseMeterDialog.this.Abort = true;
				NoiseMeterDialog.this.recordActivity.finish();
				dismiss();
			}
		});

	}

	@Override
	public void onBackPressed()
	{
		this.dismiss();
		this.recordActivity.finish();
	}


	public void level(Double level)
	{
		this.promptsView = this.li.inflate(R.layout.activity_noise_meter, null);
		LevelMeterView meterBar = (LevelMeterView) findViewById(R.id.lvlMeter1);
		TextView noise = (TextView) findViewById(R.id.noise_text_state);
		meterBar.setLevel(level);

		// db_phone      db_in
		//  BreitBand Rauschen
		// 35            30
		// 41            50
		// 60            70
		// 70            80
		// 80            90


		noise.setTextSize(20) ;
		if(level<65)
		{
			noise.setText(R.string.good_noise);
			noise.setTextColor(Color.GREEN);
			this.startButton.setEnabled(true);
			this.levelAcceptance = true;
		}else if(level<80)
		{
			noise.setTextColor(Color.YELLOW);
			noise.setText(R.string.moderate_noise);
			this.startButton.setEnabled(true);
			this.levelAcceptance = false;
		}
		else
		{
			noise.setTextColor(Color.RED);
			noise.setText(R.string.bad_noise);
			this.startButton.setEnabled(false);
			this.levelAcceptance = false;

		}
		//noise.setText(level.toString());
		setView(promptsView);
	}

	void startRecording()
	{
		if (this.noiseMeterThread != null)
		{
			this.noiseMeterThread.interrupt();
			this.noiseMeterThread = null;
		}
		this.noiseMeterThread = new NoiseMeterThread(this.recordActivity, this);
	}

	public void stopRecording()
	{
		if (this.noiseMeterThread != null)
		{
			this.noiseMeterThread.interrupt();
			this.noiseMeterThread = null;
		}
	}

	public void recordingError()
	{
		Toast toast = Toast.makeText(this.context, context.getString(R.string.recording_error), Toast.LENGTH_LONG);
		toast.show();

		this.onBackPressed();
	}

	public void NoiseMeterLevel(short[] array, short[] ringBuffer)
	{
		double db = 0;
		this.ringBuffer = ringBuffer;
		for(int i=0;i<array.length;i++)
		{
			db += (double) array[i]* (double) array[i];
		}
		if(db!=0)
		{
			db = 10*Math.log10(db/array.length);
			this.z_1 = DSP.onePoleFilt(db, this.z_1, 0.5, Values.SAMPLE_RATE);
			level((double)(Math.round(this.z_1*100))/100);
		}
	}

}

