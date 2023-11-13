package com.example.chatgpt_english;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


/**
 * Start of the Page
 */
public class MainActivity extends AppCompatActivity {

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
