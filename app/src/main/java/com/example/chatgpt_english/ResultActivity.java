package com.example.chatgpt_english;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.example.chatgpt_english.module.TTSModule;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private TTSModule ttsModule;
    private TextView testTextView;
    private Handler handler;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        testTextView = findViewById(R.id.testTextView);
        ttsModule = new TTSModule(this);
        handler = new Handler();

        handler.postDelayed(()->{
            runOnUiThread(() -> {
                testTextView.setText(sentenceToSpeech("영어 학습이 모두 종료 되었습니다. 학습 결과를 운전이 완료된 후에 확인 해 주세요."));
                    });
                },1000);


    }


    private String sentenceToSpeech(String _sentence) {
        assert ttsModule != null;
        Log.d("LearningActivity", _sentence);
        ttsModule.setLanguage(Locale.KOREA);
        ttsModule.speak(_sentence);
        return _sentence;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("앱 종료")
                .setMessage("정말로 종료하겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onPause();
                        finishAffinity();
                        onDestroy();
                    }
                })
                .setNegativeButton("아니오", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsModule.shutdown();
    }
}