package com.example.chatgpt_english;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chatgpt_english.module.TTSModule;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private TTSModule ttsModule;
    private TextView testTextView;
    private Handler handler;
    private ViewPager viewPager;

    private ListView list;
    String mTitle[]={"사과","바나나","수박","딸기","오렌지"};//listview에 title부분 설정
    String mDescription[]={"사과는 빨간색","바나나는 노란색","수박은 초록색","딸기는 빨간색","오렌지는 주황색"};//listview에 설명부분
    int images[]={R.drawable.ic_launcher_background,R.drawable.ic_launcher_background,R.drawable.ic_launcher_background,R.drawable.ic_launcher_background,R.drawable.ic_launcher_background};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        testTextView = findViewById(R.id.testTextView);
        ttsModule = new TTSModule(this);
        handler = new Handler();

        handler.postDelayed(()->{
            runOnUiThread(() -> {
                testTextView.setText(sentenceToSpeech("영어 학습이 모두 종료 되었습니다. 학습 결과를 운전이 완료된 후에 확인 해 주세요."));
                    });
                },1000);

        list=(ListView)findViewById(R.id.listview_list);

        MyAdapter adapter=new MyAdapter(this,mTitle,mDescription,images);
        list.setAdapter(adapter);//리스트에 어뎁터 설정
    }
    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        String rTitle[];
        String rDescription[];
        int rImgs[];

        MyAdapter(Context c, String title[],String description[],int imgs[]){
            super(c,R.layout.result_item,R.id.textView1,title);
            this.context=c;
            this.rTitle=title;
            this.rDescription=description;
            this.rImgs=imgs;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            //앞에서 만든 row xml파일을 view 객체로 만들기 위해서는 layoutInflater를 이용
            LayoutInflater layoutInflater=(LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row=layoutInflater.inflate(R.layout.result_item,parent,false);

            ImageView images=row.findViewById(R.id.image);
            TextView myTitle=row.findViewById(R.id.textView1);
            TextView myDescription=row.findViewById(R.id.textView2);

            images.setImageResource(rImgs[position]);
            myTitle.setText(rTitle[position]);
            myDescription.setText(rDescription[position]);


            return row;//앞에서 만든 xml 파일
        }
    }

    private String sentenceToSpeech(String _sentence) {
        assert ttsModule != null;
        Log.d("LearningActivity", _sentence);
        ttsModule.setLanguage(Locale.KOREA);
        ttsModule.speak(_sentence);
        return _sentence;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("앱 종료")
                .setMessage("정말로 종료하겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onPause();
                        finishAffinity();
                        onDestroy();
                    }
                })
                .setNegativeButton("아니오", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsModule.shutdown();
    }
}