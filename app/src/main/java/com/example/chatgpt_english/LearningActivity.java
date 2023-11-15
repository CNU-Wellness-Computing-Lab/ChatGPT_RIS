package com.example.chatgpt_english;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.chatgpt_english.module.STTModule;
import com.example.chatgpt_english.module.TTSModule;

import java.util.Locale;
import java.util.Random;

public class LearningActivity extends AppCompatActivity {

    //test view
    TextView ttsSentenceTextView;
    TextView sttResultTextView;

    TextView learningResultTextView;
    Button regenerateTestBtn; // 사용자의 주제 변경 또는 더 많은 학습 컨텐츠가 필요한 경우에 사용
    Button nextSentenceTestBtn;
    Button playSentenceTestBtn;
    private SharedPreferences sharedPreferences;
    private Handler handler;
    private Random random;

    // tts-stt 모듈
    private TTSModule ttsModule;
    private STTModule sttModule;

    private String[] parsedContent;
    int contentUncompletedIdx = -1;

    // 0: 영어 학습 | 1: 학습 진행 여부 | 2: 새로운 토픽 여부 | 3: 새로운 토픽 묻기
    int progressStatus = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        handler = new Handler();
        random = new Random();
        progressStatus = 0;

        // UI element
        ttsSentenceTextView = findViewById(R.id.testView);
        sttResultTextView = findViewById(R.id.testView2);
        learningResultTextView = findViewById(R.id.testView3);

        sttModule = new STTModule(this, new STTModule.STTListener() {
            @Override
            public void onSTTResult(String result) {
                runOnUiThread(() -> {
                    sttResultTextView.setText(result);

                    if (sttModule.getExpectingYesNoAnswer()) {
                        // "yes" / "no" 답변을 받아야 하는 프로세스 (학습 진행 여부 / 새로운 토픽 여부 / 새로운 토픽 묻기)

                        /**
                         *         : 1. 학습 진행 여부 확인 (yes / no) => yes인 경우 다음 프로세스(새로운 토픽 여부) 진행
                         *         : 2. 새로운 토픽 여부 확인 (yes / no) => yes인 경우 다음 프로세스 (새로운 토픽 묻기) 진행 / no인 경우 기존 토픽으로 재생성
                         *         : 3. 새로운 토픽 묻기 (자유로운 응답) => 새로운 토픽으로 재생성
                         */

                        boolean isYes = result.equalsIgnoreCase("yes") || result.equals("예스")
                                || result.equalsIgnoreCase("yeah") || result.equals("에스")
                                || result.equals("예") || result.equals("에") || result.equals("응")
                                || result.equals("네") || result.equals("계속") || result.equals("cancel")
                                || result.equals("진행") || result.equalsIgnoreCase("chain hang");

                        boolean isNo = result.equalsIgnoreCase("no") || result.equals("노")
                                || result.equals("아니") || result.equals("아니오") || result.equals("아니요")
                                || result.equals("중지") || result.equals("충치") || result.equalsIgnoreCase("twenty")
                                || result.equals("20") || result.equalsIgnoreCase("jonsey")
                                || result.equalsIgnoreCase("johnsey")
                                || result.equalsIgnoreCase("choosy")
                                || result.equalsIgnoreCase("john see")
                                || result.equalsIgnoreCase("a new")
                                || result.equalsIgnoreCase("I knew");

                        switch (progressStatus){
                            case 1: // 학습 진행 여부 확인 (yes / no)
                                if(isYes){
                                    Log.d("LearningActivity", "새로운 영어 학습 진행");
                                    progressStatus = 2;
                                    scheduleNextSentence("새로운 주제로 영어 학습을 진행하시겠습니까? 진행 또는 아니오로 대답 해 주세요.");
                                } else if (isNo) {
                                    Log.d("LearningActivity", "영어 학습 종료");
                                    goToResultActivity();
                                } else {
                                    // 예상 외 답변으로 다시 물어봄
                                    Log.d("LearningActivity", "예상 외 답변");
                                    scheduleNextSentence("잘 못 알아들었습니다. 다시 한번 더 말씀 해 주세요");
                                }
                                break;

                            case 2: // 새로운 토픽 확인 여부 확인 (yes / no)
                                if(isYes){
                                    Log.d("LearningActivity", "새로운 주제로 학습 진행");
                                    progressStatus = 3;
                                    scheduleNextSentence("새로운 주제를 말씀 해 주세요.");
                                } else if (isNo) {
                                    Log.d("LearningActivity", "기존 주제로 영어 학습으로 종료");
                                    returnToGenerateActivity();
                                } else {
                                    // 예상 외 답변으로 다시 물어봄
                                    Log.d("LearningActivity", "예상 외 답변");
                                    scheduleNextSentence("잘 못 알아들었습니다. 다시 한번 더 말씀 해 주세요");
                                }
                                break;

                            case 3: // 새로운 주제로 학습 진행
                                returnToGenerateActivity(result);
                                break;
                            default:
                                break;
                        }
                    } else {
                        scheduleNextSentence(parsedContent);
                    }
                });
                Log.d("LearningActivity", "STT result \n" + result);
            }

            @Override
            public void onSTTError(String errorMessage) {
                Log.d("LearningActivity", "STT Error \n" + errorMessage);
                if (!sttModule.getExpectingYesNoAnswer()) {
                    contentUncompletedIdx--;
                    // 영어 학습 진행 중, 인식이 제대로 안된 경우 다시 진행 (progressStatus = 0)
                    runOnUiThread(() -> {
                        scheduleNextSentence(parsedContent);
                    });
                }else{
                    // progressStatus = 1 / 2 / 3일때 처리 진행
                    runOnUiThread(() ->{
                       scheduleNextSentence("잘 못 알아들었습니다. 다시 한번 더 말씀 해 주세요");
                    });
                }
            }

            @Override
            public void onLearningResult(String result) {
                runOnUiThread(() -> {
                    learningResultTextView.setText(result);
                });
            }
        });
        ttsModule = new TTSModule(this, this.sttModule, false);

        parsedContent = getIntent().getStringArrayExtra("parsed_content");
        assert parsedContent != null;

        StringBuilder gptResponse = new StringBuilder();
        contentUncompletedIdx = 0;

        scheduleNextSentence(parsedContent);

        /*
         * ----- test code starts ------
         */

        // gptResponse 출력 (확인용)
        for (String s : parsedContent) {
            gptResponse.append(s).append("\n");
            Log.d("LearningActivity", s + "\n");
        }

        /*
         * ------ Test code ends -------
         */

    }

    private void scheduleNextSentence(String[] _parsedContent) {
        int delay = random.nextInt(5000) + 3000;
        if (contentUncompletedIdx < parsedContent.length) {
            // 학습이 진행되고 있는 상황
            handler.postDelayed(() -> {
                runOnUiThread(() -> {
                    ttsSentenceTextView.setText(sentenceToSpeech(_parsedContent, contentUncompletedIdx++));
                });
            }, delay);

        } else {
            // 학습이 모두 종료된 상황 (영어 학습 단계 종료)
            progressStatus = 1;
            sttModule.setExpectingYesNoAnswer(true);

            ttsModule.shutdown();
            ttsModule = new TTSModule(getApplicationContext(), sttModule, true);

            scheduleNextSentence("영어 학습이 모두 완료되었습니다. 다음 새로운 학습을 진행하시겠습니까?" +
                            " 계속 또는 아니오로 대답 해 주세요");
        }
    }

    private void scheduleNextSentence(String _sentence) {
        int delay = 3000;

        handler.postDelayed(() -> {
            runOnUiThread(() -> {
                ttsSentenceTextView.setText(sentenceToSpeech(_sentence));
            });
        }, delay);
    }



    /**
     * tts, stt 객체 파괴를 위한 메소드
     */
    private void destroyTTSnSTT() {
        if (ttsModule != null) {
            ttsModule.shutdown();
        }

        if (sttModule != null) {
            sttModule.destroy();
        }
    }

    /**
     * 해당 메소드는 영어 학습 컨텐츠 문장을 사용자에게 제공하기 위해 사용
     *
     * @param _parsedContent         영어 학습 컨텐츠
     * @param _contentUncompletedIdx 학습이 미 완료된 문장
     * @return 재생된 문장
     */
    private String sentenceToSpeech(String[] _parsedContent, int _contentUncompletedIdx) {
        String targetSentence = _parsedContent[_contentUncompletedIdx];

        assert ttsModule != null;
        Log.d("LearningActivity", targetSentence);
        ttsModule.setLanguage(Locale.US);
        ttsModule.speak(targetSentence);
        return targetSentence;
    }

    /**
     * 해당 메소드는 영어 학습 종료 후, 사용자의 음성 명령을 받기 위한 안내 문장을 재생하기 위해 사용
     *
     * @param _sentence 안내 메시지
     * @return 재생된 문장
     */
    private String sentenceToSpeech(String _sentence) {
        assert ttsModule != null;
        Log.d("LearningActivity", _sentence);
        ttsModule.setLanguage(Locale.KOREA);
        ttsModule.speak(_sentence);
        return _sentence;
    }

    /**
     * 주제가 변경되거나 더 많은 학습 컨텐츠를 제공해야할 시, 호출되는 메서드
     * TODO: 사용자의 음성 명령에 따라 새로운 주제에 따라 재생성하는 동작 구현 필요
     *
     * @param _newTopic 사용자의 새로운 주제
     */
    private void returnToGenerateActivity(String _newTopic) {
        destroyTTSnSTT();

        Intent intent = new Intent(this, GenerateActivity.class);
        //변경된 주제에 맞게 변경
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("topic", _newTopic);
        editor.apply();
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void returnToGenerateActivity(){
        destroyTTSnSTT();

        Intent intent = new Intent(this, GenerateActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void goToResultActivity(){
        destroyTTSnSTT();

        Intent intent = new Intent(this, ResultActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
                        onDestroy();
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