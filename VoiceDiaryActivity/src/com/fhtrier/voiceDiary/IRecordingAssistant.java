package com.fhtrier.voiceDiary;

public interface IRecordingAssistant
{
    public void recordingStarted();
    public void recordingStopped();
    public void recordingError(Exception e);
    public void recordingInterrupt();
    public void noiseRecArrayPre(short[] array);
    public void noiseRecArrayPost(short[] array);
    public boolean isPrerecord();
    public boolean isPostrecord();
    public boolean isRecording();
    public void newInputArray(short[] array);
}
