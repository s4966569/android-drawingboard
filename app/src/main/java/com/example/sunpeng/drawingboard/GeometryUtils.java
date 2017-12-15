package com.example.sunpeng.drawingboard;

/**
 * Created by sp on 17-11-16.
 */

public class GeometryUtils {
    /**
     * 计算两条直线的夹角（line2相对于line1的顺时针角度）
     * @return  返回的是弧度制的角
     */
    public static double getTwoLineDegree(Line line1,Line line2){
        float a1 = line1.a;
        float b1 = line1.b;
        float a2 = line2.a;
        float b2 = line2.b;

        float d = a1 * a2 +b1 * b2;
        if(d == 0){
            return Math.PI / 2;
        }
        return Math.atan((b1 * a2 - a1 * b2) / d);
    }
}
