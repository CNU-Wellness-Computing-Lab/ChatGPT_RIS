package com.example.chatgpt_english.result;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ResultData {
    public int size=0;
    private SharedPreferences sharedPreferences;
    public ArrayList<String> date = new ArrayList<>();
    public ArrayList<String> question = new ArrayList<>();
    public ArrayList<String> answer = new ArrayList<>();
    public ArrayList<String> cognitive_load = new ArrayList<>();
    public ArrayList<String> cognitive_load_category = new ArrayList<>();
    public ArrayList<String> topic = new ArrayList<>();

    // 생성자에서 Context를 받아 SharedPreferences 초기화
    public ResultData(Context context) {
        sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    // 데이터 저장 메서드

    /**
     * 0 : dater
     * 1 : question
     * 2 : answer
     * 3 : congitive_load
     * 4 : cognitive_load_category
     * 5 : topic
     *
     */
    //data[0], data[4], data[10], data[8], data[9], data[6]
    public void saveData(String[] value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(value!=null) {
            editor.putString("date" + size, value[0]);
            editor.putString("question" + size, value[4]);
            editor.putString("answer" + size, value[10]);
            editor.putString("cognitive_load" + size, value[8]);
            editor.putString("cognitive_load_category" + size, value[9]);
            editor.putString("topic" + size, value[6]);
            size++;
        }
        editor.apply();

        Log.d("initData", "save : "+getData("date0" )+" "+getData("question0" )+" "+getData("answer0" )+" "+getData("cognitive_load0" )+" "+getData("cognitive_load_category0" )+" "+getData("topic0" ));


    }

    // 데이터 불러오기 메서드
    public String getData(String key) {
        return sharedPreferences.getString(key, "null");
    }

    // 저장된 데이터의 개수 확인 메서드

    public void initData() {
        size = sharedPreferences.getAll().size()/6;

        if(size>0) {
            for (int i = 0; i < size; i++) {
                date.add(getData("date" + i));
                question.add(getData("question" + i));
                answer.add(getData("answer" + i));
                cognitive_load.add(getData("cognitive_load" + i));
                cognitive_load_category.add(getData("cognitive_load_category" + i));
                topic.add(getData("topic" + i));
                Log.d("initData",i+". init : "+getData("date" + i)+getData("question" + i)+getData("answer" + i)+getData("cognitive_load" + i)+getData("cognitive_load_category" + i)+getData("topic" + i));
            }
        }
    }
    public void removeAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    String getDate(int position){
        return date.get(position);
    }
    String getQuestion(int position){
        return question.get(position);
    }
    String getAnswer(int position){
        return answer.get(position);
    }
    String getCognitiveLoad(int position){
        return cognitive_load.get(position);
    }
    String getTopic(int position){
        return topic.get(position);
    }
}
