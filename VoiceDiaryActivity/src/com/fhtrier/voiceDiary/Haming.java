package com.fhtrier.voiceDiary;

public class Haming
{
    private final double[] array;
    
    public Haming(int size)
    {
        array = new double[size];
        
        for (int i = 0; i < array.length; ++i)
        {
            array[i] = 0.54 + 0.46 * Math.cos((2 * Math.PI * (i - size / 2D)) / size);
        }
    }
    
    public double[] getArray()
    {
        return array.clone();
    }
    
    public double getValue(int i)
    {
        return array[i];
    }
}
