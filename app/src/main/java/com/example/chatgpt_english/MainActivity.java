package com.example.chatgpt_english;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;


/**
 * Start of the Page
 */
public class MainActivity extends AppCompatActivity {

    //    private String ip = "192.168.56.1";            // IP 번호
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        goToProfileSetting();
    }


    public void goToProfileSetting(){
        Intent intent = new Intent(this, ProfileSettingsActivity.class);
        startActivity(intent);
    }
}
