package com.example.chatgpt_english;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.chatgpt_english.connect_PC.PC_connector;


/**
 * Start of the Page
 */
public class MainActivity extends AppCompatActivity {


    //    private String ip = "192.168.56.1";            // IP 번호
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
        PC_connector.connect();
        startActivity(intent);
    }
}
