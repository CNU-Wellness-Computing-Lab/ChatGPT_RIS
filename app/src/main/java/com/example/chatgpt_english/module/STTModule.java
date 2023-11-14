package com.example.chatgpt_english.module;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class STTModule {

    private Context sttContext;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;

    private STTListener sttListener;

    private CheckSentenceModule checkSentenceModule;
    private Handler mainThreadHandler;

    // chat gpt가 생성한 문장
    private String inputText;

    public interface STTListener {
        void onSTTResult(String result);

        void onSTTError(String errorMessage);

        void onLearningResult(String result);
    }

    public STTModule(Context context, STTListener listener) {
        sttContext = context;
        sttListener = listener;
        checkSentenceModule = new CheckSentenceModule();
        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(sttContext)) {
            this.mainThreadHandler = new Handler(Looper.getMainLooper());
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(sttContext);
                    speechRecognizer.setRecognitionListener(new RecognitionListener() {
                        @Override
                        public void onReadyForSpeech(Bundle params) {
                            Log.d("STTModule", "onReadyForSpeech");
                        }

                        @Override
                        public void onBeginningOfSpeech() {
                            Log.d("STTModule", "onBeginningOfSpeech");
                        }

                        @Override
                        public void onRmsChanged(float rmsdB) {
                            // Not used
                        }

                        @Override
                        public void onBufferReceived(byte[] buffer) {
                            // Not used
                        }

                        @Override
                        public void onEndOfSpeech() {
                            Log.d("STTModule", "onEndOfSpeech");
                        }

                        @Override
                        public void onError(int error) {
                            Log.e("STTModule", "onError: " + error);
                            if (sttListener != null) {
                                sttListener.onSTTError("Error during speech recognition");
                            }
                            isListening = false;
                        }

                        @Override
                        public void onResults(Bundle results) {
                            Log.d("STTModule", "onResults");
                            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                            if (matches != null && !matches.isEmpty()) {
                                String result = matches.get(0);
                                if (sttListener != null) {
                                    sttListener.onSTTResult(result);
                                    String s1 = checkSentenceModule.preprocess_sentence(inputText);
                                    String s2 = checkSentenceModule.preprocess_sentence(result);
                                    sttListener.onLearningResult(String.valueOf(checkSentenceModule.calculateLevenshteinDistance(s1, s2)));
                                }
                            }
                            isListening = false;
                        }

                        @Override
                        public void onPartialResults(Bundle partialResults) {
                            // Not used
                        }

                        @Override
                        public void onEvent(int eventType, Bundle params) {
                            // Not used
                        }
                    });
                }
            });
        } else {
            Log.e("STTModule", "Speech recognition not available on this device.");
        }
    }

    public void startListening(String input) {
        inputText = input;

        if (!isListening) {
            isListening = true;
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US"); // Language code for English (United States)
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, sttContext.getPackageName());

            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    speechRecognizer.startListening(intent);
                }
            });
        } else {
            Log.w("STTModule", "Already listening. Ignoring start request.");
        }
    }

    public void stopListening() {
        if (isListening) {
            speechRecognizer.stopListening();
        } else {
            Log.w("STTModule", "Not currently listening. Ignoring stop request.");
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}