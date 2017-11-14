package com.example.sunpeng.drawingboard;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by sunpeng on 2017/11/14.
 */

public class ScaleTestImageView extends AppCompatImageView {

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private float mPivotX = 0.f,mPivotY = 0.f;
    private Matrix mMatrix;
    public ScaleTestImageView(Context context) {
        super(context);
        init(context);
    }

    public ScaleTestImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScaleTestImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context){
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mMatrix = new Matrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
//        canvas.scale(mScaleFactor,mScaleFactor);
        canvas.scale(mScaleFactor,mScaleFactor,mPivotX,mPivotY);
        super.onDraw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 5.0f));
            Log.i("scale",String.valueOf(detector.getScaleFactor()));
            mPivotX = detector.getFocusX();
            mPivotY = detector.getFocusY();
            invalidate();
            return true;
        }
    }
}
