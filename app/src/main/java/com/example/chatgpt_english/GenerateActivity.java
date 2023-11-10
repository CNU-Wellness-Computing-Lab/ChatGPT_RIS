package com.example.chatgpt_english;

import androidx.annotation.NonNull;
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

import org.json.JSONObject;

import java.io.IOException;

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
    private Button nextBtn;

    private JSONObject jsonResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        responseView = findViewById(R.id.responseView);
        client = new OkHttpClient();

        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setEnabled(false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //TODO: 사용자 프로필 기반 영어 학습 컨텐츠 제공
        Log.d("TAG", sharedPreferences.getString("sex", "default"));

        // GPT api response test
        postRequest("What is your GPT version?");

        nextBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(this, LearningActivity.class);
            String gptResponse = jsonResponse.toString();
            intent.putExtra("gpt_json_data", gptResponse);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

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


    /**
     * GPT-4 API 연동을 위한 코드
     * 영어 학습 생성 Prompt을 위해 나중에 사용할 것!
     *
     * @param inputText Prompt에 적용될 text
     */
    private void postRequest(String inputText) {
        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
        String apiKey = "ADD API KEY HERE";
        String model = "gpt-4-1106-preview";
        String postBody = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + inputText + "\"}]}";

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
                            Log.d("TAG", jsonResponse.toString());
                            String textResponse = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                            Log.d("TAG", textResponse);
                            responseView.setText("영어 학습 준비 완료!");
                            nextBtn.setEnabled(true);
                            nextBtn.setVisibility(View.VISIBLE);

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