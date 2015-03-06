package com.fhtrier.voiceDiary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class LevelMeterView extends View {

	Paint paint = new Paint();
	Rect rect = new Rect() ;
	RectF rectF = new RectF();

	int borderHeight = 330;
	int borderWidth  = 150;
	int sideMargin   = 0;
	int verticalMargin = 0;

	int boxMargin = sideMargin+10;
	int boxVertical = verticalMargin+20;

	int boxWidth = boxMargin+borderWidth-20;
	int boxHeight = boxVertical+20;

	double level = 30;	
	double levelMin = 30;
	double levelMax = 80;


	public LevelMeterView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public LevelMeterView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public LevelMeterView(Context context)
	{
		super(context);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		getBorder(this.paint,canvas);
		setBars(this.paint, canvas);
		setBarsOn(this.paint,canvas,this.level);
	}

	public void setLevel(double level)
	{
		this.level = level;
		this.invalidate();
	}

	public void getBorder(Paint paint, Canvas canvas)
	{
		rect = new Rect(this.sideMargin,this.verticalMargin,(int) this.borderWidth,(int) this.borderHeight);
		rectF = new RectF(rect);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.rgb(0,25,51));
		paint.setStrokeWidth(5);
		canvas.drawRoundRect(rectF, 10, 10, paint);
	}

	public void setBars(Paint paint, Canvas canvas)
	{
		for(int i=0;i<12;i++)
		{
			if(i<2)
			{
				paint.setColor(Color.rgb(102,0, 0));
			}else if(i<5)
			{
				paint.setColor(Color.rgb(204,204,0));
			}else
			{
				paint.setColor(Color.rgb(0,102, 0));
			}
			rect.set(this.boxMargin,this.boxHeight+5+(i-1)*25,this.boxWidth,this.boxHeight+i*25);
			rectF.set(rect);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawRoundRect(rectF, 6, 6, paint);
		}
	}
	
	public void setBarsOn(Paint paint, Canvas canvas, double Level)
	{
		int pos=0;
		if(Level<30)
		{
			pos = 10;
			
		}else if(Level<35)
		{
			pos = 8;
		}else if(Level<40)
		{
			pos = 7;
		}else if(Level<45)
		{
			pos = 6;
		}else if(Level<50)
		{
			pos = 5;
		}else if(Level<60)
		{
			pos = 4;
		}else if(Level<70)
		{
			pos = 3;
		}else if(Level<75)
		{
			pos = 2;
		}else if(Level<80)
		{
			pos = 1;
		}else if(Level<90)
		{
			pos = 0;
		}else if(Level<120)
		{
			pos = -1;
		}
			
		
		for(int i=11;i>pos;i--)
		{
			if(i<2)
			{
				paint.setColor(Color.rgb(255,51,51));
			}else if(i<5)
			{
				paint.setColor(Color.rgb(255,255,51));
			}else
			{
				paint.setColor(Color.rgb(51,255,51));
			}
			rect.set(this.boxMargin,this.boxHeight+5+(i-1)*25,this.boxWidth,this.boxHeight+i*25);
			rectF.set(rect);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawRoundRect(rectF, 6, 6, paint);
		}
	}
	
	
}
