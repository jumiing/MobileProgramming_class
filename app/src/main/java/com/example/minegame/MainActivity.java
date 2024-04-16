package com.example.minegame;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressLint("AppCompatCustomView")
class blockButton extends Button {
    int x;
    int y;
    boolean clickable;
    boolean mine;
    boolean flag;
    int neighborMines;
    static int flags;
    static int blocks;
    public blockButton(Context context, int x, int y) {
        super(context);
        mine = false; flag = false; neighborMines = 0;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        blocks += 1;
        mine=false;
        flag = false;
        neighborMines = 0;
        clickable = true;
    }

    public void selectFlag() {
        if (this.flag == true) {
            this.flag = false;
            flags -= 1;
            MainActivity.leftMine++;
            this.setText("");
            this.clickable = true;
        }
        else {
            this.flag = true;
            flags += 1 ;
            MainActivity.leftMine--;
            this.setText("#");
            this.clickable = false;
        }
        TextView mineT = (TextView) ((MainActivity) getContext()).findViewById(R.id.textView2);
        mineT.setText("Mine: " + MainActivity.leftMine);
    }
}

public class MainActivity extends AppCompatActivity {

    //게임 내 설정 변수
    public static int Row = 9;
    public static int Col = 9;
    int totalMine = 10;
    static int leftMine = 10;


    blockButton[][] block = new blockButton[Row][Col];

    static boolean flagMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //여기부터 작성
        TableLayout table = (TableLayout) findViewById(R.id.table);
        TableRow.LayoutParams LayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                1.0f);
        TextView mineT = (TextView) findViewById(R.id.textView2);
        mineT.setText("Mine: "+leftMine);

        //버튼 생성
        for ( int i = 0; i< Row; i++){
            TableRow row = new TableRow(this);
            for (int j = 0; j<Col; j++) {
                block[i][j] = new blockButton(this,i,j);
                block[i][j].setLayoutParams(LayoutParams);
                row.addView(block[i][j]);
            }
            table.addView(row);
        }

        //지뢰 위치 결정
        List<Integer> mineLocation = new ArrayList<>(Row * Col);
        for (int i = 0; i < Row * Col; i++) {
            mineLocation.add(i);
        }
        Collections.shuffle(mineLocation);

        for (int i = 0; i < totalMine; i++) {
            int randomIndex = mineLocation.get(i);
            int r = randomIndex / Col;
            int c = randomIndex % Col;
            block[r][c].mine = true;
        }

        //버튼리스너 설정 + 지뢰 개수 세기
        for ( int l = 0; l< Row; l++){
            for (int j = 0; j<Col; j++) {
                checkMine(block,l,j);
                int finalL = l;
                int finalJ = j;
                block[l][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        if (flagMode) {
                            block[finalL][finalJ].selectFlag();
                        }
                        else {
                            breakBlock(block, finalL, finalJ);
                        }
                    }
                });
            }
        }

        ToggleButton toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 토글 버튼 상태에 따라 모드 변경
                flagMode = toggleButton.isChecked();
            }
        });
    }

    public void checkMine(blockButton[][] b,int x, int y) {
        //주변 지뢰 수 계산 함수
        for (int i = x-1; i<x+2; i++) {
            for (int j = y-1; j<y+2; j++) {
                if (i==x && j==y)
                    continue;
                if (i >= 0 && i < Row && j >= 0 && j < Col) {
                    // 주변 셀이 지뢰이면 count 증가
                    if (b[i][j].mine) {
                        b[x][y].neighborMines++;
                    }
                }
            }
        }

    }

    public boolean breakBlock(blockButton[][] b, int x, int y) {
        //열기 성공하면 true 실패하면 false
        //깃발이거나 이미 열렸으면 열지 않음
        if (!b[x][y].clickable) {
            return false;
        }
        //블록을 여는 경우
        b[x][y].blocks--;
        b[x][y].setBackgroundColor(Color.WHITE);
        b[x][y].clickable = false;
        //-지뢰인 경우
        if (b[x][y].mine) {
            b[x][y].setBackgroundColor(Color.RED);
            b[x][y].setText("@");
//            selectMine = true;
            endGame(false);
            return true;
        }
        //-주변 지뢰 있으면 표시만 하고
        String mineNum = Integer.toString(b[x][y].neighborMines);
        if (b[x][y].neighborMines != 0) {
            b[x][y].setText(mineNum);
            return true;
        }
        //-주변 지뢰가 0이면 주변 열기
        else {
            for (int i = x-1; i<x+2; i++) {
                for (int j = y-1; j<y+2; j++) {
                    if (i==x && j==y)
                        continue;
                    if (i >= 0 && i < Row && j >= 0 && j < Col) {
                        breakBlock(b,i,j);
                    }
                }
            }
        }
        if (leftMine == 0 || b[x][y].blocks <=10) {
            endGame(true);
        }
        return true;
    }

    public void endGame(boolean win) {
        String msg;
        if (win) {
            msg = "Win!";
        }
        msg = "Lose...";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        for ( int l = 0; l< Row; l++){
            for (int j = 0; j<Col; j++) {
                block[l][j].setEnabled(false);
            }
        }
    }
}