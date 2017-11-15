package com.example.sunpeng.drawingboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by sunpeng on 2017/11/13.
 */

public class RulerView extends View {

    private Paint mPaint;
    private int mLineWidth = 20;
    private int mLineColor = Color.YELLOW;
    private TouchMode mTouchMode = TouchMode.SINGLE_TOUCH;
    private PointF mLastP0,mLastP1;
    private Matrix mMatrix = new Matrix();
    private float mTranX = 0.f,mTranY = 0.f;
    private float mRotateDegree = 0.f;
    private float mPivotX = 0.f,mPivotY = 0.f;
    private static int TOUCH_SLOP;
    public RulerView(Context context) {
        super(context);
        init();
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        TOUCH_SLOP = ViewConfiguration.getTouchSlop();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setFilterBitmap(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mPaint.setStrokeWidth(mLineWidth);
        mPaint.setColor(mLineColor);

        mLastP0 = new PointF();
        mLastP1 = new PointF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setPivotX(getWidth() /2);
        setPivotY(getHeight() /2);
        canvas.save();
        canvas.translate(mTranX,mTranY);
        canvas.rotate(mRotateDegree);
        super.onDraw(canvas);
        canvas.drawRect(getWidth()/ 2 - 150,-2000,getWidth() / 2 + 150,5000,mPaint);
        Log.i("de",String.valueOf(mRotateDegree));
        canvas.restore();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        final float x = event.getX(0);
        final float y = event.getY(0);
        switch (action){
            case MotionEvent.ACTION_DOWN:
                mTouchMode = TouchMode.SINGLE_TOUCH;
                mLastP0.set(x,y);
                break;
            case MotionEvent.ACTION_MOVE:
                if(mTouchMode == TouchMode.SINGLE_TOUCH){
                    float dx = x - mLastP0.x;
                    float dy = y - mLastP0.y;
                    mTranX += dx;
                    mTranY += dy;
                    mLastP0.set(x,y);
                    invalidate();
                }else if(mTouchMode == TouchMode.MULTITOUCH){
                    Line lastLine = new Line(mLastP0,mLastP1);
                    Line currLine = new Line(x,y,event.getX(1),event.getY(1));
                    mRotateDegree += currLine.getTwoLineDegree(lastLine);
                    mLastP0.set(x,y);
                    mLastP1.set(event.getX(1),event.getY(1));
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() < 3)
                    mTouchMode = TouchMode.SINGLE_TOUCH;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchMode = TouchMode.MULTITOUCH;
                mLastP1.set(event.getX(1),event.getY(1));
                break;
            default:
                break;

        }
        return true;
    }


    private float distance(float x0, float y0, float x1, float y1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        /** 使用勾股定理返回两点之间的距离 */
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private PointF mid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }
}
