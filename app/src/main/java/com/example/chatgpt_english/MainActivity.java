package com.example.chatgpt_english;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.chatgpt_english.connect_PC.PC_connector;


/**
 * Start of the Page
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * PC 통신을 위한 코드
         */
        PC_connector.connect();
        goToProfileSetting();
    }

    public void goToProfileSetting(){
        Intent intent = new Intent(this, ProfileSettingsActivity.class);
        startActivity(intent);
    }
}
