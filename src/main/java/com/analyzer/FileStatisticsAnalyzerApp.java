package com.analyzer;

import com.analyzer.cli.CommandLineOptions;
import com.analyzer.model.TextReport;
import com.analyzer.service.ConcurrentTextAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class FileStatisticsAnalyzerApp {

    public static void main(String[] args) {
        CommandLineOptions options = CommandLineOptions.parse(args);

        if (options.isHelpRequested()) {
            printHelp();
            return;
        }

        System.out.println("============================================");
        System.out.println("         GELİŞMİŞ DOSYA İSTATİSTİK ANALİZİ v2.0");
        System.out.println("============================================");
        System.out.println("Dosya Yolu     : " + options.getFilePath());
        System.out.println("Format         : " + options.getFormat().toUpperCase());
        System.out.println("İşçi Sayısı    : " + options.getThreads() + " Thread");
        System.out.println("Top Listesi    : " + options.getTopN());
        System.out.println("--------------------------------------------");

        createSampleFileIfNotExists(options.getFilePath());

        try {
            Set<String> stopWords = loadStopWords(options.getStopWordsPath());

            ConcurrentTextAnalyzer analyzer = new ConcurrentTextAnalyzer(options.getThreads(), stopWords);
            long startTime = System.currentTimeMillis();
            TextReport report = analyzer.analyze(options.getFilePath());
            long endTime = System.currentTimeMillis();

            String format = options.getFormat().toLowerCase();
            if (format.equals("json")) {
                System.out.println(report.toJson(options.getTopN()));
            } else if (format.equals("csv")) {
                System.out.println(report.toCsv(options.getTopN()));
            } else {
                System.out.println(report.toPlainText(options.getTopN()));
            }

            System.out.println("\nAnaliz Süresi: " + (endTime - startTime) + " ms");

        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("Hata oluştu: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("Kullanım: java FileStatisticsAnalyzerApp [seçenekler]");
        System.out.println("Seçenekler:");
        System.out.println("  --file <path>       Analiz edilecek dosya yolu");
        System.out.println("  --format <type>     Çıktı formatı (txt, json, csv)");
        System.out.println("  --threads <num>     Kullanılacak iş parçacığı sayısı");
        System.out.println("  --top <num>         Listelenecek en sık kelime sayısı");
        System.out.println("  --stopwords <path>  Stop words dosya yolu");
    }

    private static void createSampleFileIfNotExists(String path) {
        try {
            if (!Files.exists(Paths.get(path))) {
                String sampleContent = "Java multithreading ve dosya analizi örneği. Bu proje Java programlama dili ile geliştirilmektedir.";
                Files.writeString(Paths.get(path), sampleContent);
                System.out.println("Örnek dosya oluşturuldu: " + path);
            }
        } catch (IOException e) {
            // Hata durumunda yoksay
        }
    }

    private static Set<String> loadStopWords(String path) {
        Set<String> stopWords = new HashSet<>();
        if (path == null || path.isEmpty()) {
            return stopWords;
        }
        try {
            if (Files.exists(Paths.get(path))) {
                stopWords.addAll(Files.readAllLines(Paths.get(path)));
            }
        } catch (IOException e) {
            System.err.println("Stop words dosyası okunamadı: " + e.getMessage());
        }
        return stopWords;
    }
}