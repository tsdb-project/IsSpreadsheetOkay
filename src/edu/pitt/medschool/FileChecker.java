package edu.pitt.medschool;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
    private boolean isGUIMode;
    private TimeZone tz;

    public FileChecker(ReportService r, double loadFactor, boolean gui, TimeZone t) {
        int availCores = Runtime.getRuntime().availableProcessors();
        this.isGUIMode = gui;
        paraCount = (int) Math.round(loadFactor * availCores);
        paraCount = paraCount > 0 ? paraCount : 1;
        rs = r;
        tz = t;
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
            if (isGUIMode)
                showMsgbox("Checking finished!", "Done!", JOptionPane.INFORMATION_MESSAGE);
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
        Date tmp = null;
        try {
            tmp = dateTimeFormatToDate(test_date, "yyyy.MM.ddHH:mm:ss", tz);
            test_start_time = tmp.toInstant().toEpochMilli();
        } catch (ParseException e) {
            fileReport.addHardProblem("Test date malformation");
        }

        // Annoying DST!!!
        if (Util.isThisDayOnDstShift(tz, tmp))
            fileReport.addSoftProblem(5, "DST shifts today");

        // 8th Line is column header line
        String eiL = reader.readLine();
        String[] columnNames = eiL.split(",");
        int columnCount = columnNames.length;

        // More integrity checking
        if (!columnNames[0].toLowerCase().equals("clockdatetime")) {
            fileReport.addHardProblem("Wrong first column!");
            reader.close();
            return fileReport;
        }

        long totalLines = 0;
        String aLine;
        long previous_line_timestamp = test_start_time;
        while ((aLine = reader.readLine()) != null) {
            String[] values = aLine.split(",");
            totalLines++;

            // Check this line generally
            if (columnCount != values.length) {
                fileReport.addSoftProblem(totalLines + 8, String.format(
                        "expect %d found %d", columnCount, values.length));
                continue;
            }

            // Check this line date time
            // Measurement time should be later than test start time
            long measurement_epoch_time = Util.serialTimeToLongDate(values[0], null);
            if (measurement_epoch_time < test_start_time) {
                // Recalibrate time when DST shifts
                if (totalLines == 1) {
                    previous_line_timestamp = measurement_epoch_time;
                }
                fileReport.addSoftProblem(totalLines + 8, "Measurement time earlier than test start time");
            }

            // Possible time overlap?
            if (previous_line_timestamp > measurement_epoch_time) {
                fileReport.addSoftProblem(totalLines + 8, "Possible overlap occurred");
            }
            previous_line_timestamp = measurement_epoch_time;

            // Try parse
            List<String> parseProblemId = new LinkedList<>();
            for (int i = 1; i < values.length; i++) {
                try {
                    Double.valueOf(values[i]);
                } catch (NumberFormatException e) {
                    parseProblemId.add(String.valueOf(i));
                }
            }
            if (parseProblemId.size() != 0) {
                fileReport.addSoftProblem(totalLines + 8, String.format(
                        "Failed to parse number #'%s'", String.join(", ", parseProblemId)));
            }

            // And more?

        }

        reader.close();

        return fileReport;
    }

}