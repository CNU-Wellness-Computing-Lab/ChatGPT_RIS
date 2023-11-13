package com.example.chatgpt_english;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.chatgpt_english.module.STTModule;
import com.example.chatgpt_english.module.TTSModule;

public class LearningActivity extends AppCompatActivity {

    //test view
    TextView gptSentenceTextView;
    TextView sttResultTextView;

    TextView learningResultTextView;
    Button regenerateTestBtn; // 사용자의 주제 변경 또는 더 많은 학습 컨텐츠가 필요한 경우에 사용
    Button nextSentenceTestBtn;
    Button playSentenceTestBtn;
    private SharedPreferences sharedPreferences;

    // tts-stt 모듈
    private TTSModule ttsModule;
    private STTModule sttModule;

    private String[] parsedContent;
    int contentUncompletedIdx = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        // UI element
        gptSentenceTextView = findViewById(R.id.testView);
        sttResultTextView = findViewById(R.id.testView2);
        learningResultTextView = findViewById(R.id.testView3);
        regenerateTestBtn = findViewById(R.id.regenTestBtn);
        nextSentenceTestBtn = findViewById(R.id.nextTestBtn);
        playSentenceTestBtn = findViewById(R.id.playTestBtn);

        sttModule = new STTModule(this, new STTModule.STTListener() {
            @Override
            public void onSTTResult(String result) {
                runOnUiThread(()->{
                    sttResultTextView.setText(result);
                });
                Log.d("LearningActivity", "STT result \n" + result);
            }

            @Override
            public void onSTTError(String errorMessage) {
                Log.d("LearningActivity", "STT Error \n" + errorMessage);
            }

            @Override
            public void onLearningResult(String result) {
                runOnUiThread(()->{
                    learningResultTextView.setText(result);
                });
            }
        });
        ttsModule = new TTSModule(this, this.sttModule);

        parsedContent = getIntent().getStringArrayExtra("parsed_content");
        assert parsedContent != null;

        StringBuilder gptResponse = new StringBuilder();
        contentUncompletedIdx = 0;

        // gptResponse 출력 (확인용)
        for (String s : parsedContent) {
            gptResponse.append(s).append("\n");
            Log.d("LearningActivity", s + "\n");
        }

        // 재생성 페이지로 넘어가는 태스트 버튼
        regenerateTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToGenerateActivity("여행");
            }
        });

        // text to speech 테스트 버튼
        playSentenceTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(() -> {
                    if(contentUncompletedIdx < parsedContent.length){
                        gptSentenceTextView.setText(sentenceToSpeech(parsedContent, contentUncompletedIdx));
                    }
                });
                nextSentenceTestBtn.setVisibility(View.VISIBLE);
            }
        });

        nextSentenceTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contentUncompletedIdx < parsedContent.length-1){
                    contentUncompletedIdx++;
                    runOnUiThread(()->{
                       gptSentenceTextView.setText(parsedContent[contentUncompletedIdx]);
                       sttResultTextView.setText("waiting for speech-to-text result");

                    });
                }else{
                    runOnUiThread(()->{
                       gptSentenceTextView.setText("영어 학습 끝!");
                    });
                }
            }
        });

    }

    private void destroyTTSnSTT() {
        if (ttsModule != null) {
            ttsModule.shutdown();
        }

        if (sttModule != null) {
            sttModule.destroy();
        }
    }

    private String sentenceToSpeech(String[] _parsedContent, int _contentUncompletedIdx) {
        String targetSentence = _parsedContent[_contentUncompletedIdx];

        assert ttsModule != null;
        Log.d("LearningActivity", targetSentence);
        ttsModule.speak(targetSentence);
        return targetSentence;
    }

    /**
     * 주제가 변경되거나 더 많은 학습 컨텐츠를 제공해야할 시, 호출되는 메서드
     * TODO: 사용자의 음성 명령에 따라 새로운 주제에 따라 재생성하는 동작 구현 필요
     *
     * @param newTopic 사용자의 새로운 주제
     */
    private void returnToGenerateActivity(String newTopic) {
        Intent intent = new Intent(this, GenerateActivity.class);
        //변경된 주제에 맞게 변경
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("topic", newTopic);
        editor.apply();
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        destroyTTSnSTT();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyTTSnSTT();
    }
}