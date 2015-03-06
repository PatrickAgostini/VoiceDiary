package com.fhtrier.voiceDiary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;

public class SliderView extends View
{
    private static final int PERVENTAGE_FOR_GREENAREA = 30;
    private static final int DIFFERENCE_SIDE = 34;

    private Bitmap unscaledBar = BitmapFactory.decodeResource(this.getResources(), R.drawable.viewbar_bar);
    private Bitmap unscaledCursor = BitmapFactory.decodeResource(this.getResources(), R.drawable.viewbar_cursor);
    private Matrix matrix = new Matrix();
    private int parentWidth;
    private int position = 10;
    private int greenArea = 31;
    private float scale;

    public SliderView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public SliderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    public SliderView(Context context)
    {
        super(context);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        parentWidth = MeasureSpec.getSize(widthMeasureSpec);
       
        scale = (float)parentWidth / unscaledBar.getWidth();

        matrix.reset();
        matrix.postScale(scale, scale);
        
        this.setMeasuredDimension(parentWidth, (int)(unscaledBar.getHeight() * scale));
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        
        canvas.drawBitmap(unscaledBar, matrix, null);

        float greenAreaWidth = SliderView.PERVENTAGE_FOR_GREENAREA / 100f * (parentWidth - 2 * SliderView.DIFFERENCE_SIDE);
        float valueDistance = (float)greenAreaWidth / greenArea;
        
        int cursorX = parentWidth / 2 - (int)(unscaledCursor.getWidth() * scale / 2);
        cursorX += position * valueDistance;

        cursorX = Math.min(cursorX, parentWidth - SliderView.DIFFERENCE_SIDE);
        cursorX = Math.max(cursorX, SliderView.DIFFERENCE_SIDE);
        
        
        matrix.postTranslate(cursorX, 0);
        canvas.drawBitmap(unscaledCursor, matrix, null);
    }


    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
        this.invalidate();
    }

    public int getGreenArea()
    {
        return greenArea;
    }

    public void setGreenArea(int greenArea)
    {
        if (greenArea < 0)
        {
            throw new IllegalArgumentException();
        }

        this.greenArea = greenArea;
        this.invalidate();
    }
}
