package com.example.chatgpt_english;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivities(new Intent[]{new Intent(MainActivity.this, LearningProcessActivity.class)});
    }
}

//# Gradle files
//        .gradle/
//        build/
//
//        # Local configuration file (sdk path, etc)
//        local.properties
//
//        # Log/OS Files
//        *.log
//
//        # Android Studio generated files and folders
//        captures/
//        .externalNativeBuild/
//        .cxx/
//        *.apk
//        output.json
//
//        # IntelliJ
//        *.iml
//        .idea/
//        misc.xml
//        deploymentTargetDropDown.xml
//        render.experimental.xml
//
//        # Keystore files
//        *.jks
//        *.keystore
//
//        # Google Services (e.g. APIs or Firebase)
//        google-services.json
//
//        # Android Profiling
//        *.hprof