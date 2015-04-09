package com.fhtrier.voiceDiary;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

import 	java.lang.Math;

public class DSP {
	public static double signal2HarmonicRatio(double[] x,int nFFT,int ovl,double fs, double f0, int w)
	{
		double[][] spec = spectrogram(x,nFFT,ovl);
		double He = getHarmonicEnergy(spec,fs,f0,w);
		double Se = getSpectralEnergy(spec);
		return Se-He;
	}
	
	public static double[] conv(double[] x1, double[] x2)
	{
		int n1 = x1.length;
		int n2 = x2.length;
		double[] res = new double[n1+n2];
		int k = 0;

		for(int i = 0; i < n1+n2-1; ++i)
		{
			for(int j = 0; j < k; ++j)
			{
				res[i] += x1[j]+x2[i-j];
			}
			k++;
		}
		return res;		 
	}

	public static double onePoleFilt(double z, double z_1, double tau, double fs)
	{
		double alpha = Math.exp(-1/(fs*tau));
		return alpha*z-(1-alpha)*z_1;		
	}
	
	public static double[] corr(double[] x1, double[] x2)
	{
		int n1 = x1.length;
		int n2 = x2.length;
		double[] res = new double[n1+n2];
		int k = 0;

		for(int i = 0; i < n1+n2-1; ++i)
		{
			for(int j = 0; j < k; ++j)
			{
				res[i] += x1[j]+x2[i+j];
			}
			k++;
		}
		return res;		 
	}

	public static double[][] spectrogram(double[] x, int nFFT, int ovl)
	{
		Haming haming = new Haming(nFFT);
		double[] ham = haming.getArray();
		int n = x.length;
		Double overlap = Math.ceil(nFFT*ovl/100);
		int hop =nFFT-overlap.intValue();
		int nHop =  Math.round(n/nFFT); 
		double[][] res = new double[nHop][2*nFFT];

		DoubleFFT_1D fft = new DoubleFFT_1D(nFFT);
		int pos=0;
		double[] fftArray = new double[2*nFFT];

		for(int i = 0; i < nHop; ++i)
		{
			pos=i*hop;
			for(int j = 0; j < nFFT; ++j)
			{
				fftArray[2*j] = ham[j]*x[pos+j];				
			}
			fft.complexForward(fftArray);
			for(int k = 0; k < 2*nFFT; ++k)
			{
				res[i][k] = fftArray[k]/nFFT;				
			}
		}
		return res;
	}

	public static double[][] specAbs(double[][] spec)
	{
		int N = spec.length;
		int n2FFT     = spec[0].length;		
		double[][] res = new double[N][n2FFT/2];
		for(int i = 0; i < N; ++i)
		{
			for(int j = 0; j < n2FFT/2; ++j)
			{
				res[i][j] = Math.sqrt(spec[i][j * 2] * spec[i][j * 2] + spec[i][j * 2 + 1] * spec[i][j * 2 + 1]);
			}
		}
		return res;
	}

	public static double getHarmonicEnergy(double[][] spec, double fs, double f0, int w)
	{
		double[][] spcAbs = specAbs(spec);
		int nFFT = spcAbs[0].length;
		int N    = spcAbs.length;
		Double bin = f0/fs*nFFT;
		double[] harm= new double[N];
		double res=0;
		int j = 1;
		int binPos = bin.intValue();

		for(int k = 0; k < N;++k)
		{   harm[k] = 0;
			while(j*binPos<nFFT/2-(w/2))
			{
				for(int i=j*binPos-Math.round(w/2);i<=j*binPos+Math.round(w/2);i++)
				{
					harm[k] += (2* spcAbs[k][i] * spcAbs[k][i]);
				}
				j++;
			}	
			harm[k]=Math.sqrt(harm[k]/nFFT);
			j=1;
		}
		
		for(int i=0;i<N;++i)
		{
			res+=harm[i];
		}
		res/=N;
		res = 10*Math.log10(res);
		return res;		
	}

	public static double getSpectralEnergy(double[][] spec)
	{
		double[][] absSpec = specAbs(spec);
		int nFFT = absSpec[0].length;
		int N    = absSpec.length;
		double res = 0;
		double temp;
		
		for(int k=0;k<N;++k)
		{
			temp = 0;
			for(int j=0;j<nFFT/2;++j)
			{
				temp+=2*absSpec[k][j]*absSpec[k][j];
			}
			temp = Math.sqrt(temp/nFFT);
			res+=temp;
		}
		return 10*Math.log10(res/N);
	}
}