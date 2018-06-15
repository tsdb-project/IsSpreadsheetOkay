package edu.pitt.medschool;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class Validator {

    public final static SimpleDateFormat global_operation_sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

    private JButton startButton;
    private JPanel panel1;
    private JTextArea checkPath;

    public Validator() {
        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    String[] paths = checkPath.getText().trim().split("\n");
                    List<String> f = new ArrayList<>();
                    for (String aP : paths) {
                        f.addAll(Arrays.asList(Util.getAllSpecificFileInDirectory(aP, "csv")));
                    }
                    newCheckTask(f.toArray(new String[0]),
                            global_operation_sdf.format(new Date())
                                    .replace(":", "") + ".csv", 0.5, true);
                } catch (IOException e1) {
                    StringWriter sw = new StringWriter();
                    e1.printStackTrace(new PrintWriter(sw));
                    Util.showErrorMsgbox(sw.toString());
                }
            }
        });
    }

    public static void main(String[] args) throws IOException {
        global_operation_sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        if (args.length == 3 || args.length == 1) {
            System.out.println("You are using command line mode...");

            String path_tocheck = args[0].trim(),
                    report_file_path = global_operation_sdf.format(new Date())
                            .replace(":", "") + ".csv";
            double lf;

            if (args.length == 1) {
                lf = 0.5;
            } else {
                lf = Double.valueOf(args[0].trim());
                lf = lf > 1 ? 1 : lf;
                path_tocheck = args[1].trim();
                report_file_path = args[2].trim();
            }

            newCheckTask(Util.getAllSpecificFileInDirectory(path_tocheck, "csv"), report_file_path, lf, false);
        } else {
            JFrame frame = new JFrame("Validator");
            frame.setContentPane(new Validator().panel1);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

    }

    private static void newCheckTask(String[] targets, String report_name, double load_factor, boolean gui) throws IOException {
        if (targets.length == 0) {
            if (gui)
                Util.showErrorMsgbox("Nothing to check!");
            else
                System.err.println("Nothing to check!");
            return;
        }

        ReportService rs = new ReportService(report_name);
        FileChecker fc = new FileChecker(rs, load_factor, gui);

        fc.AddArrayFiles(targets);
        fc.startCheck();
    }

}
