import edu.pitt.medschool.Util;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class UtilTest {

    public static void main(String... args) {
        List<File> tt = Util.getAllSubDirectories(new File("D:\\je_test_data"));
        String[] t = Util.getAllSpecificFileInDirectory("D:\\je_test_data", "csv");
        System.out.println(String.format("%s", 1L));

        TimeZone tz = TimeZone.getTimeZone("America/New_York");

        // https://www.timeanddate.com/time/change/usa/pittsburgh
        assert Util.isThisDayOnDstShift(tz, new Date(2010 - 1900, 2, 14, 11, 10));
        assert Util.isThisDayOnDstShift(tz, new Date(2018 - 1900, 10, 4, 3, 5));
        assert Util.isThisDayOnDstShift(tz, new Date(2013 - 1900, 10, 3, 2, 5));
        assert Util.isThisDayOnDstShift(tz, new Date(2011 - 1900, 10, 6, 0, 55));
        assert !Util.isThisDayOnDstShift(tz, new Date(2026 - 1900, 0, 1, 0, 10));
    }

}
