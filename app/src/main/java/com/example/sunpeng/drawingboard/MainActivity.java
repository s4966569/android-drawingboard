package com.example.sunpeng.drawingboard;

import android.content.Intent;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button btn_reset,btn_erase, btn_enter, btn_palette;
    private TouchScaleImageView iv;
    private Matrix matrix;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_reset = (Button) findViewById(R.id.btn);
        btn_erase = (Button) findViewById(R.id.btn1);
        btn_enter = (Button) findViewById(R.id.btn2);
        btn_palette = (Button)findViewById(R.id.btn3);
        iv = (TouchScaleImageView) findViewById(R.id.iv);

        matrix = new Matrix();
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double angle = Math.asin(0.5);
                Toast.makeText(MainActivity.this,String.valueOf(angle),Toast.LENGTH_SHORT).show();
            }
        });

        btn_erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        btn_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,DrawLineActivity.class);
                startActivity(intent);
            }
        });

        btn_palette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,PaletteActivity.class);
                startActivity(intent);
            }
        });
    }
}
