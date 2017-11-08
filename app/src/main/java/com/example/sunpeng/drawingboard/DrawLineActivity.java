package com.example.sunpeng.drawingboard;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;


/**
 * Created by sunpeng on 2017/11/8.
 */

public class DrawLineActivity extends Activity {

    private Button btn_switch;
    private DrawLineView drawLineView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawline);
        btn_switch = (Button) findViewById(R.id.btn_switch);
        drawLineView = (DrawLineView) findViewById(R.id.draw_line);

        btn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawLineView.changeMode();
            }
        });

    }
}
