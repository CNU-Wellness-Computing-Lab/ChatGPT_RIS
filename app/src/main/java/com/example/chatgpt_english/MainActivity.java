package com.example.chatgpt_english;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatgpt_english.connect_PC.PC_connector;
public class MainActivity extends AppCompatActivity{
    Button connect_btn;                 // ip 받아오는 버튼

    EditText ip_edit;               // ip 에디트
    TextView show_text;             // 서버에서온거 보여주는 에디트
    // 소켓통신에 필요한것


    //    private String ip = "192.168.56.1";            // IP 번호
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect_btn = (Button)findViewById(R.id.connect_btn);
        connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PC_connector.connect();
            }
        });

        show_text = (TextView)findViewById(R.id.show_text);

    }



}
