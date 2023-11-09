package com.example.chatgpt_english;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText userInput;
    private TextView responseView;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput = findViewById(R.id.userInput);
        responseView = findViewById(R.id.responseView);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener((view) -> {
            String inputText = userInput.getText().toString();
//            Log.d("TAG", (new ChatGPTTask().execute(inputText)) + "");
            responseView.setText(new ChatGPTTask().execute(inputText) + "");
        });

    }



    private class ChatGPTTask extends AsyncTask<String, Void, String> {
        String accessToken = "Put access token here";

        protected String doInBackground(String... inputs){
            String inputText = inputs[0];
            try{
                URL url = new URL(
                        "https://api.openai.com/v1/chat/completions");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String jsonInputString = "{\"prompt\": \"" + inputText + "\", \"max_tokens\": 150}";

                try(OutputStream os = connection.getOutputStream()){
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0 , input.length);
                }

                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8")
                )){
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null){
                        response.append(responseLine);
                    }

                    Log.d("TAG", response.toString());
                    return response.toString();
                }
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }
}
