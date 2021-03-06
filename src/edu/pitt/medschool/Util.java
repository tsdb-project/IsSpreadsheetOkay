package edu.pitt.medschool;

import org.apache.poi.ss.usermodel.DateUtil;

import javax.swing.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class Util {

    /**
     * Functions as func name, ignore 2am-3am
     */
    public static boolean isThisDayOnDstShift(TimeZone tz, Date now) {
        Calendar c = Calendar.getInstance(tz);
        c.setTime(now);
        c.set(Calendar.HOUR_OF_DAY, 12); // We want to ignore 2am-3am problems
        Date normalized_now = c.getTime();
        c.add(Calendar.DATE, -1);
        Date dayBefore = c.getTime();
        return tz.inDaylightTime(dayBefore) != tz.inDaylightTime(normalized_now);
    }

    public static Date dateTimeFormatToDate(String dateTime, String format, TimeZone timeZone) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(timeZone);
        return sdf.parse(dateTime);
    }

    public static Instant dateTimeFormatToInstant(String dateTime, String format, TimeZone timeZone) throws ParseException {
        return dateTimeFormatToDate(dateTime, format, timeZone).toInstant();
    }

    public static long dateTimeFormatToTimestamp(String dateTime, String format, TimeZone timeZone) throws ParseException {
        return dateTimeFormatToInstant(dateTime, format, timeZone).toEpochMilli();
    }

    /**
     * Convert serial# time to a specific timestamp
     *
     * @param serial   String Serial number
     * @param timeZone Null for NY(PGH) timezone
     * @return Apache POI defined timestamp
     */
    public static long serialTimeToLongDate(String serial, TimeZone timeZone) {
        if (timeZone == null)
            timeZone = TimeZone.getTimeZone("UTC");
        double sTime = Double.valueOf(serial);
        Date d = DateUtil.getJavaDate(sTime, timeZone);
        return d.getTime();
    }

    private final static FileFilter directoryFilter = File::isDirectory;

    public static List<File> getAllSubDirectories(File file) {
        File[] tmp = file.listFiles(directoryFilter);
        if (tmp == null) return new ArrayList<>(0);

        List<File> subdirs = new LinkedList<>(Arrays.asList(tmp)),
                nextSubDirs = new LinkedList<>();

        subdirs.forEach((sub) -> {
            nextSubDirs.addAll(getAllSubDirectories(sub));
        });
        subdirs.addAll(nextSubDirs);

        return subdirs;
    }

    /**
     * Get all specific files under a directory
     *
     * @param dir  String directory path
     * @param type String file extension
     * @return String Full file path
     */
    public static String[] getAllSpecificFileInDirectory(String dir, String type) {
        File rootFolder = new File(dir);

        // If it's not a rootFolder but a file
        if (rootFolder.isFile()) {
            if (!rootFolder.getName().startsWith(".") && dir.toLowerCase().endsWith("." + type))
                return new String[]{dir};
            else
                return new String[0];
        }

        FilenameFilter extensionFilter = (dirs, name) -> {
            // Filter hidden or not wanted file
            return !name.startsWith(".") && name.toLowerCase().endsWith("." + type);
        };

        List<File> allDirs = Util.getAllSubDirectories(rootFolder);
        allDirs.add(rootFolder); // Don't forget the root dir

        List<String> final_res = new LinkedList<>();
        allDirs.forEach(file -> {
            File[] targets = file.listFiles(extensionFilter);
            if (targets == null) return;
            for (File f : targets) {
                // Folder named like '123.csv' is not good
                if (f.isDirectory()) continue;
                final_res.add(f.getAbsolutePath());
            }
        });

        return final_res.toArray(new String[0]);
    }

    public static void showMsgbox(String text, String title, int type) {
        JOptionPane.showMessageDialog(null, text, title, type);
    }


}
