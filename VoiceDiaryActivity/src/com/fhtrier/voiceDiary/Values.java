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
    
    public static String[] numbers = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"};
    public static String[] letters = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    
    public static String[] Phone_Id = {"phone_name","current_clinic"}; 

    public static String[] minutes   = {"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59"};
    public static String[] hours   = {"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23"};
    public static String[] intervals   = {"01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"};

    public static String adminPassword = "bW92ZDIwMTU=";
    
}