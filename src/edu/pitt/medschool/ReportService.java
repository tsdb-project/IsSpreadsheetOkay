package edu.pitt.medschool;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

/**
 * Service for writing reports
 */
public class ReportService {

    public static class Report {
        private String filepath, filename;
        private long filesize;

        private LinkedList<String> hardProblem = new LinkedList<>();
        private LinkedList<String> softProblem = new LinkedList<>();

        public Report(String fp, String fn, long fs) {
            this.filepath = fp;
            this.filename = fn;
            this.filesize = fs;
        }

        public boolean isAllGood() {
            return (hardProblem.size() + softProblem.size()) == 0;
        }

        public void addHardProblem(String s) {
            hardProblem.add(s);
        }

        public void addSoftProblem(String s) {
            softProblem.add(s);
        }

    }

    private BufferedWriter bw;

    public ReportService(String path) throws IOException {
        bw = new BufferedWriter(
                (new OutputStreamWriter(
                        new FileOutputStream(path), StandardCharsets.UTF_8)));
    }

    public boolean WriteOut(Report data) {
        if (data.isAllGood()) return true;
        try {
            bw.write("Test");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void closeService() throws IOException {
        bw.newLine();
        bw.newLine();
        bw.write("Closing checker....");
        bw.flush();
        bw.close();
    }

}
