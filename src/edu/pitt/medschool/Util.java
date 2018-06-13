package edu.pitt.medschool;

import org.apache.poi.ss.usermodel.DateUtil;

import java.io.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;

public class Util {

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

        FilenameFilter fileFilter = (dirs, name) -> {
            // Filter hidden or not wanted file
            return !name.startsWith(".") && name.toLowerCase().endsWith("." + type);
        };
        File[] files = folder.listFiles(fileFilter);

        assert files != null;
        if (files.length == 0)
            return new String[0];

        LinkedList<String> file_list = new LinkedList<>();
        for (File file : files) {
            if (file.isFile())
                file_list.add(file.getAbsolutePath());
        }

        return file_list.toArray(new String[0]);
    }


}
