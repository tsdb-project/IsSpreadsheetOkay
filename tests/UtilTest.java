import edu.pitt.medschool.Util;

import java.io.File;
import java.util.List;

public class UtilTest {

    public static void main(String... args) {
        List<File> tt = Util.getAllSubDirectories(new File("D:\\je_test_data"));
        String[] t = Util.getAllSpecificFileInDirectory("D:\\je_test_data", "csv");
    }

}
