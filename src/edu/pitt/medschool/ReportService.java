package edu.pitt.medschool;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for writing reports
 */
public class ReportService {

    private AtomicInteger counter = new AtomicInteger(0);
    private static String writeOutDataTemplate = "%d,\"%s\",\"%s\",%d,\"%s\",\"%s\",%s,\"%s\"";

    public static class Report {
        private String filepath, filename, eegUUID;
        private long filesize;

        private LinkedList<String> hardProblem = new LinkedList<>();
        private LinkedList<String> softProblemDetails = new LinkedList<>();
        private LinkedList<Long> softProblemCorrespondingLine = new LinkedList<>();

        public Report(String fp, String fn, long fs) {
            this.filepath = fp;
            this.filename = fn;
            this.filesize = fs;
        }

        public void setEegUUID(String eegUUID) {
            this.eegUUID = eegUUID;
        }

        public int getSoftProblemsCount() {
            return softProblemDetails.size();
        }

        public boolean isAllGood() {
            if (softProblemCorrespondingLine.size() != softProblemDetails.size())
                return false;
            return (hardProblem.size() + softProblemDetails.size()) == 0;
        }

        public void addHardProblem(String s) {
            hardProblem.add(s);
        }

        public void addSoftProblem(long line, String s) {
            softProblemCorrespondingLine.add(line);
            softProblemDetails.add(s);
        }

    }

    private BufferedWriter bw;

    private void initHeaders() throws IOException {
        bw.write("id,File Name,File Path,File Size,EEG UUID,Problem Type,Line Number,Comments");
        bw.newLine();
        bw.flush();
    }

    public ReportService(String path) throws IOException {
        bw = new BufferedWriter(
                (new OutputStreamWriter(
                        new FileOutputStream(path), StandardCharsets.UTF_8)));
        initHeaders();
    }

    public void WriteOut(Report data) {
        if (data.isAllGood()) return;
        try {
            for (String hard : data.hardProblem) {
                bw.write(String.format(writeOutDataTemplate,
                        counter.getAndIncrement(),
                        data.filename, data.filepath, data.filesize, data.eegUUID,
                        "Critical", "", hard));
                bw.newLine();
            }
            for (int i = 0; i < data.getSoftProblemsCount(); i++) {
                bw.write(String.format(writeOutDataTemplate,
                        counter.getAndIncrement(),
                        data.filename, data.filepath, data.filesize, data.eegUUID,
                        "Warning", data.softProblemCorrespondingLine.get(i), data.softProblemDetails.get(i)));
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            System.err.println("Error when writing logs for file: " + data.filepath);
        }
    }

    public void closeService() throws IOException {
        bw.newLine();
        bw.newLine();
        bw.write("Closing checker....");
        bw.flush();
        bw.close();
    }

}
