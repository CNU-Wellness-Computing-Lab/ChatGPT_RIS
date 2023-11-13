package com.example.chatgpt_english.module;

public class CheckSentenceModule {
    public String preprocess_sentence(String sentence) {
        String cleanedSentence = sentence.replaceAll("[?.!]", "");

        return cleanedSentence;
    }

    public int calculateLevenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        // 초기화
        for (int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= str2.length(); j++) {
            dp[0][j] = j;
        }

        // 동적 계획법을 사용하여 편집 거리 계산
        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        // 계산 결과 반환
        return dp[str1.length()][str2.length()];
    }
}
