package com.analyzer.cli;

public class CommandLineOptions {
    private String filePath = "sample.txt";
    private String format = "txt";
    private int threads = Runtime.getRuntime().availableProcessors();
    private int topN = 10;
    private String stopWordsPath = "";
    private boolean helpRequested = false;

    public static CommandLineOptions parse(String[] args) {
        CommandLineOptions options = new CommandLineOptions();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--file":
                    if (i + 1 < args.length) options.filePath = args[++i];
                    break;
                case "--format":
                    if (i + 1 < args.length) options.format = args[++i];
                    break;
                case "--threads":
                    if (i + 1 < args.length) {
                        try {
                            options.threads = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            // Hatalı sayı girilirse varsayılan kalır
                        }
                    }
                    break;
                case "--top":
                    if (i + 1 < args.length) {
                        try {
                            options.topN = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            // Hatalı sayı girilirse varsayılan kalır
                        }
                    }
                    break;
                case "--stopwords":
                    if (i + 1 < args.length) options.stopWordsPath = args[++i];
                    break;
                case "--help":
                case "-h":
                    options.helpRequested = true;
                    break;
            }
        }
        return options;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFormat() {
        return format;
    }

    public int getThreads() {
        return threads;
    }

    public int getTopN() {
        return topN;
    }

    public String getStopWordsPath() {
        return stopWordsPath;
    }

    public boolean isHelpRequested() {
        return helpRequested;
    }
}