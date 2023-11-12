package com.example.chatgpt_english;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

public class TTSModule implements OnInitListener {

    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;

    private Context ttsContext;

    public TTSModule(Context context) {
        textToSpeech = new TextToSpeech(context, this);
        ttsContext = context;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // TTS 초기화 성공
            isInitialized = true;
            setLanguage(Locale.getDefault()); // 기본 언어로 설정

            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.d("TTSModule", "TTS 시작: ");
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.d("TTSModule", "TTS 완료: ");
                }

                @Override
                public void onError(String utteranceId) {
                    Log.e("TTSModule", "TTS 에러: ");
                }
            });
        } else {
            Log.e("TTSModule", "TextToSpeech 초기화 실패");
        }
    }

    public void setLanguage(Locale locale) {
        if (isInitialized) {
            int result = textToSpeech.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSModule", "언어를 지원하지 않습니다.");
            }
        } else {
            Log.e("TTSModule", "TextToSpeech가 초기화되지 않았습니다.");
        }
    }

    public void speak(String text) {
        if (isInitialized) {
            String utteranceId = "utteranceId";
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            Log.e("TTSModule", "TextToSpeech가 초기화되지 않았습니다.");
        }
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();

            textToSpeech = new TextToSpeech(ttsContext, this);
        }
    }
}