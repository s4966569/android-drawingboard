package com.example.sunpeng.drawingboard;

import android.graphics.PointF;

/**
 * Created by sunpeng on 2017/11/14.
 * 定义描述直线的类
 * 直线的公式为 两点式 (x-x1) / (x2 - x1) = (y - y1) / (y2 -y1)
 * 普通式 ax+by+c = 0
 * 点到直线的距离公式为：
 */

public class Line {
    private float a, b ,c;

    public Line(PointF p1, PointF p2){
        a = p2.y - p1.y;
        b = p1.x -p2.x;
        c = p2.x*p1.y - p1.x*p2.y;
    }

    public Line(float x1,float y1,float x2, float y2){
        a = y2 - y1;
        b = x1 -x2;
        c = x2*y1 - x1*y2;
    }

    /**
     * 点到直线的距离
     * @param x0
     * @param y0
     * @return
     */
    public float distanceToPoint(float x0,float y0){
        return (float) (Math.abs(a * x0 + b * y0 + c) / Math.sqrt(a * a + b * b));
    }

    /**
     * 求点在该直线的投影点
     * @param x0
     * @param y0
     * @return
     */
    public PointF shadowPoint(float x0, float y0){
        float x = (b * b * x0 - a*(b*y0 +c)) / (a * a + b * b);

        float y = (a * a * y0 - a * b * x0 - b * c) / (a * a + b * b);

        return new PointF(x,y);
    }

    /**
     * 计算两条直线的夹角
     * @param line
     * @return  返回的是弧度制的角
     */
    public double getTwoLineDegree(Line line){
        float a1 = line.a;
        float b1 = line.b;

        float d = a * a1 +b * b1;
        if(d == 0){
            return Math.PI / 2;
        }
        return Math.atan((b * a1 - a * b1) / d);
    }
}
