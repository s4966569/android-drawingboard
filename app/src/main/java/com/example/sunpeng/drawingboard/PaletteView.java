package com.example.sunpeng.drawingboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    private Path mPath;
    private int mLineWidth;
    private int mEraseWidth;
    private int mLineColor;

    private Bitmap mBufferBitmap;
    private Canvas mBufferCanvas;

    private Mode mMode;

    private float mLastX, mLastY;
    private Xfermode mClearMode;

    private ArrayList<PathDrawingInfo> mCachedPathList;
    private ArrayList<PathDrawingInfo> mRemovedPathList;

    public enum Mode {
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
        mMode = Mode.DRAW;
        mPath = new Path();

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
        if (mBufferBitmap != null) {
            canvas.drawBitmap(mBufferBitmap, 0, 0, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mBufferBitmap == null)
                    initDrawBuffer();
                mPath.lineTo(x, y);
                mBufferCanvas.drawPath(mPath, mPaint);
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                savePathDrawingInfo(mPaint, mPath);
                mRemovedPathList.clear();
                mPath.reset();
                break;
            default:
                break;
        }
        return true;
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
            setPaintMode(mode);
        }
    }

    public Mode getMode() {
        return mMode;
    }

    private void setPaintMode(Mode mode) {
        if (mMode == Mode.DRAW) {
            mPaint.setXfermode(null);
            mPaint.setStrokeWidth(mLineWidth);
        } else {
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
