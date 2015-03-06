package com.fhtrier.voiceDiary;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class AudioAnalyzer
{
    private final static Haming haming = new Haming(Values.BUFFER_SIZE);

    public static int getFrequency(short[] s)
    {
        double[] fftArray = new double[s.length * 2];

        for (int i = 0; i < s.length; ++i)
        {
            fftArray[i * 2] = s[i];
            fftArray[i * 2] *= haming.getValue(i);
        }

        DoubleFFT_1D fft = new DoubleFFT_1D(s.length);
        fft.complexForward(fftArray);

        double[] absArray = new double[fftArray.length / 2];

        for (int i = 0; i < absArray.length; ++i)
        {
            absArray[i] = Math.sqrt(fftArray[i * 2] * fftArray[i * 2] + fftArray[i * 2 + 1] * fftArray[i * 2 + 1]);
        }

        int start = (int) (50D * Values.BUFFER_SIZE / Values.SAMPLE_RATE);
        int end = (int) (500D * Values.BUFFER_SIZE / Values.SAMPLE_RATE);

        double min = absArray[start];
        double max = absArray[start];

        for (int i = start + 1; i < end; ++i)
        {
            min = Math.min(min, absArray[i]);
            max = Math.max(max, absArray[i]);
        }

        double m = (max - min) / 20;

        int i = start;
        for (i = start; i < end && absArray[i] < m; ++i)
            ;

        max = fftArray[i];

        while (max < absArray[i + 1] && i < absArray.length - 1)
        {
            ++i;
            max = absArray[i];
        }

        return (int) ((double) i * Values.SAMPLE_RATE / Values.BUFFER_SIZE);
    }
}
