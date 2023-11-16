package com.example.chatgpt_english.module;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVModule {
//    사용법
//    CSVModule.writeCsvFile(getApplicationContext(),"English_Learning.csv","start, test");

    public static void writeCsvFile(Context context, String fileName, String data) {
        FileWriter fileWriter = null;

        try {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadFolder, fileName);
            fileWriter = new FileWriter(file,true);

            fileWriter.append(data);
            fileWriter.append("\n");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
