package com.example.sunpeng.drawingboard;

/**
 * Created by sp on 17-11-16.
 * 刻度描述类，就是一个线段的两个端点
 */

public class Dial {
    public float startX,startY,stopX,stopY;

    public Dial() {

    }

    public Dial(float startX, float startY, float stopX, float stopY) {
        this.startX = startX;
        this.startY = startY;
        this.stopX = stopX;
        this.stopY = stopY;
    }
}
