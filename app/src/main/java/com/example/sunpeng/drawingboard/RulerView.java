package com.example.sunpeng.drawingboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunpeng on 2017/11/13.
 */

public class RulerView extends View {

    private static final double PI2 = Math.PI * 2;
    private Paint mPaint;
    private static final int STROKE_WIDTH = 20;
    private int mLineColor = Color.YELLOW;
    private TouchMode mTouchMode = TouchMode.SINGLE_TOUCH;
    private PointF mLastP0,mLastP1;
    private Matrix mMatrix = new Matrix();
    private float mTranX = 0.f,mTranY = 0.f;
    private double mRotateDegree = 0.f; //算出来的是顺时针旋转的角度
    private PointF mPivot,mPivot_1;  //mPivot_1:轴心点随canvas旋转之后，在canvas坐标系的坐标
    private int mRulerTop,mRulerLeft,mRulerRight,mRulerBottom;
    private PointF p1,p2,p3,p4; //最开始的时候，尺子的四个顶点的坐标（没有平移，也没有旋转）
    private PointF p5,p6,p7,p8; //每次绘制完，尺子的四个顶点坐标
    private static int TOUCH_SLOP;
    private boolean mFirstDraw = true;
    private Paint mBoardPaint;
    private Paint mDialPaint;   //刻度画笔
    private static final float DIAL_SPACING = 30; // 刻度间距
    private static final float DIAL_LENGTH = 20;  //小刻度
    private static final float DIAL_LENGTH_LONG = 40;  //大刻度
    private List<Dial> mLeftDials,mRightDials;
    private Rect mRulerRect;
    private OnDrawFinishListener mOnDrawFinishListener;
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
//        mPaint.setStrokeJoin(Paint.Join.ROUND);
//        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mPaint.setStrokeWidth(STROKE_WIDTH);
        mPaint.setColor(mLineColor);

        mBoardPaint = new Paint(mPaint);
        mBoardPaint.setColor(Color.RED);

        mDialPaint = new Paint(mPaint);
        mDialPaint.setStrokeWidth(5);
        mDialPaint.setColor(Color.BLACK);

        mLastP0 = new PointF();
        mLastP1 = new PointF();
        
        mPivot = new PointF();
        mPivot_1 = new PointF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mFirstDraw){

            mRulerLeft = getWidth() /2 - 150;
            mRulerTop = -getHeight();
            mRulerRight = getWidth() /2 + 150;
            mRulerBottom = getHeight() * 2;

            mFirstDraw = false;

            p1 = new PointF(mRulerLeft,mRulerTop);
            p2 = new PointF(mRulerRight,mRulerTop);
            p3 = new PointF(mRulerLeft,mRulerBottom);
            p4 = new PointF(mRulerRight,mRulerBottom);

            mRulerRect = new Rect(mRulerLeft, mRulerTop, mRulerRight, mRulerBottom);

            initDials();
        }

        canvas.save();
        canvas.translate(mTranX,mTranY);
        canvas.rotate((float) Math.toDegrees(mRotateDegree),mPivot.x,mPivot.y);
        super.onDraw(canvas);
        canvas.drawRect(mRulerLeft, mRulerTop, mRulerRight, mRulerBottom,mPaint);
        //画刻度
        drawDials(canvas);
        //canvas是一个独立的坐标系，一开始跟view的坐标系是重合的，但是平移选装之后，canvas的坐标系也随着平移旋转，view的坐标系不变（重要）

        //算出尺子的四个顶点在view坐标系的坐标
        p5 = getPointAfterRotateAndTrans(p1,-mTranX,-mTranY, mRotateDegree,mPivot.x,mPivot.y);
        p6 = getPointAfterRotateAndTrans(p2,-mTranX,-mTranY, mRotateDegree,mPivot.x,mPivot.y);
        p7 = getPointAfterRotateAndTrans(p3,-mTranX,-mTranY, mRotateDegree,mPivot.x,mPivot.y);
        p8 = getPointAfterRotateAndTrans(p4,-mTranX,-mTranY, mRotateDegree,mPivot.x,mPivot.y);


        canvas.restore();

//        canvas.drawLine(p6.x,p6.y,p8.x,p8.y,mBoardPaint);


        if(mOnDrawFinishListener != null){
            mOnDrawFinishListener.onDrawFinish();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        final float x0 = event.getX(0);
        final float y0 = event.getY(0);
        Log.i("xy","x:" + String.valueOf(x0) + "====y:" + String.valueOf(y0));
        switch (action){
            case MotionEvent.ACTION_DOWN:
                //需要把触摸点的坐标转换成 旋转平移后的canvas坐标系的坐标
                PointF p = new PointF(x0,y0);
                p = getPointAfterRotateAndTrans1(p,mTranX,mTranY, PI2 - mRotateDegree,mPivot.x,mPivot.y);
                Region region = new Region(mRulerRect);
                if(!region.contains((int)p.x,(int)p.y))
                    return false;
                mTouchMode = TouchMode.SINGLE_TOUCH;
                mLastP0.set(x0,y0);
                break;
            case MotionEvent.ACTION_MOVE:
                if(mTouchMode == TouchMode.SINGLE_TOUCH){
                    float dx = x0 - mLastP0.x;
                    float dy = y0 - mLastP0.y;
                    mTranX += dx;
                    mTranY += dy;
                    mLastP0.set(x0,y0);
                    invalidate();
                }else if(mTouchMode == TouchMode.MULTITOUCH){
                    final float x1 = event.getX(1);
                    final float y1 = event.getY(1);
                    Line lastLine = new Line(mLastP0,mLastP1);
                    Line currLine = new Line(x0,y0,x1,y1);
                    //求出旋转的角度(顺时针)
                    mRotateDegree += currLine.getTwoLineDegree(lastLine);
                    float dx0 = x0 - mLastP0.x;
                    float dy0 = y0 - mLastP0.y;
                    float dx1 = x1 - mLastP1.x;
                    float dy1 = y1 - mLastP1.y;
                    float dxAva = (dx0 + dx1) / 2;
                    float dyAva = (dy0 + dy1) / 2;
                    mTranX += dxAva;
                    mTranY += dyAva;

                    mPivot.x = (x0 + x1) / 2;
                    mPivot.y = (y0 + y1) / 2;

                    mLastP0.set(x0,y0);
                    mLastP1.set(x1,y1);
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() < 3){
                    mTouchMode = TouchMode.SINGLE_TOUCH;
                }
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

    /**
     * 初始化刻度集合
     */
    private void initDials(){
        mLeftDials = new ArrayList<>();
        mRightDials = new ArrayList<>();

        float length = p3.y - p1.y;

        int count = Math.round(length / DIAL_SPACING);

        for(int i = 0 ; i < count; i++){
            float dialLength;
            if(i % 5 == 0){
                dialLength = DIAL_LENGTH_LONG;
            }else {
                dialLength = DIAL_LENGTH;
            }
            Dial leftDial = new Dial();
            leftDial.startX = p1.x - STROKE_WIDTH / 2;
            leftDial.startY = p1.y + i * DIAL_SPACING;
            leftDial.stopX = p1.x + dialLength - STROKE_WIDTH / 2;
            leftDial.stopY = p1.y + i * DIAL_SPACING;

            mLeftDials.add(leftDial);


            Dial rightDial = new Dial();
            rightDial.startX = p2.x + STROKE_WIDTH / 2;
            rightDial.startY = p2.y + i * DIAL_SPACING;
            rightDial.stopX = p2.x - dialLength + STROKE_WIDTH / 2;
            rightDial.stopY = p2.y + i * DIAL_SPACING;

            mRightDials.add(rightDial);

        }
    }

    private void drawDials(Canvas canvas){
        for(int i = 0; i < mLeftDials.size(); i ++){
            Dial leftDial = mLeftDials.get(i);
            Dial rightDial = mRightDials.get(i);

            canvas.drawLine(leftDial.startX,leftDial.startY,leftDial.stopX,leftDial.stopY,mDialPaint);
            canvas.drawLine(rightDial.startX,rightDial.startY,rightDial.stopX,rightDial.stopY,mDialPaint);
        }
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

    private boolean isTouchInRect(float x, float y, Rect rect) {
        if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
            return true;
        }
        return false;
    }

    //先旋转再平移(逆时针)
    private PointF getPointAfterRotateAndTrans(PointF pointF,float tranX, float tranY, double degrees,float pivotX,float pivotY){
        float x = pointF.x ;
        float y = pointF.y ;

        float x0 = (float) ((x - pivotX) * Math.cos(degrees) - (y - pivotY) * Math.sin( degrees)) + pivotX;
        float y0 = (float) ((y - pivotY) * Math.cos( degrees) + (x - pivotX) * Math.sin(degrees)) + pivotY;

        return new PointF(x0 - tranX,y0 - tranY);
    }

    //先平移再旋转（逆时针）
    private PointF getPointAfterRotateAndTrans1(PointF pointF,float tranX, float tranY, double degrees,float pivotX,float pivotY){

        float x = pointF.x - tranX;
        float y = pointF.y - tranY;
        float x0 = (float) ((x - pivotX) * Math.cos(degrees) - (y - pivotY) * Math.sin( degrees)) + pivotX;
        float y0 = (float) ((y - pivotY) * Math.cos( degrees) + (x - pivotX) * Math.sin(degrees)) + pivotY;

        return new PointF(x0,y0);
    }

    public Line getLine1(){
        return new Line(p5,p7);
    }

    public Line getLine2(){
        return  new Line(p6, p8);
    }

    public void setOnDrawFinishListener(OnDrawFinishListener listener){
        mOnDrawFinishListener = listener;
    }
    public interface OnDrawFinishListener{
        void onDrawFinish();
    }
}
