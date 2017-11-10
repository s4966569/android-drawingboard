package com.example.sunpeng.drawingboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PaletteActivity extends AppCompatActivity {
    private Button btn_switch,btn_undo,btn_redo,btn_clear;
    private PaletteView mPaletteView;
    private PaletteImageView iv_palette;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette);
        btn_switch = (Button) findViewById(R.id.btn_switch);
        btn_undo = (Button)findViewById(R.id.btn_undo);
        btn_redo = (Button)findViewById(R.id.btn_redo);
        btn_clear = (Button)findViewById(R.id.btn_clear);
        mPaletteView = (PaletteView) findViewById(R.id.palette);
        iv_palette = (PaletteImageView) findViewById(R.id.iv_palette);

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

                if(iv_palette.getMode() == PaletteImageView.Mode.DRAW){
                    iv_palette.setMode(PaletteImageView.Mode.ERASER);
                    btn_switch.setText("eraser");
                }else if(iv_palette.getMode() == PaletteImageView.Mode.ERASER){
                    iv_palette.setMode(PaletteImageView.Mode.DRAW);
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

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaletteView.clear();
            }
        });
    }
}
