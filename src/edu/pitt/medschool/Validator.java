package edu.pitt.medschool;

import java.io.IOException;

public class Validator {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java -jar IsFileOkay.jar load_factor Path_to_check Report_file_path");
            System.out.println("  load_factor: 0-1, how many parallels");
            System.out.println("  Path_to_check: Check path, can be a folder or file");
            System.out.println("  Report_file_path: Report file name (not a folder name).");
            return;
        }

        double lf = Double.valueOf(args[0].trim());
        lf = lf > 1 ? 1 : lf;

        ReportService rs = new ReportService(args[2].trim());
        FileChecker fc = new FileChecker(rs, lf);

        String[] targets = Util.getAllSpecificFileInDirectory(args[1], "csv");

        if (targets.length == 0) {
            System.err.println("Nothing to check!");
            return;
        }

        fc.AddArrayFiles(targets);

        fc.startCheck();
    }

}
