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
    private float lastX0, lastY0,lastX1,lastY1;
    private int mRulerTop,mRulerLeft,mRulerRight,mRulerBottom;
    private PointF p1,p2,p3,p4; //最开始的时候，尺子的四个顶点的坐标（没有平移，也没有旋转）
    private PointF p5,p6,p7,p8; //每次绘制完，尺子的四个顶点坐标
    private PointF mPivot; //轴心点
    private boolean mFirstDraw = true;
    private Paint mBoardPaint;
    private Paint mDialPaint;   //刻度画笔
    private float mTranY; //相对于view自己的坐标系（非旋转平移之后的）
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

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), 5000);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mFirstDraw){

            mRulerLeft = 0;
            mRulerTop = 0;
            mRulerRight = getWidth();
            mRulerBottom = getHeight();

            mFirstDraw = false;

            p1 = new PointF(mRulerLeft,mRulerTop);
            p2 = new PointF(mRulerRight,mRulerTop);
            p3 = new PointF(mRulerLeft,mRulerBottom);
            p4 = new PointF(mRulerRight,mRulerBottom);

//            p1 = new PointF(getLeft(),getTop());
//            p2 = new PointF(getRight(),getBottom());
//            p3 = new PointF(getLeft(),getBottom());
//            p4 = new PointF(getRight(),getBottom());


            mPivot = new PointF(getWidth() / 2, getHeight() / 2);

            mRulerRect = new Rect(mRulerLeft, mRulerTop, mRulerRight, mRulerBottom);

            initDials();
        }

        canvas.drawRect(mRulerLeft, mRulerTop, mRulerRight, mRulerBottom,mPaint);
        //画刻度
        drawDials(canvas);
        //canvas是一个独立的坐标系，一开始跟view的坐标系是重合的，但是平移选装之后，canvas的坐标系也随着平移旋转，view的坐标系不变（重要）

        //算出尺子的四个顶点在view坐标系的坐标
        float tranX = getTranslationX();
        float tranY = getTranslationY();
        double radians = Math.toRadians(getRotation());

        p1.set(p1.x + tranX, p1.y + tranY);
        p2.set(p2.x + tranX, p2.y + tranY);
        p3.set(p3.x + tranX, p3.y + tranY);
        p4.set(p4.x + tranX, p4.y + tranY);

        mPivot.x += tranX;
        mPivot.y += tranY;

        p5 = getPointAfterRotateAndTrans1(p1,radians, mPivot.x,mPivot.y);
        p6 = getPointAfterRotateAndTrans1(p2,radians, mPivot.x,mPivot.y);
        p7 = getPointAfterRotateAndTrans1(p3,radians, mPivot.x,mPivot.y);
        p8 = getPointAfterRotateAndTrans1(p4,radians, mPivot.x,mPivot.y);

//        canvas.drawLine(p6.x,p6.y,p8.x,p8.y,mBoardPaint);


        if(mOnDrawFinishListener != null){
            mOnDrawFinishListener.onDrawFinish();
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        final float x0 = event.getX();
        final float y0 = event.getY();
        Log.i("event","x = " + String.valueOf(x0) + ",y = " + String.valueOf(y0));
        switch (action){
            case MotionEvent.ACTION_DOWN:
                lastX0 = x0;
                lastY0 = y0;
                break;
            case MotionEvent.ACTION_MOVE:
                if(mTouchMode == TouchMode.SINGLE_TOUCH){
                    float dx = x0 - lastX0;
                    float dy = y0 - lastY0;

                    mTranY += dy;

                    if(Math.abs (mTranY )  > 300){
                        float a = mTranY  % DIAL_SPACING;
                        dy = a - (mTranY - dy);
                        mTranY = a ;
                    }

                    double degrees = Math.toRadians(getRotation());
                    float dx1 = (float) (dx * Math.cos(degrees) - dy  * Math.sin(degrees)) ;
                    float dy1 = (float) (dy * Math.cos(degrees) + dx * Math.sin(degrees)) ;

                    float tranX = getTranslationX() +  dx1;
                    float tranY = getTranslationY() +  dy1;

                    setTranslationX(tranX);
                    setTranslationY(tranY);
                }else if(mTouchMode == TouchMode.MULTITOUCH){
                    final float x1 = event.getX(1);
                    final float y1 = event.getY(1);

                    float dx0 = x0 - lastX0;
                    float dy0 = y0 - lastY0;

                    float dx1 = x1 - lastX1;
                    float dy1 = y1 - lastY1;

                    float dx_ava = (dx0 + dx1 ) / 2 ;
                    float dy_ava = (dy0 + dy1 ) / 2 ;

                    double degrees = Math.toRadians(getRotation());
                    float dx = (float) (dx_ava * Math.cos(degrees) - dy_ava  * Math.sin(degrees)) ;
                    float dy = (float) (dy_ava * Math.cos(degrees) + dx_ava * Math.sin(degrees)) ;

                    float tranX = getTranslationX() +  dx;
                    float tranY = getTranslationY() +  dy;

                    Line lastLine = new Line(lastX0,lastY0,lastX1,lastY1);
                    Line currLine = new Line(x0,y0,x1,y1);
                    double radians = GeometryUtils.getTwoLineDegree(currLine,lastLine);
                    double rotation = Math.toDegrees(radians)  + getRotation();

                    setRotation((float) rotation);
                    setTranslationX(tranX);
                    setTranslationY(tranY);

                    Log.i("event","tranx = " + String.valueOf(tranX) + ",trany = " + String.valueOf(tranY));
                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() < 3){
                    mTouchMode = TouchMode.SINGLE_TOUCH;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchMode = TouchMode.MULTITOUCH;
                lastX1 = event.getX(1);
                lastY1 = event.getY(1);
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

    //某个点绕一个点顺时针旋转之后的坐标
    private PointF getPointAfterRotateAndTrans1(PointF pointF, double degrees,float pivotX,float pivotY){

        float x = pointF.x;
        float y = pointF.y;
        float x0 = (float) ((x - pivotX) * Math.cos(PI2 - degrees) - (y - pivotY) * Math.sin(PI2 - degrees)) + pivotX;
        float y0 = (float) ((y - pivotY) * Math.cos(PI2 -  degrees) + (x - pivotX) * Math.sin(PI2 - degrees)) + pivotY;

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
