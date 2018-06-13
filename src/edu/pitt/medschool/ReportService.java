package edu.pitt.medschool;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Service for writing reports
 */
public class ReportService {

    private BufferedWriter bw;

    public ReportService(String path) throws IOException {
        bw = new BufferedWriter(
                (new OutputStreamWriter(
                        new FileOutputStream(path), StandardCharsets.UTF_8)));
    }

    public boolean writeline(String data) {
        try {
            bw.write(data);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void closeService() throws IOException {
        bw.newLine();
        bw.newLine();
        bw.write("Closing service....");
        bw.flush();
        bw.close();
    }

}
