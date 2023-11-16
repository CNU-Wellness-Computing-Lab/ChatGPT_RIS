package com.example.chatgpt_english;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.chatgpt_english.module.TTSModule;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * GPT-4에게 영어 학습 컨텐츠 제공을 위한 prompt 전송 및 전달 받는 activity
 */
public class GenerateActivity extends AppCompatActivity {
    private TextView responseView;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;

    private String drivingSkill;
    private String englishSkill;
    private String topic;

    private JSONObject jsonResponse;
    private String[] parsedContent;

    private Handler handler;
    private TTSModule ttsModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        responseView = findViewById(R.id.responseView);
        handler = new Handler();
        ttsModule = new TTSModule(this);

        client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        drivingSkill = sharedPreferences.getString("driving_exp", "1");
        englishSkill = sharedPreferences.getString("english_skill", "1");
        topic = sharedPreferences.getString("topic", "랜덤 주제");

        Log.d("GenerateActivity", "driving skill= " + drivingSkill + "| english skill= " + englishSkill + "| topic= " + topic);

        // GPT api response test
        String input = "당신의 역할은 '영어 학습 컨텐츠 제공자'입니다. " +
                "운전 실력을 1 ~ 10이라고 할 때, 1은 초보자, 10은 숙련자와 같다고 하고, " +
                "영어 실력을 1 ~ 10이라고 할 때, 1은 초보자, 10은 영어 언어학자 수준하고 같다고 해." +
                "이때 영어 문장 생성을 위해 조건 1, 조건 2, 조건 3에 따라 생성해. 조건은 다음과 같음. "+
                "조건 1은 사용자의 운전 실력은" + drivingSkill + " 영어 실력은" + englishSkill + " 주제는 " + topic + "으로 설정할 때" +
                "사용자의 운전 실력과 영어 실력에 따라 영어 학습 문장 난이도를 조절해야함. "+
                "조건 2는 주제에 맞는 영어 학습을 하기 위한 영어 문장을 " +
                "영어 단어의 개수가 3개인 문장 3개, 영어 단어의 개수가 4개인 문장 3개, 영어 단어의 개수가 5개인 문장 3개, 즉 총 9개 문장을 생성해야함. " +
                "조건 3은 아래의 예시와 같이 학습을 위한 오직 영어 문장만 출력하고, 이 외 다른 응답은 출력하지말아야 함." +
                "Again, you MUST only say the requested total number of 9 english sentences for learning and do not contain numbering" +
                "출력 예시 다음과 같아. I am a boy";
        postRequest(input);

    }

    private void goToLearningActivity() {
        Intent intent = new Intent(this, LearningActivity.class);
        intent.putExtra("parsed_content", parsedContent);

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ttsModule.shutdown();
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

    public String[] parseResponse(String response) {
        String[] parsedResponse = response.split("\n");
        ArrayList<String> finalResponse = new ArrayList<>();
        //check parsing error
        for (int i = 0; i < parsedResponse.length; i++) {
            if (!parsedResponse[i].equals("")) {
                finalResponse.add(parsedResponse[i]);
            }
        }

        Collections.sort(finalResponse, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                int comp1 = s1.split("\\s+").length;
                int comp2 = s2.split("\\s+").length;
                return Integer.compare(comp1, comp2);
            }
        });

        return finalResponse.toArray(new String[0]);
    }


    /**
     * GPT-4 API 연동을 위한 코드
     * 영어 학습 생성 Prompt을 위해 나중에 사용할 것!
     *
     * @param inputText Prompt에 적용될 text
     */
    private void postRequest(String inputText) {
        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
        String apiKey = "PUT API KEY HERE";
        String model = "gpt-4-1106-preview";
        String postBody = "{\"model\": \"" + model + "\", " +
                "\"messages\": [" +
                "{\"role\": \"user\", " +
                "\"content\": \"" + inputText + "\"}" +
                "]" +
                "}";

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(RequestBody.create(postBody, MEDIA_TYPE_JSON))
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> responseView.setText("Failed to connect to the server"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        responseView.setText("Unexpected code " + response);
                    });
                    throw new IOException("Unexpected code " + response);
                } else {
                    final String responseData = response.body().string();

                    runOnUiThread(() -> {
                        try {
                            jsonResponse = new JSONObject(responseData);
                            Log.d("GenerateActivity", jsonResponse.toString());
                            parsedContent = parseResponse(jsonResponse.
                                    getJSONArray("choices").
                                    getJSONObject(0).
                                    getJSONObject("message").
                                    getString("content")
                            );

                            responseView.setText("영어 학습 준비 완료!");
                            ttsModule.setLanguage(Locale.KOREA);
                            ttsModule.speak("잠시후 영어 학습을 시작하도록 하겠습니다. 딩동 소리 이후에 들으신 문장을 따라 해 주세요");
                            handler.postDelayed(() -> {goToLearningActivity();}, 10000);


                        } catch (Exception e) {
                            responseView.setText("Failed to parse the response");
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }
}