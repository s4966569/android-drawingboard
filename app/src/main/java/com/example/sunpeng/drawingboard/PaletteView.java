package com.example.sunpeng.drawingboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by sunpeng on 2017/11/8.
 */

public class PaletteView extends View {

    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final int TIME_INTERVAL = 500;  //时间间隔（用来判定是单指触摸还是多指）
    private Paint mPaint;
    private Path mPath;
    private int mLineWidth;
    private int mEraseWidth;
    private int mLineColor;
    long mActionDownBegin = 0;  //单根手指按下的时间
    long mActionPointerDownBegin = 0; //第二根或者以上的手指按下屏幕
    private PointF mLastPoint0 = new PointF();
    private float mLastDis;

    private PointF midPoint;
    private Boolean mIsMultiTouch = false;

    private Bitmap mBufferBitmap;
    private Canvas mBufferCanvas;

    private PaintMode mPaintMode;
    private TouchMode mTouchMode;

    private Xfermode mClearMode;
    private Matrix mMatrix;

    private Bitmap mBgBitmap;

    private Line mBaseLine; //尺子的基准线

    private boolean mShouldBaseRuler = false;

    private ArrayList<PathDrawingInfo> mCachedPathList;
    private ArrayList<PathDrawingInfo> mRemovedPathList;

    public enum PaintMode {
        DRAW,
        ERASER
    }


    public PaletteView(Context context) {
        super(context);
        init();
    }

    public PaletteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaletteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PaletteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaintMode = PaintMode.DRAW;
        mPath = new Path();
        mMatrix = new Matrix();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setFilterBitmap(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mLineWidth = 20;
        mEraseWidth = 40;
        mLineColor = Color.RED;
        mPaint.setStrokeWidth(mLineWidth);
        mPaint.setColor(mLineColor);

        mClearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        mCachedPathList = new ArrayList<>();
        mRemovedPathList = new ArrayList<>();

        mBaseLine = new Line(600,500,600,1000);

    }

    private void initDrawBuffer() {
        mBufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mBufferCanvas = new Canvas(mBufferBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mBgBitmap == null){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.mm);
            mBgBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight());
        }
        canvas.drawBitmap(mBgBitmap,mMatrix,null);
        if (mBufferBitmap != null) {
            canvas.drawBitmap(mBufferBitmap,mMatrix,null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
         final  int action = event.getAction() & MotionEvent.ACTION_MASK;
         final float x = event.getX();
         final float y = event.getY();
         float[] values = new float[9];
         mMatrix.getValues(values);
         float drawX = x;
         float drawY = y;
         Log.i("dis",String.valueOf(mBaseLine.distanceToPoint(drawX,drawY)));
         if(action == MotionEvent.ACTION_DOWN && mBaseLine.distanceToPoint(drawX,drawY) < 150){
             PointF p = mBaseLine.shadowPoint(drawX,drawY);
             drawX = p.x + mLineWidth;
             drawY = p.y;
             mShouldBaseRuler = true;
         }
         if(mTouchMode == TouchMode.SINGLE_TOUCH && mShouldBaseRuler){
             PointF p = mBaseLine.shadowPoint(drawX,drawY);
             drawX = p.x + mLineWidth;
             drawY = p.y;
         }
         float matrixX =( drawX - values[Matrix.MTRANS_X] ) / values[Matrix.MSCALE_X];
         float matrixY = (drawY - values[Matrix.MTRANS_Y]) / values[Matrix.MSCALE_Y];
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchMode = TouchMode.SINGLE_TOUCH;
                mActionDownBegin = SystemClock.uptimeMillis();
                mPath.moveTo(matrixX, matrixY);
                mLastPoint0.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                //判断是否是多根手指的原因是因为存在一种情况就是一开始是multiTouch,但是过程中，抬起了几根手指，只剩下一根手指
                if(mTouchMode == TouchMode.MULTITOUCH){
                    float dis = distance(event);
                    int multiTouchGesture = -1;
                    if(Math.abs(dis - mLastDis) < 10){
                        multiTouchGesture = DRAG;
                    }else {
                        multiTouchGesture = ZOOM;
                    }
                    // 拖拉图片
                    if (multiTouchGesture == DRAG) {
                        float dx = event.getX() - mLastPoint0.x; // 得到x轴的移动距离
                        float dy = event.getY() - mLastPoint0.y; // 得到y轴的移动距离
                        mMatrix.postTranslate(dx, dy);
                    }
                    // 放大缩小图片
                    else if (multiTouchGesture == ZOOM) {
                        midPoint = mid(event);
                        float endDis = distance(event);// 结束距离
                        float scale = endDis / mLastDis;// 得到缩放倍数
                        mMatrix.postScale(scale, scale,midPoint.x,midPoint.y);
                    }

                    mLastDis = dis;
                    mLastPoint0.set(event.getX(), event.getY());
                    postInvalidate();

                }else if(mTouchMode == TouchMode.SINGLE_TOUCH){
                    if (mBufferBitmap == null)
                        initDrawBuffer();
                    mPath.lineTo(matrixX, matrixY);
                    mBufferCanvas.drawPath(mPath, mPaint);
                    postInvalidate();

                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mTouchMode = TouchMode.NONE;
                break;
            case MotionEvent.ACTION_UP:
                if(!mPath.isEmpty()){
                    savePathDrawingInfo(mPaint, mPath);
                    //清除反撤销
                    mRemovedPathList.clear();
                }
                mPath.reset();
                mTouchMode = TouchMode.SINGLE_TOUCH;
                mShouldBaseRuler = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //优化体验，以免想双指操作的时候，因为时间差，第一个手指被判定为画线操作
                if(!mPath.isEmpty()){
                    mActionPointerDownBegin = SystemClock.uptimeMillis();
                    if(mActionPointerDownBegin - mActionDownBegin < TIME_INTERVAL){
                        mPath.reset();
                        reDraw();
                        mTouchMode = TouchMode.MULTITOUCH;
                        mLastDis = distance(event);
                    }else {
                        mTouchMode = TouchMode.NONE;
                    }
                }
                break;
            default:
                break;
        }

        return true;
    }

    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        /** 使用勾股定理返回两点之间的距离 */
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private PointF mid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }

    private void savePathDrawingInfo(Paint paint, Path path) {
        PathDrawingInfo cachedPathDrawingInfo = new PathDrawingInfo();
        cachedPathDrawingInfo.mPaint = new Paint(paint);
        cachedPathDrawingInfo.mPath = new Path(path);
        mCachedPathList.add(cachedPathDrawingInfo);
    }

    private void reDraw() {
        if (mBufferBitmap != null) {
            mBufferBitmap.eraseColor(Color.TRANSPARENT);
            for (PathDrawingInfo pathDrawingInfo : mCachedPathList) {
                pathDrawingInfo.draw(mBufferCanvas);
            }
            postInvalidate();
        }
    }

    public void clear(){
        if(mBufferBitmap != null)
            mBufferBitmap.eraseColor(Color.TRANSPARENT);
        mCachedPathList.clear();
        mRemovedPathList.clear();
        postInvalidate();
    }
    public void undo() {
        if(!canUndo())
            return;
        PathDrawingInfo removedPath = mCachedPathList.remove(mCachedPathList.size() - 1);
        mRemovedPathList.add(removedPath);
        reDraw();
    }

    public void redo(){
        if(!canRedo())
            return;
        PathDrawingInfo removedPath = mRemovedPathList.remove(mRemovedPathList.size() - 1);
        mCachedPathList.add(removedPath);
        reDraw();
    }

    private boolean canUndo() {
        return mCachedPathList != null && mCachedPathList.size() > 0;
    }

    private boolean canRedo() {
        return mRemovedPathList != null && mRemovedPathList.size() > 0;
    }

    public void setPaintMode(PaintMode mode) {
        if (mPaintMode != mode) {
            mPaintMode = mode;
            setPaintMode();
        }
    }

    public PaintMode getPaintMode() {
        return mPaintMode;
    }

    public void setBaseLine(Line line){
        mBaseLine = line;
    }

    private void setPaintMode() {
        if (mPaintMode == PaintMode.DRAW) {
            mPaint.setXfermode(null);
            mPaint.setStrokeWidth(mLineWidth);
        } else if(mPaintMode == PaintMode.ERASER){
            mPaint.setXfermode(mClearMode);
            mPaint.setStrokeWidth(mEraseWidth);
        }
    }

    private static class PathDrawingInfo {
        Paint mPaint;
        Path mPath;

        public void draw(Canvas canvas) {
            canvas.drawPath(mPath, mPaint);
        }
    }
}
