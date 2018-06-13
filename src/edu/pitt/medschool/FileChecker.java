package edu.pitt.medschool;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static edu.pitt.medschool.CheckFunc.checkerFromFilename;

/**
 * File checker entry point
 */
public class FileChecker {

    private int paraCount;
    private ReportService rs;
    private final BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();

    public FileChecker(ReportService r, double loadFactor) throws IOException {
        int availCores = Runtime.getRuntime().availableProcessors();
        paraCount = (int) Math.round(loadFactor * availCores);
        paraCount = paraCount > 0 ? paraCount : 1;
        rs = r;
    }

    /**
     * Check files!
     */
    public void startCheck() {
        ExecutorService scheduler = Executors.newFixedThreadPool(paraCount);
        Runnable importTask = () -> {
            Path aFilePath;
            while ((aFilePath = fileQueue.poll()) != null) {
                try {
                    rs.WriteOut(internalCheckOne(aFilePath));
                } catch (IOException e) {
                    System.err.println("Error when reading file: " + aFilePath.toString());
                }
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

    private ReportService.Report internalCheckOne(Path pFile) throws IOException {
        String fileFullPath = pFile.toString(), fileName = pFile.getFileName().toString();
        System.out.println("Checking file: " + fileFullPath);
        ReportService.Report fileReport = new ReportService.Report(fileFullPath, fileName, Files.size(pFile));

        String[] fileInfo = checkerFromFilename(fileName);

        // Ar/NoAr Check & Response
        if (fileInfo[1] == null) {
            fileReport.addHardProblem("Ambiguous Ar/NoAr in file name.");
        }

        BufferedReader reader = Files.newBufferedReader(pFile);


        return fileReport;
    }

}