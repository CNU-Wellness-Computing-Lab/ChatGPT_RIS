package com.example.chatgpt_english.module;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CSVModule {
//    사용법
//    CSVModule.writeCsvFile(getApplicationContext(),"English_Learning.csv","start, test");

    public static void writeCsvFile(Context context, String fileName, String data) {
        BufferedWriter bufferedWriter = null;

        try {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadFolder, fileName);

            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true),
                    StandardCharsets.UTF_8));
            // UTF-8 BOM 추가
            if (file.length() == 0) { // 파일이 비어있을 때만 BOM을 추가
                bufferedWriter.write('\ufeff');
            }
            bufferedWriter.append(data);
            bufferedWriter.newLine();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    Log.d("CSVModule", "Write data: " + data );
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
