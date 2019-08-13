package com.son.CapstoneProject.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstantValue {

    // GG cloud storage properties
    public static final String GOOGLE_CREDENTIALS_URL = "src\\main\\resources\\Test-Project-1-d0294f6a791f.json";

    // Production
//    public static final String GOOGLE_CREDENTIALS_URL = "C:\\Users\\sonnpmse04810\\Desktop\\Test-Project-1-d0294f6a791f.json";
    public static final String GOOGLE_PROJECT_ID = "test-project-1-234610";
    // https://storage.cloud.google.com/[BUCKET_NAME]/[OBJECT_NAME]
    // To view on browsers
    public static final String GOOGLE_ACCESS_FILE_PREFIX_URL = "https://storage.cloud.google.com";

    public static final String FILE_WORD_BUCKET = "sonson_word_file";
    public static final String FILE_PDF_BUCKET = "sonson_pdf_file";
    public static final String FILE_IMAGE_BUCKET = "sonson_image_file";
    public static final String UNKNOWN_FILE_BUCKET = "unknown";

    // Login role
    public enum Role {
        ADMIN("ADMIN"),
        USER("USER"),
        ANONYMOUS("ANONYMOUS");

        private String value;

        Role(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static List<String> listAdmin = new ArrayList<>(Arrays.asList(
            "sonnpmse04810@fpt.edu.vn",
            "sonnpm197@gmail.com",
            "hienndse05142@fpt.edu.vn"
    ));

    // Pagination
    public static final int QUESTIONS_PER_PAGE = 10;
    public static final int ARTICLES_PER_PAGE = 10;
    public static final int TAGS_PER_PAGE = 10;
    public static final int USERS_PER_PAGE = 10;
    public static final int REPORTS_PER_PAGE = 10;
    public static final int NOTIFICATION_PER_PAGE = 10;

    public static final int HOME_PAGE_SEARCH_QUESTIONS_PER_PAGE = 5;
    public static final int HOME_PAGE_SEARCH_ARTICLES_PER_PAGE = 5;

    public static final int RECOMMENDED_QUESTIONS_PER_PAGE = 10;
    public static final int RECOMMENDED_ARTICLES_PER_PAGE = 10;

    // Vote point
    public static final int ARTICLE_UPVOTE_POINT = 1;
    public static final int ARTICLE_DOWNVOTE_POINT = -1;

    public static final int QUESTION_UPVOTE_POINT = 1;
    public static final int QUESTION_DOWNVOTE_POINT = -1;

    public static final int ANSWER_UPVOTE_POINT = 1;
    public static final int ANSWER_DOWNVOTE_POINT = -1;

    public static final int COMMENT_UPVOTE_POINT = 1;
    public static final int COMMENT_DOWNVOTE_POINT = -1;

    public static final int EDITED_APPROVE_POINT = 1;

    public static final int MARK_ACCEPTED_ANSWER_POINT = 1;

    // Count view
    public static final int INCREASE_COUNT_PER_MINUTES = 15;

    public static final int INCREASE_COUNT_PER_MINUTES_FOR_TEST = 1;

    public static final int MAXIMUM_TIME_SAVED_FOR_IP_IN_HOUR = 24;

    public static final int VIEW_COUNT = 1;

    // Ranking
    public static final int RANKING_VIEW_COUNT_GAP = 10;

    // Constant values
    public static final String QUESTION = "question";
    public static final String ARTICLE = "article";
    public static final String ANSWER = "answer";
    public static final String TAG = "tag";
    public static final String COMMENT = "comment";
    public static final String SYSTEM_CHART_BY_DATE = "date";
    public static final String SYSTEM_CHART_BY_MONTH = "month";
    public static final String SYSTEM_CHART_BY_YEAR = "year";

    // Sort by values
    public static final String SORT_VIEW_COUNT = "viewCount";
    public static final String SORT_UPVOTE_COUNT = "upvoteCount";
    public static final String SORT_DATE = "date";
}
