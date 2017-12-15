package com.example.sunpeng.drawingboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

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
    private float lastX0, lastY0, lastX1, lastY1;
    private int mRulerTop, mRulerLeft, mRulerRight, mRulerBottom;  //相对于自己坐标系的四个顶点
    private PointF p1, p2, p3, p4; //最开始的时候，尺子的四个顶点的坐标（没有平移，也没有旋转）
    private PointF p5, p6, p7, p8; //每次绘制完，尺子的四个顶点坐标
    private PointF mPivot; //轴心点
    private boolean mFirstDraw = true;
    private Paint mBoardPaint;
    private Paint mDialPaint;   //刻度画笔
    private float mTranY; //相对于view自己的坐标系（非旋转平移之后的）
    private static final float DIAL_SPACING = 30; // 刻度间距
    private static final float DIAL_LENGTH = 20;  //小刻度
    private static final float DIAL_LENGTH_LONG = 40;  //大刻度
    private List<Dial> mLeftDials, mRightDials;
    private Rect mRulerRect;
    private OnTransformFinishedListener mOnTransformFinishedListener;

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

    private void init() {
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

        p5= new PointF();
        p6= new PointF();
        p7= new PointF();
        p8= new PointF();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), 5000);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFirstDraw) {

            mRulerLeft = 0;
            mRulerTop = 0;
            mRulerRight = getWidth();
            mRulerBottom = getHeight();

            mFirstDraw = false;

            p1 = new PointF(getLeft(), getTop());
            p2 = new PointF(getRight(), getTop());
            p3 = new PointF(getLeft(), getBottom());
            p4 = new PointF(getRight(), getBottom());

            mPivot = new PointF((getLeft() + getRight()) / 2, (getTop() + getBottom()) / 2);

            mRulerRect = new Rect(mRulerLeft, mRulerTop, mRulerRight, mRulerBottom);

            initDials();
        }

        canvas.drawRect(mRulerLeft, mRulerTop, mRulerRight, mRulerBottom, mPaint);
        //画刻度
        drawDials(canvas);
        //canvas是一个独立的坐标系，一开始跟view的坐标系是重合的，但是平移旋转之后，canvas的坐标系也随着平移旋转，view的坐标系不变（重要）

        computeVertexesAfterTransformed();

        if (mOnTransformFinishedListener != null) {
            mOnTransformFinishedListener.onTransformFinished(p1, p2, p3, p4);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        final float x0 = event.getX();
        final float y0 = event.getY();
        Log.i("event", "x = " + String.valueOf(x0) + ",y = " + String.valueOf(y0));
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX0 = x0;
                lastY0 = y0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchMode == TouchMode.SINGLE_TOUCH) {
                    float dx = x0 - lastX0;
                    float dy = y0 - lastY0;

                    mTranY += dy;

                    if (Math.abs(mTranY) > 300) {
                        float a = mTranY % DIAL_SPACING;
                        dy = a - (mTranY - dy);
                        mTranY = a;
                    }

                    double degrees = Math.toRadians(getRotation());
                    float dx1 = (float) (dx * Math.cos(degrees) - dy * Math.sin(degrees));
                    float dy1 = (float) (dy * Math.cos(degrees) + dx * Math.sin(degrees));

                    float tranX = getTranslationX() + dx1;
                    float tranY = getTranslationY() + dy1;

                    setTranslationX(tranX);
                    setTranslationY(tranY);
                    computeVertexesAfterTransformed();
                    if (mOnTransformFinishedListener != null) {
                        mOnTransformFinishedListener.onTransformFinished(p5, p6, p7, p8);
                    }
                } else if (mTouchMode == TouchMode.MULTITOUCH) {
                    final float x1 = event.getX(1);
                    final float y1 = event.getY(1);

                    float dx0 = x0 - lastX0;
                    float dy0 = y0 - lastY0;

                    float dx1 = x1 - lastX1;
                    float dy1 = y1 - lastY1;

                    float dx_ava = (dx0 + dx1) / 2;
                    float dy_ava = (dy0 + dy1) / 2;

                    double degrees = Math.toRadians(getRotation());
                    float dx = (float) (dx_ava * Math.cos(degrees) - dy_ava * Math.sin(degrees));
                    float dy = (float) (dy_ava * Math.cos(degrees) + dx_ava * Math.sin(degrees));

                    float tranX = getTranslationX() + dx;
                    float tranY = getTranslationY() + dy;

                    Line lastLine = new Line(lastX0, lastY0, lastX1, lastY1);
                    Line currLine = new Line(x0, y0, x1, y1);
                    double radians = GeometryUtils.getTwoLineDegree(currLine, lastLine);
                    double rotation = Math.toDegrees(radians) + getRotation();

                    //这两种操作并不会重绘view
                    setRotation((float) rotation);
                    setTranslationX(tranX);
                    setTranslationY(tranY);
                    computeVertexesAfterTransformed();
                    if (mOnTransformFinishedListener != null) {
                        mOnTransformFinishedListener.onTransformFinished(p5, p6, p7, p8);
                    }
                    Log.i("event", "tranx = " + String.valueOf(tranX) + ",trany = " + String.valueOf(tranY));
                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() < 3) {
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

    private void computeVertexesAfterTransformed() {

        float tranX = getTranslationX();
        float tranY = getTranslationY();
        double radians = Math.toRadians(getRotation());

        Log.i("tran", "tranx = " + String.valueOf(tranX) + ",trany = " + String.valueOf(tranY));
        Log.i("rotation",String.valueOf(getRotation()));

        float[] points = new float[]{p1.x,p1.y,p2.x,p2.y,p3.x,p3.y,p4.x,p4.y};

        Matrix matrix = new Matrix();
        //注意，应该先旋转再平移（因为一直是以view的中心点旋转的，如果先平移再旋转，那么旋转的轴心点也需要做相应的平移）
        matrix.postRotate(getRotation(),mPivot.x,mPivot.y);
        matrix.postTranslate(tranX,tranY);
        matrix.mapPoints(points);
//        //此处如果这样写，后一个变换会直接覆盖前一个变换(相当于第一个变换会被reset然后做第二个变换)
//        matrix.setRotate(getRotation(),mPivot.x,mPivot.y);
//        matrix.setTranslate(tranX,tranY);
//        matrix.mapPoints(points);

        p5.set(points[0],points[1]);
        p6.set(points[2],points[3]);
        p7.set(points[4],points[5]);
        p8.set(points[6],points[7]);


        //第二种方法算平移后点的坐标
//        //算出尺子的四个顶点在view坐标系的坐标
//        p5 = getPointAfterRotateAndTrans(p1, tranX, tranY, radians, mPivot.x, mPivot.y);
//        p6 = getPointAfterRotateAndTrans(p2, tranX, tranY, radians, mPivot.x, mPivot.y);
//        p7 = getPointAfterRotateAndTrans(p3, tranX, tranY, radians, mPivot.x, mPivot.y);
//        p8 = getPointAfterRotateAndTrans(p4, tranX, tranY, radians, mPivot.x, mPivot.y);

    }

    /**
     * 初始化刻度集合
     */
    private void initDials() {

        mLeftDials = new ArrayList<>();
        mRightDials = new ArrayList<>();

        float length = mRulerBottom - mRulerTop;

        int count = Math.round(length / DIAL_SPACING);

        for (int i = 0; i < count; i++) {
            float dialLength;
            if (i % 5 == 0) {
                dialLength = DIAL_LENGTH_LONG;
            } else {
                dialLength = DIAL_LENGTH;
            }
            Dial leftDial = new Dial();
            leftDial.startX = mRulerLeft - STROKE_WIDTH / 2;
            leftDial.startY = mRulerTop + i * DIAL_SPACING;
            leftDial.stopX = mRulerLeft + dialLength - STROKE_WIDTH / 2;
            leftDial.stopY = mRulerTop + i * DIAL_SPACING;

            mLeftDials.add(leftDial);


            Dial rightDial = new Dial();
            rightDial.startX = mRulerRight + STROKE_WIDTH / 2;
            rightDial.startY = mRulerTop + i * DIAL_SPACING;
            rightDial.stopX = mRulerRight - dialLength + STROKE_WIDTH / 2;
            rightDial.stopY = mRulerTop + i * DIAL_SPACING;

            mRightDials.add(rightDial);

        }
    }

    private void drawDials(Canvas canvas) {
        for (int i = 0; i < mLeftDials.size(); i++) {
            Dial leftDial = mLeftDials.get(i);
            Dial rightDial = mRightDials.get(i);

            canvas.drawLine(leftDial.startX, leftDial.startY, leftDial.stopX, leftDial.stopY, mDialPaint);
            canvas.drawLine(rightDial.startX, rightDial.startY, rightDial.stopX, rightDial.stopY, mDialPaint);
        }
    }


    //先旋转再平移(逆时针)
    private PointF getPointAfterRotateAndTrans(PointF pointF, float tranX, float tranY, double degrees, float pivotX, float pivotY) {
        float x = pointF.x ;
        float y = pointF.y ;

        float x0 = (float) ((x - pivotX) * Math.cos(degrees) - (y - pivotY) * Math.sin(degrees)) + pivotX;
        float y0 = (float) ((y - pivotY) * Math.cos(degrees) + (x - pivotX) * Math.sin(degrees)) + pivotY;

        return new PointF(x0+ tranX , y0+ tranY );
    }


    public Line getLine1() {
        return new Line(p5, p7);
    }

    public Line getLine2() {
        return new Line(p6, p8);
    }

    public void setOnDrawFinishListener(OnTransformFinishedListener listener) {
        mOnTransformFinishedListener = listener;
    }

    public interface OnTransformFinishedListener {
        void onTransformFinished(PointF leftTop, PointF rightTop, PointF leftBottom, PointF rightBottom);
    }
}
