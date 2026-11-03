package com.analyzer.model;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class TextReport {
    private final long totalWords;
    private final long totalSentences;
    private final long totalCharacters;
    private final double averageWordLength;
    private final Map<String, Integer> wordFrequencies;

    public TextReport(long totalWords, long totalSentences, long totalCharacters,
                      double averageWordLength, Map<String, Integer> wordFrequencies) {
        this.totalWords = totalWords;
        this.totalSentences = totalSentences;
        this.totalCharacters = totalCharacters;
        this.averageWordLength = averageWordLength;
        this.wordFrequencies = wordFrequencies;
    }

    public String toPlainText(int topN) {
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------------\n");
        sb.append("            ANALİZ SONUÇLARI (TXT)        \n");
        sb.append("------------------------------------------\n");
        sb.append(String.format("🔹 Toplam Cümle Sayısı   : %d\n", totalSentences));
        sb.append(String.format("🔹 Toplam Kelime Sayısı  : %d\n", totalWords));
        sb.append(String.format("🔹 Toplam Karakter (Harf) : %d\n", totalCharacters));
        sb.append(String.format("🔹 Ortalama Kelime Boyu  : %.2f karakter\n", averageWordLength));
        sb.append("------------------------------------------\n");
        sb.append("📊 En Sık Kullanılan ").append(topN).append(" Anlamlı Kelime:\n");

        getSortedFrequencies(topN).forEach(entry ->
                sb.append(String.format("   • %-15s : %d kez\n", entry.word, entry.count))
        );
        sb.append("==========================================");
        return sb.toString();
    }

    public String toJson(int topN) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"totalSentences\": ").append(totalSentences).append(",\n");
        sb.append("  \"totalWords\": ").append(totalWords).append(",\n");
        sb.append("  \"totalCharacters\": ").append(totalCharacters).append(",\n");
        sb.append("  \"averageWordLength\": ").append(String.format(Locale.US, "%.2f", averageWordLength)).append(",\n");
        sb.append("  \"topFrequencies\": {\n");

        List<WordFreq> sorted = getSortedFrequencies(topN);
        for (int i = 0; i < sorted.size(); i++) {
            WordFreq entry = sorted.get(i);
            sb.append("    \"").append(entry.word).append("\": ").append(entry.count);
            if (i < sorted.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  }\n");
        sb.append("}");
        return sb.toString();
    }

    public String toCsv(int topN) {
        StringBuilder sb = new StringBuilder();
        sb.append("Kelime,Frekans\n");
        getSortedFrequencies(topN).forEach(entry ->
                sb.append(entry.word).append(",").append(entry.count).append("\n")
        );
        return sb.toString();
    }

    private List<WordFreq> getSortedFrequencies(int topN) {
        return wordFrequencies.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .map(e -> new WordFreq(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public static class WordFreq {
        public final String word;
        public final int count;
        public WordFreq(String word, int count) {
            this.word = word;
            this.count = count;
        }
    }
}