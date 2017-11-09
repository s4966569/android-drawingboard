package com.example.sunpeng.drawingboard;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by sunpeng on 2017/10/12.
 */

public class TouchScaleImageView extends AppCompatImageView {

    /** 记录是拖拉照片模式还是放大缩小照片模式 */
    private int mode = 0;// 初始状态
    /** 拖拉照片模式 */
    private static final int MODE_DRAG = 1;
    /** 放大缩小照片模式 */
    private static final int MODE_ZOOM = 2;

    private PointF mLastPoint0 = new PointF();
    private PointF mLastPoint1 = new PointF();

    /** 用于记录拖拉图片移动的坐标位置 */
    private Matrix mMatrix = new Matrix();
    /** 用于记录图片要进行拖拉时候的坐标位置 */
    private Matrix mCurrentMatrix = new Matrix();

    /** 两个手指的距离 */
    private float mLastDis;

    /** 两个手指的中间点 */
    private PointF midPoint;

    private boolean mIsMultiTouch = false;

    private Paint mPaint;

    public TouchScaleImageView(Context context) {
        super(context);
        init(context);
    }

    public TouchScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TouchScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void init(Context context){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(8);
        mPaint.setStyle(Paint.Style.STROKE);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 手指压下屏幕
            case MotionEvent.ACTION_DOWN:
                // 记录ImageView当前的移动位置
                mCurrentMatrix.set(getImageMatrix());
                mLastPoint0.set(event.getX(), event.getY());
                break;
            // 手指在屏幕上移动，改事件会被不断触发
            case MotionEvent.ACTION_MOVE:
                mCurrentMatrix.set(getImageMatrix());
                if(mIsMultiTouch && event.getPointerCount() > 1){
                    float dis = distance(event);
                    if(Math.abs(dis - mLastDis) < 10){
                        mode = MODE_DRAG;
                    }else {
                        mode = MODE_ZOOM;
                    }
                    // 拖拉图片
                    if (mode == MODE_DRAG) {
                        float dx = event.getX() - mLastPoint0.x; // 得到x轴的移动距离
                        float dy = event.getY() - mLastPoint0.y; // 得到y轴的移动距离
                        // 在没有移动之前的位置上进行移动
                        mMatrix.set(mCurrentMatrix);
                        mMatrix.postTranslate(dx, dy);
                    }
                    // 放大缩小图片
                    else if (mode == MODE_ZOOM) {
                        midPoint = mid(event);
                        float endDis = distance(event);// 结束距离
                        float scale = endDis / mLastDis;// 得到缩放倍数
                        mMatrix.set(mCurrentMatrix);
                        mMatrix.postScale(scale, scale,midPoint.x,midPoint.y);
                    }

                    mLastDis = dis;
                }
                mLastPoint0.set(event.getX(), event.getY());
                mLastPoint1.set(event.getX(1),event.getY(1));
                break;
            // 手指离开屏幕
            case MotionEvent.ACTION_UP:
                mIsMultiTouch = false;
                break;
            // 当触点离开屏幕，但是屏幕上还有触点(手指)
            case MotionEvent.ACTION_POINTER_UP:
                break;
            // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
            case MotionEvent.ACTION_POINTER_DOWN:
                mIsMultiTouch = true;
                mLastPoint1.set(event.getX(1),event.getY(1));
                /** 计算两个手指间的距离 */
                mLastDis = distance(event);
                /** 计算两个手指间的中间点 */
                midPoint = mid(event);
                //记录当前ImageView的缩放倍数
                mCurrentMatrix.set(getImageMatrix());
                break;
        }
        setImageMatrix(mMatrix);
        return true;
    }


    /** 计算两个手指间的距离 */
    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        /** 使用勾股定理返回两点之间的距离 */
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float distance(PointF p1,Point p2){
        float dx = p1.x - p2.x;
        float dy = p1.y - p2.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /** 计算两个手指间的中间点 */
    private PointF mid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }


}
