package com.fhtrier.voiceDiary;

public interface Values
{
    //Recording Values
    public static final int SAMPLE_RATE = 44100;
    public static final int BUFFER_SIZE = 4096;
    //Frequenz Analyse
    public static final int MAX_FREQUENCY_DEVITATION = 15;
    //Server
    public static final String ADDRESS = "143.93.53.44";
    public static final int PORT = 3099;
    
    //DSP
    public static int nFFT = 4096;
    public static int ovl  = 50;
    public static int w    = 1;
    
    public static String[] numbers = {"1","2","3","4","5","6","7","8","9"};
    public static String[] letters = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    
    public static String[] Phone_Id = {"phone_name","current_clinic"}; 


    public static String adminPassword = "bW92ZDIwMTU=";
    
}