package edu.pitt.medschool;

public class CheckFunc {

    public static String firstLineInCSV(String fLine, String pid, ReportService.Report r) {
        if (!fLine.toUpperCase().contains(pid))
            r.addHardProblem("Wrong PID in filename!");
        if (fLine.length() < 50)
            r.addHardProblem("File UUID malformed!");
        return fLine.substring(fLine.length() - 40, fLine.length() - 4);
    }

    /**
     * Lots of info in Filename
     *
     * @return 0: PID; 1: ar/noar
     */
    public static String[] checkerFromFilename(String filename) {
        String[] res = new String[2];
        // PUH-20xx_xxx
        res[0] = filename.substring(0, 12).trim().toUpperCase();
        String fn_laterpart = filename.substring(12).toLowerCase();
        // Ar or NoAr
        if (fn_laterpart.contains("noar")) {
            res[1] = "noar";
        } else if (fn_laterpart.contains("ar")) {
            res[1] = "ar";
        }
        return res;
    }

}
