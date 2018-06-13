package edu.pitt.medschool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * File checker entry point
 */
public class FileChecker {

    private int paraCount;
    private ReportService rs;
    private final BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();

    public FileChecker(String reportPath, double loadFactor) throws IOException {
        int availCores = Runtime.getRuntime().availableProcessors();
        paraCount = (int) Math.round(loadFactor * availCores);
        paraCount = paraCount > 0 ? paraCount : 1;
        rs = new ReportService(reportPath);
    }

    /**
     * Check files!
     */
    public void startCheck() {
        ExecutorService scheduler = Executors.newFixedThreadPool(paraCount);
        Runnable importTask = () -> {
            Path aFilePath;
            while ((aFilePath = fileQueue.poll()) != null) {
                // Check here
            }
        };

        for (int i = 0; i < paraCount; ++i)
            scheduler.submit(importTask);
        scheduler.shutdown();
    }

    public void AddOneFile(String path) {
        fileQueue.offer(Paths.get(path));
    }

    public void AddArrayFiles(String[] paths) {
        for (String aPath : paths) {
            this.AddOneFile(aPath);
        }
    }

}