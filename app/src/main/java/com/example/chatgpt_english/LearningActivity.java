package com.example.chatgpt_english;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LearningActivity extends AppCompatActivity {

    //test view
    TextView testView;
    Button testBtn; // 사용자의 주제 변경 또는 더 많은 학습 컨텐츠가 필요한 경우에 사용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        testView = findViewById(R.id.testView);

        String[] parsedContent = getIntent().getStringArrayExtra("parsed_content");


        StringBuilder gptResponse = new StringBuilder();
        try {
            assert parsedContent != null;
            for (String s : parsedContent){
                gptResponse.append(s).append("\n");
                Log.d("TAG", s + "\n");
            }

            runOnUiThread(()->{
                testView.setText(gptResponse.toString());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        testBtn = findViewById(R.id.testBtn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToGenerateActivity();
            }
        });
    }

    private void returnToGenerateActivity(){
        Intent intent = new Intent(this, GenerateActivity.class);
        startActivity(intent);
        overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("앱 종료")
                .setMessage("정말로 종료하겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                    }
                })
                .setNegativeButton("아니오", null)
                .show();
    }
}