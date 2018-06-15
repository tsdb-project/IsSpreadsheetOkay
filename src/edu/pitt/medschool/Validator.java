package edu.pitt.medschool;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Validator {

    public final static SimpleDateFormat global_operation_sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

    private static void printHelpText() {
        System.out.println("Usage: java -jar IsFileOkay.jar load_factor Path_to_check Report_file_path");
        System.out.println("  load_factor: 0-1, how many parallels");
        System.out.println("  Path_to_check: Check path, can be a folder or file");
        System.out.println("  Report_file_path: Report file name (not a folder name).");
        System.out.println("Or, you can use:");
        System.out.println("    Fast: java -jar IsFileOkay.jar Path_to_check");
    }

    public static void main(String[] args) throws IOException {
        global_operation_sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        if (args.length == 3 || args.length == 1) {
            String path_tocheck = args[0].trim(),
                    report_file_path = global_operation_sdf.format(new Date()) + ".csv";
            double lf;

            if (args.length == 1) {
                lf = 0.5;
            } else {
                lf = Double.valueOf(args[0].trim());
                lf = lf > 1 ? 1 : lf;
                path_tocheck = args[1].trim();
                report_file_path = args[2].trim();
            }

            String[] targets = Util.getAllSpecificFileInDirectory(path_tocheck, "csv");
            if (targets.length == 0) {
                System.err.println("Nothing to check!");
                return;
            }

            ReportService rs = new ReportService(report_file_path);
            FileChecker fc = new FileChecker(rs, lf);

            fc.AddArrayFiles(targets);
            fc.startCheck();
        } else {
            printHelpText();
        }

    }

}
