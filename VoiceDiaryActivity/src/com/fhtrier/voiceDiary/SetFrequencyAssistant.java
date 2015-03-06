package com.fhtrier.voiceDiary;

import java.util.ArrayList;
import java.util.Collections;

public class SetFrequencyAssistant implements IRecordingAssistant
{
    private static final int RECORDINGTIME = 3000;
    
    private final SetFrequencyActivity setFrequencyActivity;
    private final ArrayList<Integer> frequencyArray = new ArrayList<Integer>();
    
    private int counter = 0;
    
    public SetFrequencyAssistant(SetFrequencyActivity setFrequencyActivity)
    {
        this.setFrequencyActivity = setFrequencyActivity;
    }

    @Override
    public void recordingStarted()
    {
        setFrequencyActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                setFrequencyActivity.recordingStarted();
                setFrequencyActivity.restTime(SetFrequencyAssistant.RECORDINGTIME);
            }
        });
    }

    @Override
    public void recordingStopped()
    {
        setFrequencyActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                setFrequencyActivity.recordingStopped();
                if (!isRecording())
                {
                    setFrequencyActivity.resultFrequency(frequencyArray.get(frequencyArray.size() / 2));
                }
            }
        });
    }
    public void noiseRecArray(short[] array)
	{
	
	}
    
    public boolean isPrerecord()
	{
    	return false;
	}
	
	public boolean isPostrecord()
	{
		return false;
	}
	
    @Override
    public void recordingError(Exception e)
    {
        setFrequencyActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                setFrequencyActivity.recordingError();
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
        return RECORDINGTIME - (1000D / Values.SAMPLE_RATE * counter) > 0;
    }

    public void noiseRecArrayPre(short[] array)
	{
	}
	public void noiseRecArrayPost(short[] array)
	{
	}
	
    @Override
    public void newInputArray(short[] array)
    {
        frequencyArray.add(AudioAnalyzer.getFrequency(array));
        Collections.sort(frequencyArray);
        
        counter += array.length;
        
        setFrequencyActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                setFrequencyActivity.frequency(frequencyArray.get(frequencyArray.size() / 2));
                setFrequencyActivity.restTime(Math.max((long) (RECORDINGTIME - (1000D / Values.SAMPLE_RATE * counter)), 0));
            }
        });
    }
}
