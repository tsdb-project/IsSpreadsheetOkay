package edu.pitt.medschool;

import org.apache.poi.ss.usermodel.DateUtil;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class Util {

    public static Date dateTimeFormatToDate(String dateTime, String format, TimeZone timeZone) throws ParseException {
        if (timeZone == null)
            timeZone = TimeZone.getTimeZone("America/New_York");
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

    /**
     * Get all specific files under a directory
     *
     * @param dir  String directory path
     * @param type String file extension
     * @return String Full file path
     */
    public static String[] getAllSpecificFileInDirectory(String dir, String type) {
        File folder = new File(dir);
        if (folder.isFile()) {
            if (dir.toLowerCase().endsWith("." + type))
                return new String[]{dir};
            else
                return new String[0];
        }

        FilenameFilter extensionFilter = (dirs, name) -> {
            // Filter hidden or not wanted file
            return !name.startsWith(".") && name.toLowerCase().endsWith("." + type) && !dirs.isDirectory();
        };

        LinkedList<File> files = new LinkedList<>();
        File[] mainFileList = folder.listFiles();
        if (mainFileList == null) return new String[0];

        File[] toCheck;
        toCheck = folder.listFiles(extensionFilter);
        if (toCheck == null) return new String[0];

        Collections.addAll(files, toCheck);

        for (File aFile : mainFileList) {
            if (aFile.isDirectory()) {
                toCheck = aFile.listFiles(extensionFilter);
                if (toCheck == null) continue;
                Collections.addAll(files, toCheck);
            }
        }

        List<String> final_res = new ArrayList<>(files.size());
        files.forEach((file -> {
            if (file.isFile()) {
                final_res.add(file.getAbsolutePath());
            }
        }));

        return final_res.toArray(new String[0]);
    }


}
