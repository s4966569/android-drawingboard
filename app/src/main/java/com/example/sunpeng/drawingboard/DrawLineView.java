package com.example.sunpeng.drawingboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by sunpeng on 2017/11/8.
 */

public class DrawLineView extends View {

    private Paint paint;
    private List<PointF> pts = new ArrayList<>();
    private int mode = 1;
    private static final int MODE_DRAW = 1;
    private static final int MODE_ERASE = 2;

    private List<PointF> pts_erase = new ArrayList<>();

    public DrawLineView(Context context) {
        super(context);
        init();
    }

    public DrawLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DrawLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(8);
        paint.setStyle(Paint.Style.STROKE);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(pts.size() > 1){
            drawLines(canvas,pts);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(mode == MODE_DRAW){
                    pts.add(new PointF(event.getX(),event.getY()));

                }else if(mode == MODE_ERASE){
                    erase(event.getX(),event.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mode == MODE_DRAW){
                    pts.add(new PointF(event.getX(),event.getY()));
                    postInvalidate();
                }else if(mode == MODE_ERASE){
                    erase(event.getX(),event.getY());
                }

                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return true;
    }

    private float[] arrayToFloat(List<Float> list){
        float[] result = new float[list.size()];
        for(int i =0; i< list.size(); i ++){
            result[i] = list.get(i);
        }
        return result;
    }

    private void erase(float x, float y){
        ListIterator<PointF> iterator = pts.listIterator();
        PointF pointF;
        while (iterator.hasNext()){
            pointF = iterator.next();
//            if(Math.abs(pointF.x - x) < 40.0 && Math.abs(pointF.y - y ) < 40.0){
//                iterator.remove();
//                Log.i("erase","::::::::x=" + String.valueOf(x) +"&&y=" + String.valueOf(y));
//                postInvalidate();
//            }

            if(isPointInCircle(pointF,x,y)){
                iterator.remove();
                Log.i("erase","::::::::x=" + String.valueOf(x) +"&&y=" + String.valueOf(y));
                postInvalidate();
            }
        }
    }

    private void drawLines(Canvas canvas, List<PointF> points){
        for (int i=0; i < points.size() - 1; i ++){
            canvas.drawLine(points.get(i).x,points.get(i).y,points.get(i+1).x,points.get(i+1).y,paint);
        }
    }

    private boolean isPointInCircle(PointF p,float x,float y){
        boolean b = false;
        if((p.x - x) * (p.x - x) + (p.y -y) * (p.y - y) <= 4){
            b = true;
        }
        return  b;
    }

    public void changeMode(){
        if(mode == MODE_DRAW){
            mode = MODE_ERASE;
        }else {
            mode = MODE_DRAW;
        }
    }
}
