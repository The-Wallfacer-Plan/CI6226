package models.utility;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

public class Config {
    public static final String homeDir = System.getProperty("user.home");
    public static final String tempDir = System.getProperty("java.io.tmpdir");
    public static final String rootDir = homeDir + File.separator + "Dropbox/PHDCourses/IR/assignment";
    public static final String xmlFile = rootDir + File.separator + "dblp.xml";
    public static final String VALIDATION = "http://xml.org/sax/features/validation";
    public static final String indexFolder = tempDir + File.separator + "index";
    public static final String splitString = "; ";

    public static final String I_PAPER_ID = "paperId";
    public static final String I_TITLE = "title";
    public static final String I_KIND = "kind";
    public static final String I_AUTHORS = "authors";
    public static final String I_VENUE = "venue";
    public static final String I_PUB_YEAR = "pubYear";
    public static final List<String> defaultFields =
            Lists.newArrayList(I_PAPER_ID, I_TITLE, I_KIND, I_AUTHORS, I_VENUE, I_PUB_YEAR);

    //    interface Configurable
    public static final int topN = 10;
}
