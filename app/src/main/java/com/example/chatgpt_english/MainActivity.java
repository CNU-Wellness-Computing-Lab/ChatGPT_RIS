package com.example.chatgpt_english;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.chatgpt_english.data.drivingData;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    TextView speed;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speed = findViewById(R.id.speed);
        btn = findViewById(R.id.testButton);
        speed.setText(drivingData.speed+"");

        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//                checkExternalStorage();
                File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)+"/somefile.txt"+"/somefile.txt");
                Log.d("somefile", file.getAbsolutePath());

                Toast.makeText(getApplicationContext(),"sdcard/Download"+"/somefile.txt",Toast.LENGTH_SHORT).show();
                try {
                    Log.d("somefile", file.getAbsolutePath());

                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    Log.d("somefile", "외부메모리 읽기 쓰기 모두 가능");

                    StringBuffer buffer = new StringBuffer();
                    String line;
                    while ((line=reader.readLine())!=null){
                         buffer.append(line);
                    }
                    reader.close();

                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"오류", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    boolean checkExternalStorage() {
        String state = Environment.getExternalStorageState();
        // 외부메모리 상태
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // 읽기 쓰기 모두 가능
            Log.d("STATE", "외부메모리 읽기 쓰기 모두 가능");
            Toast.makeText(getApplicationContext(),"외부메모리 읽기 쓰기 모두 가능", Toast.LENGTH_SHORT).show();
            return true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            //읽기전용
            Log.d("STATE", "외부메모리 읽기만 가능");
            Toast.makeText(getApplicationContext(),"외부메모리 읽기만 가능",Toast.LENGTH_SHORT).show();
            return false;
        } else {
            // 읽기쓰기 모두 안됨
            Log.d("STATE", "외부메모리 읽기쓰기 모두 안됨 : "+ state);
            Toast.makeText(getApplicationContext(),"외부메모리 읽기쓰기 모두 안됨 : "+ state,Toast.LENGTH_SHORT).show();
            return false;
        }
    }
//    public void readFile() {
//
//        String fileTitle = "somefile.txt";
//        File file = new File(Environment.DIRECTORY_DOWNLOADS(), fileTitle);
//
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(file));
//            String result = "";
//            String line;
//            while ((line = reader.readLine()) != null) {
//                result += line;
//            }
//
//            System.out.println( "불러온 내용 : " + result);
//
//            reader.close();
//        } catch (FileNotFoundException e1) {
//
//        } catch (IOException e2) {
//
//        }
//
//    }

}
