package com.analyzer.service;

import com.analyzer.model.TextReport;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class ConcurrentTextAnalyzer {
    private final int threadCount;
    private final Set<String> stopWords;

    public ConcurrentTextAnalyzer(int threadCount, Set<String> stopWords) {
        this.threadCount = threadCount;
        this.stopWords = stopWords;
    }

    public TextReport analyze(String filePath) throws IOException, InterruptedException, ExecutionException {
        List<String> allLines = Files.readAllLines(Paths.get(filePath));
        if (allLines.isEmpty()) {
            return new TextReport(0, 0, 0, 0.0, Collections.emptyMap());
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(threadCount, allLines.size()));
        List<Future<PartialResult>> futures = new ArrayList<>();

        int chunkSize = (int) Math.ceil((double) allLines.size() / threadCount);
        Locale turkishLocale = new Locale("tr", "TR");

        for (int i = 0; i < allLines.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, allLines.size());
            List<String> subList = allLines.subList(i, end);

            Callable<PartialResult> worker = () -> {
                long words = 0;
                long sentences = 0;
                long chars = 0;
                Map<String, Integer> freqMap = new HashMap<>();

                for (String line : subList) {
                    if (line.trim().isEmpty()) continue;

                    String[] sentParts = line.split("[.!?]+");
                    for (String s : sentParts) {
                        if (!s.trim().isEmpty()) sentences++;
                    }

                    String cleaned = line.toLowerCase(turkishLocale).replaceAll("[^a-zğüşıöç0-9\\s]", " ");
                    String[] wordsArr = cleaned.split("\\s+");

                    for (String w : wordsArr) {
                        if (w.length() > 1 && !stopWords.contains(w)) {
                            words++;
                            chars += w.length();
                            freqMap.put(w, freqMap.getOrDefault(w, 0) + 1);
                        }
                    }
                }
                return new PartialResult(words, sentences, chars, freqMap);
            };

            futures.add(executor.submit(worker));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        long totalWords = 0;
        long totalSentences = 0;
        long totalChars = 0;
        Map<String, Integer> mergedFrequencies = new HashMap<>();

        for (Future<PartialResult> future : futures) {
            PartialResult res = future.get();
            totalWords += res.words;
            totalSentences += res.sentences;
            totalChars += res.chars;

            res.freqMap.forEach((k, v) ->
                    mergedFrequencies.put(k, mergedFrequencies.getOrDefault(k, 0) + v)
            );
        }

        double avgLength = totalWords > 0 ? (double) totalChars / totalWords : 0.0;
        return new TextReport(totalWords, totalSentences, totalChars, avgLength, mergedFrequencies);
    }

    private static class PartialResult {
        long words;
        long sentences;
        long chars;
        Map<String, Integer> freqMap;

        public PartialResult(long words, long sentences, long chars, Map<String, Integer> freqMap) {
            this.words = words;
            this.sentences = sentences;
            this.chars = chars;
            this.freqMap = freqMap;
        }
    }
}