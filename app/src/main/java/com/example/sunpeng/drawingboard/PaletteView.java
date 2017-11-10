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
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by sunpeng on 2017/11/8.
 */

public class PaletteView extends View {
    private Paint mPaint;
    private Path mPath,mTempPath;
    private int mLineWidth;
    private int mEraseWidth;
    private int mLineColor;

    private PointF mLastPoint0 = new PointF();
    private float mLastDis;

    private PointF midPoint;

    private boolean mIsMultiTouch = false;

    private Bitmap mBufferBitmap;
    private Canvas mBufferCanvas;

    private Mode mMode;

    private float mTranslateX,mTranslateY;

    private float mLastX, mLastY;
    private Xfermode mClearMode;
    private Matrix mMatrix;

    private Bitmap mBgBitmap;

    private ArrayList<PathDrawingInfo> mCachedPathList;
    private ArrayList<PathDrawingInfo> mRemovedPathList;

    public enum Mode {
        DRAW,
        ERASER,
        DRAG,
        ZOOM
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
        mMode = Mode.DRAW;
        mPath = new Path();
        mTempPath = new Path();
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
            mBgBitmap = Bitmap.createBitmap(bitmap,0,0,getWidth(),getHeight());
        }
        canvas.drawBitmap(mBgBitmap,mMatrix,null);
        if (mBufferBitmap != null) {
            canvas.drawBitmap(mBufferBitmap,mMatrix,null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
         final float x = event.getX();
         final float y = event.getY();
         float[] values = new float[9];
         mMatrix.getValues(values);
         float matrixX =( x - values[Matrix.MTRANS_X] ) / values[Matrix.MSCALE_X];
         float matrixY = (y - values[Matrix.MTRANS_Y]) / values[Matrix.MSCALE_Y];
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(mMode != Mode.DRAW && mMode !=Mode.ERASER){
                    setMode(Mode.DRAW);
                }
                mPath.moveTo(matrixX, matrixY);
                mLastPoint0.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                if(mIsMultiTouch && event.getPointerCount() > 1){
                    float dis = distance(event);
                    if(Math.abs(dis - mLastDis) < 10){
                        mMode = Mode.DRAG;
                    }else {
                        mMode = Mode.ZOOM;
                    }
                    // 拖拉图片
                    if (mMode == Mode.DRAG) {
                        float dx = event.getX() - mLastPoint0.x; // 得到x轴的移动距离
                        float dy = event.getY() - mLastPoint0.y; // 得到y轴的移动距离
                        // 在没有移动之前的位置上进行移动
                        mTranslateX += dx;
                        mTranslateY += dy;
                        mMatrix.postTranslate(dx, dy);
                    }
                    // 放大缩小图片
                    else if (mMode == Mode.ZOOM) {
                        midPoint = mid(event);
                        float endDis = distance(event);// 结束距离
                        float scale = endDis / mLastDis;// 得到缩放倍数
                        mMatrix.postScale(scale, scale,midPoint.x,midPoint.y);
                    }

                    mLastDis = dis;
                    mLastPoint0.set(event.getX(), event.getY());

                }else if(mMode == Mode.DRAW || mMode == Mode.ERASER){
                    if (mBufferBitmap == null)
                        initDrawBuffer();
                    mPath.lineTo(matrixX, matrixY);
                    mBufferCanvas.drawPath(mPath, mPaint);
                }
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                if(mMode == Mode.DRAW || mMode == Mode.ERASER){
                    savePathDrawingInfo(mPaint, mPath);
                    mRemovedPathList.clear();
                }
                mPath.reset();
                mIsMultiTouch = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mIsMultiTouch = true;
                mLastDis = distance(event);
                midPoint = mid(event);
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

    public void setMode(Mode mode) {
        if (mMode != mode) {
            mMode = mode;
            setPaintMode();
        }
    }

    public Mode getMode() {
        return mMode;
    }

    private void setPaintMode() {
        if (mMode == Mode.DRAW) {
            mPaint.setXfermode(null);
            mPaint.setStrokeWidth(mLineWidth);
        } else if(mMode == Mode.ERASER){
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
