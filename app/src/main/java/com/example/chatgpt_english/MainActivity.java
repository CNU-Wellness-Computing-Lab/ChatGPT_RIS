package com.example.chatgpt_english;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText userInput;
    private TextView responseView;
    private Button sendButton;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput = findViewById(R.id.userInput);
        responseView = findViewById(R.id.responseView);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener((view) -> {
            String inputText = userInput.getText().toString();
            postRequest(inputText);
        });
        client = new OkHttpClient();

    }

    private void postRequest(String inputText) {
        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
        String apiKey = "Put API key here";
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
                if(!response.isSuccessful()){
                    throw new IOException("Unexpected code " + response);
                } else {
                    final String responseData = response.body().string();

                    runOnUiThread(() -> {
                        try{
                            JSONObject jsonResponse = new JSONObject(responseData);
                            Log.d("TAG", jsonResponse.toString());
                            String textResponse = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                            responseView.setText(textResponse);
                        } catch (Exception e){
                            responseView.setText("Failed to parse the response");
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }
}
