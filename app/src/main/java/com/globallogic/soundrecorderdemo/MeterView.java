package com.globallogic.soundrecorderdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MeterView extends View {
    static final float PIVOT_RADIUS = 3.5f;
    static final float PIVOT_Y_OFFSET = 10f;
    static final float SHADOW_OFFSET = 2.0f;
    static final float DROPOFF_STEP = 0.18f;
    static final float SURGE_STEP = 0.35f;
    static final long  ANIMATION_INTERVAL = 70;

    MediaRecorder mRecorder;
    private Paint mPaint;
    private int mCurrentValue;

    private boolean isRecording = false;


    private static final String TAG = "MeterView";


    public MeterView(Context context) {
        super(context);
        init(context);
    }


    public MeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    void init(Context context) {
        Log.w(TAG, "init: " );
        Drawable background = context.getResources().getDrawable(android.R.color.darker_gray);
        //setBackground(background);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);

        mRecorder = null;

        mCurrentValue = 0;
    }

    public void setRecorder(MediaRecorder recorder) {
        mRecorder = recorder;
        invalidate();
    }

    public void setIsRecording(boolean isRecording){
        this.isRecording=isRecording;
        invalidate();
    }
    public boolean isRecording(){
        return isRecording;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float value = 0.0f;
        if(mRecorder!=null && isRecording) {
            value = mRecorder.getMaxAmplitude();
        }
        Log.w(TAG, "onDraw: value="+value );
        int colorValue = (int) ((int)255*value/32767.0f);
        Paint paint = new Paint();
        int green = 255-colorValue;
        //if(green<0) green=0;
        int red = colorValue;
        if(red<128) red=0;
        paint.setColor(Color.rgb(red,green,0));
        paint.setStrokeWidth(getWidth());

        float currentLength=getHeight()*value/32767.0f;
        canvas.drawLine(getWidth()/2,getHeight(),getWidth()/2,getHeight()-currentLength,paint);
        if (mRecorder != null  && isRecording){
            postInvalidateDelayed(ANIMATION_INTERVAL);
            Log.w(TAG, "postInvalidateDelayed: " );
        }else {
            Log.w(TAG, "postInvalidateDelayed: NO" );

        }
    }


}
