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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.chatgpt_english.connect_PC.PC_connector;
import com.example.chatgpt_english.module.CSVModule;
import com.example.chatgpt_english.module.STTModule;
import com.example.chatgpt_english.module.TTSModule;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class LearningActivity extends AppCompatActivity {
    private final int MAX_LEARNING_COUNT = 5;       // 현재 주제에 학습 가능한 문장 수
    private final int COG_RISK_THRESHOLD = 100;     // 인지 부하 100 초과 경우 '매우 높음'
    private final int COG_HIGH_THRESHOLD = 75;      // 인지 부하 75 초과 경우 (75 <  <= 100) '높음'
    private final int COG_MID_THRESHOLD = 50;       // 인지 부하 50 초과 경우 (50 <  <= 75) '중간'

    private final int STATUS_RISK = 3;
    private final int STATUS_HIGH = 2;
    private final int STATUS_MID = 1;
    private final int STATUS_LOW = 0;

    // 인지 부하가 50보다 낮은 경우 '낮음'
    //test view
    TextView cognitiveLoadTextView;
    TextView ttsSentenceTextView;
    TextView sttResultTextView;

    TextView learningResultTextView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Handler handler;
    private Random random;

    // tts-stt 모듈
    private TTSModule ttsModule;
    private STTModule sttModule;

    private String[] parsedContent;

    private ArrayList<Sentence> lowCognitiveContent;     // 인지 부하가 낮은 경우 재생될 문장

    private ArrayList<Sentence> midCognitiveContent;     // 인지 부하가 중간인 경우 재생될 문장
    private ArrayList<Sentence> highCognitiveContent;    // 인지 부하가 높은 경우 재생될 문장

    private int lowIdx = -1;
    private int midIdx = -1;
    private int highIdx = -1;


    int learnedCount = -1;

    // 학습 진행 상황 파악하기 위한 변수
    int progressStatus = -1;        // 0: 영어 학습 | 1: 학습 진행 여부 | 2: 새로운 토픽 여부 | 3: 새로운 토픽 묻기

    // 사용자 인지 부하 정도를 파악하기 위한 변수
    int userCognitiveStatus = -1;   // 0: 낮음 | 1: 중간 | 2: 높음 | 3: 매우 높음
    int currentCycle = -1;
    StringBuilder dataSB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        handler = new Handler();
        random = new Random();
        progressStatus = 0;
        currentCycle = sharedPreferences.getInt("Cycle", -1);

        // UI element
        ttsSentenceTextView = findViewById(R.id.testView);
        sttResultTextView = findViewById(R.id.testView2);
        learningResultTextView = findViewById(R.id.testView3);
        cognitiveLoadTextView = findViewById(R.id.CognitiveLoad);

        // 인지 부하에 따른 학습 컨텐츠 선언
        lowCognitiveContent = new ArrayList<>();
        midCognitiveContent = new ArrayList<>();
        highCognitiveContent = new ArrayList<>();

        // CSV에 저장 될 data 선언
        dataSB = new StringBuilder();

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

                        switch (progressStatus) {
                            case 1: // 학습 진행 여부 확인 (yes / no)
                                if (isYes) {
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
                                if (isYes) {
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
                        /**
                         * 데이터 Add (인식 결과 )
                         */
                        addData(result);
                        scheduleNextSentence();
                    }
                });
                Log.d("LearningActivity", "STT result \n" + result);
            }

            @Override
            public void onSTTError(String errorMessage) {
                Log.d("LearningActivity", "STT Error \n" + errorMessage);
                if (!sttModule.getExpectingYesNoAnswer()) {
                    learnedCount--;
                    switch (userCognitiveStatus) {
                        case 2:
                            highIdx--;
                            break;
                        case 1:
                            midIdx--;
                            break;
                        case 0:
                            lowIdx--;
                            break;
                        default:
                    }
                    /**
                     * 데이터 Add (학습 결과 )
                     * & Write
                     */
                    // 영어 문장을 말한 것이 아니므로 정답: "ERROR", "ERROR", LDistance: "-1", "(현재 학습 Cycle)"
                    addData("ERROR","ERROR", "-1", currentCycle + "");
                    flushData();

                    runOnUiThread(() -> {
                        scheduleNextSentence();
                    });
                } else {
                    // progressStatus = 1 / 2 / 3일때 처리 진행
                    runOnUiThread(() -> {
                        scheduleNextSentence("잘 못 알아들었습니다. 다시 한번 더 말씀 해 주세요");
                    });
                }
            }

            @Override
            public void onLearningResult(String result) {
                if(progressStatus <= 1) {
                    String answerResult = "";
                    int lDist = Integer.parseInt(result);

                    if (lDist < 11) {
                        answerResult += "\nCORRECT";
                        addData("CORRECT");
                    } else {
                        answerResult += "\nWrong";
                        addData("WRONG");
                    }

                    addData(lDist + "", currentCycle + "");
                    flushData();

                    if(dataSB.toString().split(",").length > 8){
                        flushData();
                    }else {
                        Log.d("LearningActivity", "Wrong data length: " + dataSB.toString().split(", ").length + "\n"
                                + "deleting current data...");
                        dataSB = new StringBuilder();
                    }

                    String finalAnswerResult = answerResult;
                    runOnUiThread(() -> {
                        learningResultTextView.setText(result + finalAnswerResult);
                    });
                }
            }
        });

        ttsModule = new TTSModule(this, this.sttModule, false);

        parsedContent = getIntent().getStringArrayExtra("parsed_content");
        learnedCount = 0;
        lowIdx = 0;
        midIdx = 0;
        highIdx = 0;

        organizeContent(parsedContent); // GPT에서 생성된 문장 학습에 맞게 재구성
        scheduleNextSentence();


        /*
         * ----- test code starts ------
         */

        // gptResponse 출력 (확인용)
        StringBuilder gptResponse = new StringBuilder();
        for (String s : parsedContent) {
            gptResponse.append(s).append("\n");
            Log.d("LearningActivity", s + "\n");
        }

        /*
         * ------ Test code ends -------
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
//                        Thread.sleep(50);
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /*
                                     TEST CODE STARTS
                                 */
                                PC_connector.cognitiveLoad = random.nextInt(130);
                                /*
                                     TEST CODE ENDS
                                 */
                                cognitiveLoadTextView.setText(PC_connector.cognitiveLoad + "");
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void addData(String... _data){
        if(dataSB != null){
            for (String s: _data){
                dataSB.append(s).append(",  ");
            }
            Log.d("LearningActivity", "Added Data. Current data: \n"
                    + dataSB.toString());
        }
    };

    private void flushData(){
        if(dataSB != null) {
            if(dataSB.toString().split(",  ").length > 8){
                CSVModule.writeCsvFile(getApplicationContext(), "English_Learning_withCog.csv", dataSB.toString());
            }else {
                Log.d("LearningActivity", "Wrong data length: " + dataSB.toString().split(", ").length + "\n"
                        + "deleting current data...");
            }
        }
        resetData();
    }

    private void resetData(){
        dataSB = new StringBuilder();
    }


    /**
     * GenerateActivity에서 생성된 GPT의 문장을 난이도에 맞게 학습 컨텐츠 재구성
     *
     * @param _parsedContent GPT에서 생성된 문장
     */
    private void organizeContent(String[] _parsedContent) {
        /*
         *  임의로 지정, 언제든 변경될 수 있도록 설정
         */

        /*
            TEST CODE START
         */
//        testDataOrganize();
        /*
            TEST CODE ENDS
         */

        // 인지 부하에 따라 적용될 단어의 길이
        // 인지 부하가 낮을 수록 단어 수가 많은 문장을 재생하며,
        // 인비 부하가 높을 수록 단어 수가 적은 문장을 재생한다
        final int LOW_COG_WORD_COUNT = 7;
        final int MID_COG_WORD_COUNT = 5;
        final int HIGH_COG_WORD_COUNT = 3;

        for (int idx = 0; idx < _parsedContent.length; idx++) {
            int wordCount = _parsedContent[idx].split(" ").length;

            if( wordCount <= HIGH_COG_WORD_COUNT){
                highCognitiveContent.add(new Sentence(_parsedContent[idx], 0));
            } else if (wordCount <= MID_COG_WORD_COUNT){
                midCognitiveContent.add(new Sentence(_parsedContent[idx], 1));
            }else {
                lowCognitiveContent.add(new Sentence(_parsedContent[idx],2));
            }
        }
    }


    /**
     * 테스트용 데이터. 실제 실험에서 사용되지 않을 메소드
     */
    private void testDataOrganize() {
        highCognitiveContent.add(new Sentence("I am human", 0));
        highCognitiveContent.add(new Sentence("I am machine", 0));
        highCognitiveContent.add(new Sentence("I am student", 0));

        midCognitiveContent.add(new Sentence("I am very excited", 1));
        midCognitiveContent.add(new Sentence("I am very sad", 1));
        midCognitiveContent.add(new Sentence("I am very happy", 1));

        lowCognitiveContent.add(new Sentence("I am doing my work", 2));
        lowCognitiveContent.add(new Sentence("I am doing my job", 2));
        lowCognitiveContent.add(new Sentence("I am doing my chores", 2));
    }

    /**
     * 다음 문장을 재생하기 위한 스케줄링 메소드
     * 해당 메소드는 영어 학습 컨텐츠를 재생 할 때 ( progressStatus = 0 ) 사용 된다
     */
    private void scheduleNextSentence() {
        int delay = random.nextInt(5000) + 3000;
        final Sentence[] targetSentence = new Sentence[1];

        if (learnedCount < MAX_LEARNING_COUNT) {
            learnedCount++;

            handler.postDelayed(() -> {
                double currentCogLoad = PC_connector.cognitiveLoad;

                addData(
                        System.currentTimeMillis() + "",
                        sharedPreferences.getString("driving_exp", "-1"),
                        sharedPreferences.getString("english_skill", "-1")
                        );


                if (currentCogLoad > COG_RISK_THRESHOLD) {
                    // 인지 부하가 '매우 높음' 상태 => 학습 보류
                    userCognitiveStatus = STATUS_RISK;
                    Log.d("LearningActivity", "Driver status: Risky condition. Halt the content");
                    runOnUiThread(() -> {
                        ttsSentenceTextView.setText(
                                "(인지 부하 매우 높음 상태: " + currentCogLoad + ")");
                    });
                    learnedCount--;
                    scheduleNextSentence();
                } else if (currentCogLoad > COG_HIGH_THRESHOLD) {
                    // 인지 부하가 '높음' 상태 => 낮은 레벨 문장 학습
                    Log.d("LearningActivity", "Driver status: High condition. Play low level sentence");
                    userCognitiveStatus = STATUS_HIGH;
                    targetSentence[0] = highCognitiveContent.get(highIdx);
                    addData(
                            targetSentence[0].getSentence(),
                            targetSentence[0].getLearningLevel() + "",
                            sharedPreferences.getString("topic", "unknown"),
                            currentCogLoad + ""
                    );
                    addData("HIGH");
                    runOnUiThread(() -> {
                        ttsSentenceTextView.setText(sentenceToSpeech(targetSentence[0])
                                + "\n (인지 부하 높음 상태: " + currentCogLoad + ")"
                        );
                        highIdx++;
                    });
                } else if (currentCogLoad > COG_MID_THRESHOLD) {
                    // 인지 부하가 '중간' 상태 => 중간 레벨 문장 학습
                    Log.d("LearningActivity", "Driver status: Mid condition. Play mid level sentence");
                    userCognitiveStatus = STATUS_MID;
                    targetSentence[0] = midCognitiveContent.get(midIdx);
                    addData(
                            targetSentence[0].getSentence(),
                            targetSentence[0].getLearningLevel() + "",
                            sharedPreferences.getString("topic", "unknown"),
                            currentCogLoad + ""
                    );
                    addData("MID");
                    runOnUiThread(() -> {
                        ttsSentenceTextView.setText(sentenceToSpeech(targetSentence[0])
                                + "\n (인지 부하 중간 상태: " + currentCogLoad + ")"
                        );
                        midIdx++;
                    });
                } else {
                    // 인지 부하가 '낮음' 상태 => 낮음 레벨 문장 학습
                    Log.d("LearningActivity", "Driver status: Low condition. Play high level sentence");
                    userCognitiveStatus = STATUS_LOW;
                    targetSentence[0] = lowCognitiveContent.get(lowIdx);
                    addData(
                            targetSentence[0].getSentence(),
                            targetSentence[0].getLearningLevel() + "",
                            sharedPreferences.getString("topic", "unknown"),
                            currentCogLoad + ""
                    );
                    addData("LOW");
                    runOnUiThread(() -> {
                        ttsSentenceTextView.setText(sentenceToSpeech(targetSentence[0])
                                + "\n (인지 부하 낮음 상태: " + currentCogLoad + ")"
                        );
                        lowIdx++;
                    });
                }

                /**
                 * Data add (문장 정보 | 인지부하 정보 )
                 */
                if(currentCogLoad > COG_RISK_THRESHOLD) {
                    // 인지 부하가 RISK 상태일 때 데이터 저장으로, 영어 문장 재생하지 않고 넘어간다
                    addData(
                            "DRIVER'S COGNITIVE IS IN RISK... SCHEDULING NEXT SENTENCE",  // 재생 문장
                            "-1",                                                         // 재생 문장 난이도
                            sharedPreferences.getString("topic", "unknown"),       // 주제
                            currentCogLoad + "",                                         // 인지 부하 값
                            "RISK",                                                      // 인지 부하 레벨
                            "SKIPPED",                                                   // 쉐도잉 인식 문장
                            "SKIPPED",                                                   // 정답 여부
                            "-1",                                                        // 알고리즘 계산 값
                            currentCycle + ""                                            // 학습 사이클 차수
                    );
                    flushData();
                }

            }, delay);
        } else {
            // 학습이 모두 종료된 상황 (영어 학습 단계 종료)
            progressStatus = 1;
            sttModule.setExpectingYesNoAnswer(true);

            ttsModule.shutdown();
            ttsModule = new TTSModule(getApplicationContext(), sttModule, true);

            scheduleNextSentence("새로운 학습을 진행하시겠습니까?" +
                    " 진행 또는 아니오로 대답 해 주세요");
        }
    }

    /**
     * 다음 문장을 재생하기 위한 스케줄링 메소드
     * 다음 학습 관련 절차 수행 시 사용 된다 ( progressStatus > 1 )
     * @param _sentence 재생 될 문장
     */
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
     * 영어 학습 컨텐츠를 재생하는 경우 사용되는 메소드.
     * @param _sentence 영어 학습 컨텐츠
     * @return  재생된 영어 문장
     */
    private String sentenceToSpeech(Sentence _sentence){
        Log.d("LearningActivity", _sentence.getSentence());
        switch (_sentence.getLearningLevel()) {
            case 0: // 문장 레벨 0, 즉 인지 부하가 높음 상태에서 재생되는 문장
                Log.d("LearningActivity", "Sentence for high cog status... set speed reate to 0.75f");
                ttsModule.setTextSpeechRate(0.75f);
                break;
            case 1: // 문장 레벨 1, 즉 인지 부하가 중간 상태에서 재생되는 문장
                Log.d("LearningActivity", "Sentence for mid level sentence... set speed reate to 0.80f");
                ttsModule.setTextSpeechRate(0.80f);
                break;
            case 2: // 문장 레밸 2, 즉 인지 부하가 낮음 상태에서 재생되는 문장
                Log.d("LearningActivity", "sentence for low level sentence... set speed reate to 0.85f");
                ttsModule.setTextSpeechRate(0.85f);
                break;
            default:
        }

        ttsModule.setLanguage(Locale.US);
        ttsModule.speak(_sentence.getSentence());
        return _sentence.getSentence();
    }

    /**
     * 사용자의 음성 명령을 받는 단계에서 사용, 한국어를 재생할 때 사용한다
     * @param _sentence 재생할 문장 (한국어 , 안내 매세지)
     * @return 재생된 문장
     */
    private String sentenceToSpeech(String _sentence) {
        Log.d("LearningActivity", _sentence);
        ttsModule.setLanguage(Locale.KOREA);
        ttsModule.speak(_sentence);
        return _sentence;
    }

    /**
     * 주제가 변경되거나 더 많은 학습 컨텐츠를 제공해야할 시, 호출되는 메서드
     *
     * @param _newTopic 사용자의 새로운 주제
     */
    private void returnToGenerateActivity(String _newTopic) {

        Intent intent = new Intent(this, GenerateActivity.class);
        //변경된 주제에 맞게 변경
        editor.putString("topic", _newTopic);
        editor.apply();
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void returnToGenerateActivity() {

        Intent intent = new Intent(this, GenerateActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void goToResultActivity() {

        Intent intent = new Intent(this, ResultActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyTTSnSTT();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed(); -> 지우지 말 것
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
        handler.removeCallbacksAndMessages(null);
        destroyTTSnSTT();
    }
}