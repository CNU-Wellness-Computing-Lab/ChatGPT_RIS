package com.example.chatgpt_english;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LearningProcessActivity extends AppCompatActivity {

    private TextView speaking;

    private TextView speechSct;
    private TextView firstKor;
    private TextView progress;
    private TextView follow;
    private TextView postSpeech;
    private ImageButton blindBtn;
    private ImageButton leftBtn;
    private ImageButton rightBtn;
    private ImageButton replayBtn;
    private ImageButton correct;
    private ImageButton backBtn;

    private LinearLayout something;

    private TTSModule ttsModule;
    private STTModule sttModule;

    private androidx.appcompat.widget.AppCompatButton learningStartBtn;
    private androidx.appcompat.widget.AppCompatButton learningStopBtn;

    private androidx.appcompat.widget.AppCompatButton speakingStartBtn;
    private androidx.appcompat.widget.AppCompatButton speakingStopBtn;
    private int MY_PERMISSIONS_RECORD_AUDIO = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_process);

        ttsModule = new TTSModule(this);
        sttModule = new STTModule(this, new STTModule.STTListener() {
            @Override
            public void onSTTResult(String result) {
                Log.d("MainActivity", "STT Result: " + result);
                postSpeech.setText(result);
            }

            @Override
            public void onSTTError(String errorMessage) {
                Log.e("MainActivity", "STT Error: " + errorMessage);
            }
        });

        speechSct = findViewById(R.id.speechScript);
        firstKor = findViewById(R.id.firstKor);
        progress = findViewById(R.id.progress);
        follow = findViewById(R.id.follow);
        postSpeech = findViewById(R.id.postSpeech);
        speaking = findViewById(R.id.speaking);

        follow.setText("");
        postSpeech.setText("");
        something = findViewById(R.id.something);
        blindBtn = findViewById(R.id.blind);

        correct = findViewById(R.id.correct);
        backBtn = findViewById(R.id.backbutton1);

        learningStartBtn = findViewById(R.id.learningStart);
        learningStopBtn = findViewById(R.id.learningStop);

        speakingStartBtn = findViewById(R.id.speakingStart);
        speakingStopBtn = findViewById(R.id.speakingStop);

        firstKor.setText("");
        learningStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ttsModule.speak("시작입니다.");
            }
        });
        learningStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ttsModule != null) {
                    ttsModule.shutdown();
                }
            }
        });

        speakingStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sttModule.startListening();
            }
        });

        speakingStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sttModule.stopListening();
            }
        });

        requestRecordAudio();
    }


    private void requestRecordAudio() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsModule != null) {
            ttsModule.shutdown();
        }

        if (sttModule != null) {
            sttModule.destroy();
        }
    }

}