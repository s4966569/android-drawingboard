package com.example.sunpeng.drawingboard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button btn_switch,btn_undo,btn_redo, btn_reset,btn_enter;
    private PaletteView mPaletteView;
    private RulerView2 mRulerView;
    private RulerView mRulerView1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_switch = (Button) findViewById(R.id.btn_switch);
        btn_undo = (Button)findViewById(R.id.btn_undo);
        btn_redo = (Button)findViewById(R.id.btn_redo);
        btn_reset = (Button)findViewById(R.id.btn_reset);
        btn_enter = (Button) findViewById(R.id.btn_enter);
        mPaletteView = (PaletteView) findViewById(R.id.palette);
        mRulerView = (RulerView2) findViewById(R.id.ruler);
        mRulerView1 = (RulerView) findViewById(R.id.ruler1);

        btn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPaletteView.getPaintMode() == PaletteView.PaintMode.DRAW){
                    mPaletteView.setPaintMode(PaletteView.PaintMode.ERASER);
                    btn_switch.setText("eraser");
                }else if(mPaletteView.getPaintMode() == PaletteView.PaintMode.ERASER){
                    mPaletteView.setPaintMode(PaletteView.PaintMode.DRAW);
                    btn_switch.setText("draw");
                }
            }
        });

        btn_undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaletteView.undo();
            }
        });

        btn_redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaletteView.redo();
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaletteView.clear();
            }
        });


        btn_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,PaletteActivity.class);
                startActivity(intent);
            }
        });

        mRulerView1.setOnDrawFinishListener(new RulerView.OnDrawFinishListener() {
            @Override
            public void onDrawFinish() {
                mPaletteView.setBaseLine(mRulerView1.getLine2());
            }
        });

    }
}
