package com.example.chatgpt_english;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chatgpt_english.module.TTSModule;
import com.example.chatgpt_english.result.ResultData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ResultActivity extends AppCompatActivity {

    private TTSModule ttsModule;
    private TextView testTextView;
    private Handler handler;
    private ViewPager viewPager;

    private ListView list;
    List<ListView_Item> items = null;
    private LinearLayout resultLayout=null;
    static ResultData resultData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
//        testTextView = findViewById(R.id.testTextView);
        ttsModule = new TTSModule(this);
        handler = new Handler();
//        handler.postDelayed(()->{
//            runOnUiThread(() -> {
////                testTextView.setText(sentenceToSpeech("영어 학습이 모두 종료 되었습니다. 학습 결과를 운전이 완료된 후에 확인 해 주세요."));
//                    });
//                },1000);
        ListView listView = findViewById(R.id.listview_list);
        resultData = new ResultData(getApplicationContext());
        resultData.initData();
        // Item 리스트 선언 함수 init_ArrayList(20), 20은 추가할 아이템 개수
        init_ArrayList(resultData.size);

        ListView_Adapter mAdapter = new ListView_Adapter(this, items);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // AdapterView - 리스트뷰에 연결한 Adapter, getItemAtPosition(),
                // Adapter의 메소드 getItem()과 동일한 메소드
                ListView_Item item = (ListView_Item) adapterView.getItemAtPosition(position);
                if(item.getClick()) {
                    expandLayer(view, position,resultData);
                    mAdapter.setClick(position,true);
                } else {
                    reduceLayer(view, position,resultData);
                    mAdapter.setClick(position,false);
                }
                // 클릭한 위치의 Item Title 문자열 토스트로 보여주기
            }
        });
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



    /**
     * 각 결과 item 내부의 정보를 관리하는 클래스
     *
     * */
    private class ListView_Item {
        // 아이템 각각 내용, 'Title'
        private String title;
        private String time;

        // 아이템 각각 이미지 리소스 ID, 'Image'
        private int image;
        private boolean click=false;
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
        public boolean getClick() {
            click = !click;
            return click;
        }

    }
    //정답 list
    private class ListView_Adapter extends BaseAdapter {
        // 보여줄 Item 목록을 저장할 List
        Context context;
        List<ListView_Item> items = null;
        Boolean click[] = new Boolean[100];
//        ResultData resultData = new ResultData(getApplicationContext());

        // Adapter 생성자 함수
        public ListView_Adapter(Context context, List<ListView_Item> items) {
            this.items = items;
            this.context = context;
            this.click = new Boolean[items.size()];
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
        public boolean getClick(int position){
            return click[position];
        }

        public void setClick(int position, boolean click){
            this.click[position] = click;
        }

        /**
         * 각 item 내부의 정보를 관리하는 함수.
         *
         * */
        @SuppressLint("MissingInflatedId")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Infalter 구현 방법 1

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.result_item, parent, false);

            TextView result_time = view.findViewById(R.id.result_time);
            ImageView answer_img = view.findViewById(R.id.answer_img);
            TextView result_title = view.findViewById(R.id.title);
            TextView learningContent = view.findViewById(R.id.learningContent);
            TextView answerSentence = view.findViewById(R.id.answerSentence);
            TextView reason_line_num = view.findViewById(R.id.reason_line_num);
            TextView recommendation_title = view.findViewById(R.id.recommendation_title);
            TextView recommendation_content = view.findViewById(R.id.recommendation_content);

            /**
             * 시간 설정
             */
            Date date = new Date(Long.parseLong(resultData.getDate(position)));
            // SimpleDateFormat을 사용하여 원하는 형식으로 날짜 포맷 설정
            // "a"는 오전/오후, "hh"는 시간(12시간제), "mm"은 분
            SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm");
            // 사용자의 현지 시간대 설정
            sdf.setTimeZone(TimeZone.getDefault());
            result_time.setText(sdf.format(date).toString());
//            answer_img.setText(resultData.getDate(position));
            result_title.setText("학습 주제 : "+resultData.getTopic(position));
            learningContent.setText(resultData.getQuestion(position));
            answerSentence.setText(resultData.getAnswer(position));
            reason_line_num.setText(resultData.getDistance(position));


            learningContent.post(new Runnable() {
                @Override
                public void run() {


                    if (learningContent.getLineCount()>1 | answerSentence.getLineCount()>1) {
                        // 두 줄 이상일 때의 처리
                        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                        // 레이아웃 높이를 변경합니다. 현재 높이의 2배로 설정합니다.
                        layoutParams.height = (int) (layoutParams.height * 1.1);
                        Log.d("InitData","learningContent : "+learningContent.getLineCount()+"");
                        Log.d("InitData","answerSentence : "+answerSentence.getLineCount()+"");
                        view.setLayoutParams(layoutParams);

                    }
                }
            });

            if(" CORRECT".equals(resultData.getIsCorrect(position))) {
                answer_img.setImageResource(R.drawable.correctsign);
                recommendation_title.setVisibility(View.GONE);
                recommendation_content.setVisibility(View.GONE);
            }else if(" WRONG".equals(resultData.getIsCorrect(position))){
                if(" HIGH".equals(resultData.getCognitiveLoadCategory(position))){
                    answer_img.setImageResource(R.drawable.retrysign);
                    recommendation_content.setText("학습 당시 인지부하가 높았습니다.");

                }else if(" MEDIUM".equals(resultData.getCognitiveLoadCategory(position))){
                    answer_img.setImageResource(R.drawable.wrongsign);
                    recommendation_content.setText("학습 당시 인지부하가 보통이었습니다.");

                }else if(" LOW".equals(resultData.getCognitiveLoadCategory(position))){
                    answer_img.setImageResource(R.drawable.wrongsign);
                    recommendation_content.setText("학습 당시 인지부하가 낮았습니다.");
                }
            }
            try {
                if (getClick(position)) {
                    expandLayer(view, position, resultData);
                }
            } catch (Exception e){

            }
            return view;
        }
    }
    /**
     * 함수 초기화
     *
     * */
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
    /**
     * 클릭시 item 확장 함수.
     *
     * */
    private void expandLayer(View view, int position, ResultData resultData) {
        TextView learningContent = view.findViewById(R.id.learningContent);
        TextView answerSentence = view.findViewById(R.id.answerSentence);

        LinearLayout layout = view.findViewById(R.id.result_content);
        layout.setVisibility(View.VISIBLE);
        ImageView imageView = (ImageView) view.findViewById(R.id.checkDetail);
        imageView.setImageResource(R.drawable.checkreverse);
        // check image toggle
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        // 레이아웃 높이를 변경합니다. 현재 높이의 2배로 설정합니다.
        if(" CORRECT".equals(resultData.getIsCorrect(position))) {
            layoutParams.height = (int) (layoutParams.height * 1.8);

        }else if(answerSentence.getLineCount()>1| learningContent.getLineCount()>1 | answerSentence.getLineCount()>1|" WRONG".equals(resultData.getIsCorrect(position))|" ERROR".equals(resultData.getIsCorrect(position))){
            layoutParams.height = (int) (layoutParams.height * 2.1);
        }
        Log.d("InitData","learningContent : "+learningContent.getLineCount()+"");
        Log.d("InitData","answerSentence : "+answerSentence.getLineCount()+"");
        // 변경된 레이아웃 파라미터를 뷰에 적용합니다.
        view.setLayoutParams(layoutParams);
    }

    /**
     * 클릭시 item 축소 함수.
     *
     * */
    private void reduceLayer(View view, int position,  ResultData resultData) {
        TextView learningContent = view.findViewById(R.id.learningContent);
        TextView answerSentence = view.findViewById(R.id.answerSentence);

        LinearLayout layout = view.findViewById(R.id.result_content);
        layout.setVisibility(View.GONE);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        // check image toggle
        ImageView imageView = (ImageView) view.findViewById(R.id.checkDetail);
        imageView.setImageResource(R.drawable.check);
        // 레이아웃 높이를 변경합니다. 현재 높이의 1/2배로 설정합니다.
        if(" CORRECT".equals(resultData.getIsCorrect(position))) {
            layoutParams.height = (int) (layoutParams.height / 1.8);
        }else if(learningContent.getLineCount()>1 |" WRONG".equals(resultData.getIsCorrect(position))|" ERROR".equals(resultData.getIsCorrect(position))){
            layoutParams.height = (int) (layoutParams.height / 2.1);
        }else if(answerSentence.getLineCount()>1|answerSentence.getLineCount()>1){
            layoutParams.height = (int) (layoutParams.height / 2.2);
        }

        // 변경된 레이아웃 파라미터를 뷰에 적용합니다.
        view.setLayoutParams(layoutParams);
    }


}