package edu.pitt.medschool;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.*;

import static edu.pitt.medschool.CheckFunc.*;
import static edu.pitt.medschool.Util.*;

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
        Runnable checkerTask = () -> {
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
            scheduler.submit(checkerTask);
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            rs.closeService();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        System.out.println(String.format(
                "[%s] Checking file: %s", Validator.global_operation_sdf.format(new Date()), fileFullPath));
        ReportService.Report fileReport = new ReportService.Report(fileFullPath, fileName, Files.size(pFile));

        if (!fileName.toUpperCase().startsWith("PUH")) {
            fileReport.addHardProblem("Naming incorrect!");
            return fileReport;
        }

        String[] fileInfo = checkerFromFilename(fileName);
        String pid = fileInfo[0];

        // Ar/NoAr Check & Response
        if (fileInfo[1] == null) {
            fileReport.addHardProblem("Ambiguous Ar/NoAr in file name.");
        }

        // Open and read file to check it
        BufferedReader reader = Files.newBufferedReader(pFile);
        String fileUUID = firstLineInCSV(reader.readLine(), pid, fileReport);
        fileReport.setEegUUID(fileUUID);

        // Next 6 lines no use expect time date
        String test_date = "";
        for (int i = 0; i < 6; i++) {
            String tmp = reader.readLine();
            switch (i) {
                case 3:
                case 4:
                    test_date += tmp.split(",")[1].trim();
                    break;
            }
        }
        long test_start_time = -1;
        try {
            test_start_time = dateTimeFormatToTimestamp(
                    test_date, "yyyy.MM.ddHH:mm:ss", TimeZone.getTimeZone("America/New_York"));
        } catch (ParseException e) {
            fileReport.addHardProblem("Test date malformat");
        }

        // 8th Line is column header line
        String eiL = reader.readLine();
        String[] columnNames = eiL.split(",");
        int columnCount = columnNames.length;

        // More integrity checking
        if (!columnNames[0].toLowerCase().equals("clockdatetime")) {
            fileReport.addHardProblem("Wriong first column!");
        }

        long totalLines = 0;
        String aLine;
        while ((aLine = reader.readLine()) != null) {
            String[] values = aLine.split(",");
            totalLines++;

            // Check this line generally
            if (columnCount != values.length) {
                fileReport.addSoftProblem(String.format(
                        "Column count error on line %d, expect %d found %d!",
                        totalLines + 8, columnCount, values.length));
                continue;
            }

            // Chech this line date time
            // Measurement time should be later than test start time
            long measurement_epoch_time = Util.serialTimeToLongDate(values[0], null);
            if (measurement_epoch_time < test_start_time) {
                fileReport.addSoftProblem(String.format(
                        "Measurement time ealier than test start time on line %d!", totalLines + 8));
                continue;
            }

            // Try parse
            int parseProblemId = -1;
            for (int i = 1; i < values.length; i++) {
                try {
                    Double.valueOf(values[i]);
                } catch (NumberFormatException e) {
                    parseProblemId = i;
                    break;
                }
            }
            if (parseProblemId != -1) {
                fileReport.addSoftProblem(String.format(
                        "Failed to parse the #%d number on line %d!", parseProblemId + 1, totalLines + 8));
                continue;
            }

            // And more?

        }

        reader.close();

        return fileReport;
    }

}