package com.example.chatgpt_english;

/**
 * 영어 학습 컨탠츠 정보를 저장하기 위한 클래스
 */
public class Sentence {
    private final String sentence;
    private boolean isLearned;
    private final int learningLevel;      // 영어 학습 문장의 난이도. 높을 수록 낮은 인지 부하일 때 재생 되도록 한다

    public Sentence(String _sentence, int _learningLevel){
        this.sentence = _sentence;
        this.isLearned = false;
        this.learningLevel = _learningLevel;
    }

    public void setLearned(boolean learned) {
        isLearned = learned;
    }

    public String getSentence() {
        return sentence;
    }
    public boolean getIsLearned(){
        return isLearned;
    }
    public int getLearningLevel() {
        return learningLevel;
    }
}
