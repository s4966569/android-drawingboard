package com.example.sunpeng.drawingboard;

import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.sunpeng.drawingboard.R;

public class MainActivity extends AppCompatActivity {

    private Button btn_move;
    private DrawImageView iv;
    private Matrix matrix;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_move = (Button) findViewById(R.id.btn);
        iv = (DrawImageView) findViewById(R.id.iv);

        matrix = new Matrix();
        btn_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matrix.set(iv.getImageMatrix());
                matrix.postTranslate(30,100);
                iv.setImageMatrix(matrix);
            }
        });
    }
}
