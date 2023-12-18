package com.example.chatgpt_english;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatgpt_english.module.TTSModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private TTSModule ttsModule;
    private TextView testTextView;
    private Handler handler;
    private ViewPager viewPager;

    private ListView list;
    List<ListView_Item> items = null;

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
        ListView listView = findViewById(R.id.listview_list);

        // Item 리스트 선언 함수 init_ArrayList(20), 20은 추가할 아이템 개수
        init_ArrayList(20);

        // 만들어진 item 리스트로 Aapter 생성
        ListView_Adapter mAdapter = new ListView_Adapter(this, items);
        // ListView에 Adapter 연결
        listView.setAdapter(mAdapter);

        // ListView Item 클릭이벤트 처리
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // AdapterView - 리스트뷰에 연결한 Adapter, getItemAtPosition(),
                // Adapter의 메소드 getItem()과 동일한 메소드
                ListView_Item item = (ListView_Item) adapterView.getItemAtPosition(position);

                // 클릭한 위치의 Item의 title 문자열 반환
                String title = item.getTitle();
                // 클릭한 위치의 Item Title 문자열 토스트로 보여주기
                Toast.makeText(getApplicationContext(), title, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private class ListView_Item {
        // 아이템 각각 내용, 'Title'
        private String title;
        // 아이템 각각 이미지 리소스 ID, 'Image'
        private int image;

        // 생성자 함수
        public ListView_Item(String title, int image) {
            this.title = title;
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public int getImage() {
            return image;
        }
    }
    private class ListView_Adapter extends BaseAdapter {
        // 보여줄 Item 목록을 저장할 List
        Context context;
        List<ListView_Item> items = null;

        // Adapter 생성자 함수
        public ListView_Adapter(Context context, List<ListView_Item> items) {
            this.items = items;
            this.context = context;
        }

        // Adapter.getCount(), 아이템 개수 반환 함수
        @Override
        public int getCount() {
            return items.size();
        }

        // Adapter.getItem(int position), 해당 위치 아이템 반환 함수
        @Override
        public ListView_Item getItem(int position) {
            return items.get(position);
        }

        // Adapter.getItemId(int position), 해당 위치 반환 함수
        @Override
        public long getItemId(int position) {
            return position;
        }

        // Adapter.getView() 해당위치 뷰 반환 함수
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Infalter 구현 방법 1
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.result_item, parent, false);

            // ListView의 Item을 구성하는 뷰 연결
            TextView number = view.findViewById(R.id.listitem_number);
            TextView title = view.findViewById(R.id.listitem_title);
            ImageView image = view.findViewById(R.id.listitem_image);

            // ListView의 Item을 구성하는 뷰 세팅
            ListView_Item item = items.get(position);
            number.setText(String.valueOf(position+1));		// 해당위치 +1 설정, 배열순으로 0부터 시작
            title.setText(item.getTitle());					// item 객체 내용을 가져와 세팅
            image.setImageResource(item.getImage());		// item 객체 내용을 가져와 세팅

            // 설정한 view를 반환해줘야 함
            return view;
        }
    }
    private void init_ArrayList(int count) {
        // item을 저장할 List 생성
        items = new ArrayList<>();

        // Drawable 이미지 리소스 ID 값을 가져오기 위해 Resource객체 생성
        Resources res = getResources();

        // 함수의 인자로 넘겨준 count 아이템 개수만큼 반복, 아이템 추가
        for (int i = 0; i < count; i++) {
            // 이미지리소스 id값을 가져옴, res.getIdentifier("이미지 이름", "리소스 폴더 이름", 현재패키지 이름)
            int img_ID = res.getIdentifier("listview_item" + (i % 4), "drawable", getPackageName());
            // item 객체 생성하여 리스트에 추가
            items.add(new ListView_Item((i + 1) + "번째 아이템", img_ID));
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