package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.controller.FileController;
import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.adminChart.SystemChartInfo;
import com.son.CapstoneProject.entity.adminChart.SystemChartParams;
import com.son.CapstoneProject.entity.adminChart.UserChartInfo;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.pagination.ReportPagination;
import com.son.CapstoneProject.entity.pagination.TagPagination;
import com.son.CapstoneProject.entity.search.TagSearch;
import com.son.CapstoneProject.repository.*;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import com.son.CapstoneProject.repository.searchRepository.HibernateSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.son.CapstoneProject.common.ConstantValue.*;
import static com.son.CapstoneProject.common.ConstantValue.Role.USER;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private HibernateSearchRepository hibernateSearchRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private FileController fileController;

    @Autowired
    private ControllerUtils controllerUtils;

    @Autowired
    private EditedQuestionRepository editedQuestionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an administrator";
    }

//    @GetMapping("/viewAllReports")
//    public List<Report> viewAllReports() {
//        return reportRepository.findAll();
//    }

    @GetMapping("/viewNumberOfReportPages")
    @Transactional
    public long viewNumberOfReportPages() {
        try {
            long numberOfReport = reportRepository.count();
            if (numberOfReport % REPORTS_PER_PAGE == 0) {
                logger.info("numberOfReportPages : {}", numberOfReport / REPORTS_PER_PAGE);
                return numberOfReport / REPORTS_PER_PAGE;
            } else {
                logger.info("numberOfReportPages : {}", (numberOfReport / REPORTS_PER_PAGE) + 1);
                return (numberOfReport / REPORTS_PER_PAGE) + 1;
            }
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewReportsByPageIndex/{pageNumber}")
    @Transactional
    public ReportPagination viewReportsByPageIndex(@PathVariable int pageNumber) {
        try {
            logger.info("pageNumber: {}", pageNumber);
            PageRequest pageNumWithElements = PageRequest.of(pageNumber, REPORTS_PER_PAGE, Sort.by("utilTimestamp"));
            Page<Report> reportPage = reportRepository.findAll(pageNumWithElements);

            // Return pagination objects
            ReportPagination reportPagination = new ReportPagination();
            reportPagination.setReportsByPageIndex(reportPage.getContent());
            reportPagination.setNumberOfPages(Integer.parseInt("" + viewNumberOfReportPages()));
            reportPagination.setNumberOfContents(Integer.parseInt("" + reportRepository.count()));
            return reportPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewOneReport/{reportId}")
    @Transactional
    public Report viewOneReport(@PathVariable Long reportId) {
        try {
            String methodName = "AdminController.viewOneReport";
            return reportRepository.findById(reportId)
                    .orElseThrow(() -> new Exception(methodName + ": cannot find any report with id: " + reportId));
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @DeleteMapping("/deleteReport/{reportId}")
    @Transactional
    public Map<String, String> deleteReport(@PathVariable Long reportId) {
        try {
            String methodName = "AdminController.deleteReport";
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new Exception(methodName + ": cannot find any report with id: " + reportId));
            reportRepository.delete(report);
            Map<String, String> map = new HashMap<>();
            map.put("reportId", "" + reportId);
            map.put("deleted", "true");
            return map;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/searchTagsByPageIndex/{type}/{pageNumber}")
    @Transactional
    public TagPagination searchTagsByPageIndex(@RequestBody TagSearch tagSearch,
                                               @PathVariable String type,
                                               @PathVariable int pageNumber) {
        try {
            // Admin search tags first
            TagPagination tagPagination = (TagPagination) hibernateSearchRepository.search3(
                    tagSearch.getTextSearch(),
                    TAG,
                    new String[]{"name"}, // search tag by name
                    null,
                    type,
                    pageNumber,
                    false
            );

            return tagPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/searchTopUsersByTag/{tagId}")
    @Transactional
    public List<AppUserTag> searchTopUsersByTag(@PathVariable long tagId) {
        try {
            String methodName = "adminController.searchTopUsersByTag";

            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new Exception(methodName + ": Not found any tags with id: " + tagId));

            List<AppUserTag> finalAppUserTags = new ArrayList<>();

            List<AppUserTag> appUserTagsByTagId = appUserTagRepository.findByTag_TagId(tag.getTagId());

            // Do not count anonymous user
            for (AppUserTag appUserTag : appUserTagsByTagId) {
                if (USER.getValue().equalsIgnoreCase(appUserTag.getAppUser().getRole())) {
                    finalAppUserTags.add(appUserTag);
                }
            }

//        appUserTag1 and appUserTag2 are the objects to be compared. This method returns zero if
//        the objects are equal. It returns a positive value if appUserTag1 is greater than appUserTag2. Otherwise, a negative value is returned.
            Collections.sort(finalAppUserTags, new Comparator<AppUserTag>() {
                @Override
                public int compare(AppUserTag appUserTag1, AppUserTag appUserTag2) {
                    // If 2 users have the same reputation and view count gap are <= ranking gap => equal rank
                    if (appUserTag1.getReputation() == appUserTag2.getReputation()
                            && Math.abs(appUserTag1.getViewCount() - appUserTag2.getViewCount()) <= RANKING_VIEW_COUNT_GAP) {
                        return 0;
                    }
                    // If 2 users have the same reputation or repu of 1 > repu of 2 and view count appUserTag1 - appUserTag2 greater than gap
                    // If appUserTag1 < appUserTag2 repu but appUserTag1 still has more view => user1 > user2
                    else if (appUserTag1.getReputation() >= appUserTag2.getReputation() // focus on viewCount
                            && appUserTag1.getViewCount() - appUserTag2.getViewCount() > RANKING_VIEW_COUNT_GAP
                            ||
                            appUserTag1.getReputation() < appUserTag2.getReputation()
                                    && appUserTag1.getViewCount() - appUserTag2.getViewCount() > RANKING_VIEW_COUNT_GAP
                            ||
                            appUserTag1.getReputation() > appUserTag2.getReputation() // focus on reputation
                                    && Math.abs(appUserTag1.getViewCount() - appUserTag2.getViewCount()) <= RANKING_VIEW_COUNT_GAP
                    ) {
                        return -1; // appUserTag1 > appUserTag2
                    } else if (appUserTag1.getReputation() < appUserTag2.getReputation()
                            && appUserTag2.getViewCount() - appUserTag1.getViewCount() > RANKING_VIEW_COUNT_GAP
                    ) {
                        return 1; // appUserTag1 < appUserTag2
                    }

                    return 0;
                }
            });

            return finalAppUserTags;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * ADMIN view the number of view in website
     * Total of article + question
     *
     * @return
     */
    @GetMapping("/totalWebSiteViewCount")
    @Transactional
    public int totalView() {
        try {
            Integer articleTotalView = articleRepository.getTotalViewCount();
            Integer questionTotalView = questionRepository.getTotalViewCount();
            int articleViewValue = 0;
            int questionViewValue = 0;
            if (articleTotalView != null) {
                articleViewValue = articleTotalView;
            }

            if (questionTotalView != null) {
                questionViewValue = questionTotalView;
            }

            return articleViewValue + questionViewValue;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    private void selectDistinctDateFromList(List<Date> dateList, List<String> distinctDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Date date : dateList) {
            if (date == null) {
                continue;
            }

            String dateByText = simpleDateFormat.format(date);
            if (!distinctDate.contains(dateByText)) {
                distinctDate.add(dateByText);
            }
        }
    }

    @GetMapping("/userChartInfo/{userId}")
    @Transactional
    public List<UserChartInfo> detailUserActivitiesByDays(@PathVariable Long userId) {
        try {
            AppUser appUser = appUserRepository.findById(userId)
                    .orElseThrow(() -> new Exception("Cannot find any users with this id: " + userId));

//            List<Question> questionsByUserId = questionRepository.findByAppUser_UserId(userId);
//            List<Answer> answersByUserId = answerRepository.findByAppUser_UserId(userId);
//            List<Comment> commentsByUserId = commentRepository.findByAppUser_UserId(userId);

            // Optimise performance
            List<Date> questionDateByAppUser = questionRepository.findUtilTimeStampByAppUser(appUser);
            List<Date> answerDateByAppUser = answerRepository.findUtilTimeStampByAppUser(appUser);
            List<Date> commentDateByAppUser = commentRepository.findUtilTimeStampByAppUser(appUser);

            // Get distinct list date (from 00:00 - 24.00)
            List<String> distinctDate = new ArrayList<>();

            // Select distinct dates by format
            selectDistinctDateFromList(questionDateByAppUser, distinctDate);
            selectDistinctDateFromList(answerDateByAppUser, distinctDate);
            selectDistinctDateFromList(commentDateByAppUser, distinctDate);

            // Sort list distinct date
            Collections.sort(distinctDate, new Comparator<String>() {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                @Override
                public int compare(String o1, String o2) {
                    try {
                        return simpleDateFormat.parse(o1).compareTo(simpleDateFormat.parse(o2));
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });

            List<UserChartInfo> userChartInfoByDate = new ArrayList<>();
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            // After get distinct date then select to get the list data for each day
            for (String date : distinctDate) {
                try {
                    Date startDate = simpleDateFormat1.parse(date + " 00:00");
                    Date endDate = simpleDateFormat1.parse(date + " 23:59");

                    List<Question> questions = questionRepository.findByAppUserAndUtilTimestampBetween(appUser, startDate, endDate);
                    List<Answer> answers = answerRepository.findByAppUserAndUtilTimestampBetween(appUser, startDate, endDate);
                    List<Comment> comments = commentRepository.findByAppUserAndUtilTimestampBetween(appUser, startDate, endDate);

                    UserChartInfo userChartInfo = new UserChartInfo();

                    // Set number of questions
                    userChartInfo.setNumberOfQuestion(questions.size());
                    for (Question question : questions) {
                        // Count total reputation of questions
                        if (question.getUpvotedUserIds() != null && question.getUpvotedUserIds().size() > 0) {
                            userChartInfo.setTotalQuestionReputation(userChartInfo.getTotalQuestionReputation() + question.getUpvotedUserIds().size());
                        }

                        // Set total view count of question
                        userChartInfo.setTotalQuestionViewCount(userChartInfo.getTotalQuestionViewCount() + question.getViewCount());
                    }

                    // Set number of answers
                    userChartInfo.setNumberOfAnswer(answers.size());
                    for (Answer answer : answers) {
                        // Count total reputation of questions
                        if (answer.getUpvotedUserIds() != null && answer.getUpvotedUserIds().size() > 0) {
                            userChartInfo.setTotalAnswerReputation(userChartInfo.getTotalAnswerReputation() + answer.getUpvotedUserIds().size());
                        }
                    }

                    // Set number of comments
                    userChartInfo.setNumberOfComment(comments.size());
                    for (Comment comment : comments) {
                        // Count total reputation of questions
                        if (comment.getUpvotedUserIds() != null && comment.getUpvotedUserIds().size() > 0) {
                            userChartInfo.setTotalCommentReputation(userChartInfo.getTotalCommentReputation() + comment.getUpvotedUserIds().size());
                        }
                    }

                    userChartInfo.setDate(date);
                    // Add to list
                    userChartInfoByDate.add(userChartInfo);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            return userChartInfoByDate;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/systemChartInfo/date")
    @Transactional
    public List<SystemChartInfo> systemChartInfoByDate(@RequestBody SystemChartParams systemChartParams) {
        try {
            List<SystemChartInfo> listSystemChartInfo = new ArrayList<>();

            SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdf_yyyyMMdd.parse(systemChartParams.getStartTime());

            for (int i = 0; i <= systemChartParams.getPeriod(); i++) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                calendar.add(Calendar.DATE, i);  // number of days to add
                Date dateNeedToFind = sdf_yyyyMMdd.parse(sdf_yyyyMMdd.format(calendar.getTime()));  // dt is now the new date

                // Then search data within this date (00:00 -> 23:59)
                SimpleDateFormat sdf_yyyyMMdd_HHmm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date startTime = sdf_yyyyMMdd_HHmm.parse(sdf_yyyyMMdd.format(dateNeedToFind) + " 00:00");
                Date endTime = sdf_yyyyMMdd_HHmm.parse(sdf_yyyyMMdd.format(dateNeedToFind) + " 23:59");

                // Then create an object of that date
                SystemChartInfo systemChartInfo = new SystemChartInfo();
                systemChartInfo.setChartByDate(sdf_yyyyMMdd.format(dateNeedToFind));

                //================================== Adding total viewCount ======================================//

                // Then adding info in this date
                Integer questionTotalView = questionRepository.findTotalViewOfQuestionsByUtilTimestampBetween(startTime, endTime);
                Integer articleTotalView = articleRepository.findTotalViewOfArticlesByUtilTimestampBetween(startTime, endTime);

                int questionTotalViewInteger = 0;
                int articleTotalViewInteger = 0;

                if (questionTotalView != null) {
                    questionTotalViewInteger = questionTotalView;
                }

                if (articleTotalView != null) {
                    articleTotalViewInteger = articleTotalView;
                }

                // Then add total view count
                systemChartInfo.setTotalViewCount(questionTotalViewInteger + articleTotalViewInteger);

                //================================== Adding total upvote ======================================//

                // Then adding info in this date
                Integer articleTotalUpvote = articleRepository.findTotalUpvoteOfArticlesByUtilTimestampBetween(startTime, endTime);
                Integer questionTotalUpvote = questionRepository.findTotalUpvoteOfQuestionsByUtilTimestampBetween(startTime, endTime);
                Integer answerTotalUpvote = answerRepository.findTotalUpvoteOfAnswersByUtilTimestampBetween(startTime, endTime);
                Integer commentTotalUpvote = commentRepository.findTotalUpvoteOfCommentsByUtilTimestampBetween(startTime, endTime);

                // Then create an object of that date
                int articleTotalUpvoteInteger = 0;
                int questionTotalUpvoteInteger = 0;
                int answerTotalUpvoteInteger = 0;
                int commentTotalUpvoteInteger = 0;

                if (articleTotalUpvote != null) {
                    articleTotalUpvoteInteger = articleTotalUpvote;
                }

                if (questionTotalUpvote != null) {
                    questionTotalUpvoteInteger = questionTotalUpvote;
                }

                if (answerTotalUpvote != null) {
                    answerTotalUpvoteInteger = answerTotalUpvote;
                }

                if (commentTotalUpvote != null) {
                    commentTotalUpvoteInteger = commentTotalUpvote;
                }

                // Then add total upvote count
                systemChartInfo.setTotalUpvoteCount(articleTotalUpvoteInteger + questionTotalUpvoteInteger + answerTotalUpvoteInteger + commentTotalUpvoteInteger);

                //================================== Adding total new account ======================================//
                // new accounts = from previous week to startTime

                // Then adding info in this date
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(startTime);
                calendar2.add(Calendar.DATE, -7);
                Date lastWeekStartPoint = sdf_yyyyMMdd.parse(sdf_yyyyMMdd.format(calendar2.getTime()));

                // =================================================================================From (7 days ago) to (the time we're counting)
                Integer totalNewAccounts = appUserRepository.findTotalNewAccountsByUtilTimestampBetween(lastWeekStartPoint, startTime);

                int totalNewAccountsInteger = 0;

                if (totalNewAccounts != null) {
                    totalNewAccountsInteger = totalNewAccounts;
                }

                // Then add total view count
                systemChartInfo.setTotalNewAccount(totalNewAccountsInteger);

                //================================== Adding total inactive account (in one month) ======================================//
                // Then adding info in this date
                Calendar calendar3 = Calendar.getInstance();
                calendar3.setTime(startTime);
                calendar3.add(Calendar.DATE, -30);
                Date lastMonthStartPoint = sdf_yyyyMMdd.parse(sdf_yyyyMMdd.format(calendar3.getTime()));
                Integer totalInactiveAccounts = appUserRepository.findTotalInactiveAccountsByUtilTimestampBefore(lastMonthStartPoint);

                int totalInactiveAccountsInteger = 0;

                if (totalInactiveAccounts != null) {
                    totalInactiveAccountsInteger = totalInactiveAccounts;
                }

                // Then add total view count
                systemChartInfo.setTotalInactiveAccount(totalInactiveAccountsInteger);

                listSystemChartInfo.add(systemChartInfo);
            }

            return listSystemChartInfo;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/systemChartInfo/month")
    @Transactional
    public List<SystemChartInfo> systemChartInfoByMonth(@RequestBody SystemChartParams systemChartParams) {
        try {
            List<SystemChartInfo> listSystemChartInfo = new ArrayList<>();

            SimpleDateFormat sdf_yyyyMM = new SimpleDateFormat("yyyy-MM");

            // Start time does not contain date
            Date startDate = sdf_yyyyMM.parse(systemChartParams.getStartTime());

            for (int i = 0; i <= systemChartParams.getPeriod(); i++) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                calendar.add(Calendar.MONTH, i);  // number of days to add
                Date dateNeedToFind = sdf_yyyyMM.parse(sdf_yyyyMM.format(calendar.getTime()));  // dt is now the new date

                SystemChartInfo systemChartInfo = new SystemChartInfo();
                systemChartInfo.setChartByMonth(sdf_yyyyMM.format(dateNeedToFind));

                Calendar calendarDateNeedToFind = Calendar.getInstance();
                calendarDateNeedToFind.setTime(dateNeedToFind);

                //================================== Adding total viewCount ======================================//

                // Then adding info in this date
                Integer articleTotalView = articleRepository.findTotalViewOfArticlesByYearAndMonth(
                        calendarDateNeedToFind.get(Calendar.YEAR),
                        calendarDateNeedToFind.get(Calendar.MONTH) + 1);

                Integer questionTotalView = questionRepository.findTotalViewOfQuestionsByYearAndMonth(
                        calendarDateNeedToFind.get(Calendar.YEAR),
                        calendarDateNeedToFind.get(Calendar.MONTH) + 1);

                int questionTotalViewInteger = 0;
                int articleTotalViewInteger = 0;

                if (questionTotalView != null) {
                    questionTotalViewInteger = questionTotalView;
                }

                if (articleTotalView != null) {
                    articleTotalViewInteger = articleTotalView;
                }

                // Then add total view count
                systemChartInfo.setTotalViewCount(questionTotalViewInteger + articleTotalViewInteger);

                //================================== Adding total upvote ======================================//

                // Then adding info in this date
                Integer articleTotalUpvote = articleRepository.findTotalUpvoteOfArticlesByYearAndMonth(
                        calendarDateNeedToFind.get(Calendar.YEAR),
                        calendarDateNeedToFind.get(Calendar.MONTH) + 1);

                Integer questionTotalUpvote = questionRepository.findTotalUpvoteOfQuestionsByYearAndMonth(
                        calendarDateNeedToFind.get(Calendar.YEAR),
                        calendarDateNeedToFind.get(Calendar.MONTH) + 1);

                Integer answerTotalUpvote = answerRepository.findTotalUpvoteOfAnswersByYearAndMonth(
                        calendarDateNeedToFind.get(Calendar.YEAR),
                        calendarDateNeedToFind.get(Calendar.MONTH) + 1);

                Integer commentTotalUpvote = commentRepository.findTotalUpvoteOfCommentsByYearAndMonth(
                        calendarDateNeedToFind.get(Calendar.YEAR),
                        calendarDateNeedToFind.get(Calendar.MONTH) + 1);

                // Then create an object of that date
                int articleTotalUpvoteInteger = 0;
                int questionTotalUpvoteInteger = 0;
                int answerTotalUpvoteInteger = 0;
                int commentTotalUpvoteInteger = 0;

                if (articleTotalUpvote != null) {
                    articleTotalUpvoteInteger = articleTotalUpvote;
                }

                if (questionTotalUpvote != null) {
                    questionTotalUpvoteInteger = questionTotalUpvote;
                }

                if (answerTotalUpvote != null) {
                    answerTotalUpvoteInteger = answerTotalUpvote;
                }

                if (commentTotalUpvote != null) {
                    commentTotalUpvoteInteger = commentTotalUpvote;
                }

                // Then add total upvote count
                systemChartInfo.setTotalUpvoteCount(articleTotalUpvoteInteger + questionTotalUpvoteInteger + answerTotalUpvoteInteger + commentTotalUpvoteInteger);

                //================================== Adding total new account ======================================//
                // new accounts = from previous week to startTime

                // Then adding info in this date
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(dateNeedToFind);
                calendar2.add(Calendar.DATE, -7);
                Date lastWeekStartPoint = sdf_yyyyMM.parse(sdf_yyyyMM.format(calendar2.getTime()));

                // =================================================================================From (7 days ago) to (the time we're counting)
                Integer totalNewAccounts = appUserRepository.findTotalNewAccountsByUtilTimestampBetween(lastWeekStartPoint, dateNeedToFind);

                int totalNewAccountsInteger = 0;

                if (totalNewAccounts != null) {
                    totalNewAccountsInteger = totalNewAccounts;
                }

                // Then add total view count
                systemChartInfo.setTotalNewAccount(totalNewAccountsInteger);

                //================================== Adding total inactive account (in one month) ======================================//
                // Then adding info in this date
                Calendar calendar3 = Calendar.getInstance();
                calendar3.setTime(dateNeedToFind);
                calendar3.add(Calendar.MONTH, -1);
                Date lastMonthStartPoint = sdf_yyyyMM.parse(sdf_yyyyMM.format(calendar3.getTime()));
                Integer totalInactiveAccounts = appUserRepository.findTotalInactiveAccountsByUtilTimestampBefore(lastMonthStartPoint);

                int totalInactiveAccountsInteger = 0;

                if (totalInactiveAccounts != null) {
                    totalInactiveAccountsInteger = totalInactiveAccounts;
                }

                // Then add total view count
                systemChartInfo.setTotalInactiveAccount(totalInactiveAccountsInteger);

                listSystemChartInfo.add(systemChartInfo);
            }

            return listSystemChartInfo;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/systemChartInfo/year")
    @Transactional
    public List<SystemChartInfo> systemChartInfoByYear(@RequestBody SystemChartParams systemChartParams) {
        try {
            List<SystemChartInfo> listSystemChartInfo = new ArrayList<>();

            SimpleDateFormat sdf_yyyy = new SimpleDateFormat("yyyy");

            // Start time does not contain date
            Date startDate = sdf_yyyy.parse(systemChartParams.getStartTime());

            for (int i = 0; i <= systemChartParams.getPeriod(); i++) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                calendar.add(Calendar.YEAR, i);  // number of days to add
                Date dateNeedToFind = sdf_yyyy.parse(sdf_yyyy.format(calendar.getTime()));  // dt is now the new date

                SystemChartInfo systemChartInfo = new SystemChartInfo();
                systemChartInfo.setChartByYear(sdf_yyyy.format(dateNeedToFind));

                Calendar calendarDateNeedToFind = Calendar.getInstance();
                calendarDateNeedToFind.setTime(dateNeedToFind);

                //================================== Adding total viewCount ======================================//

                // Then adding info in this date
                Integer articleTotalView = articleRepository.findTotalViewOfArticlesByYear(calendarDateNeedToFind.get(Calendar.YEAR));

                Integer questionTotalView = questionRepository.findTotalViewOfQuestionsByYear(calendarDateNeedToFind.get(Calendar.YEAR));

                int questionTotalViewInteger = 0;
                int articleTotalViewInteger = 0;

                if (questionTotalView != null) {
                    questionTotalViewInteger = questionTotalView;
                }

                if (articleTotalView != null) {
                    articleTotalViewInteger = articleTotalView;
                }

                // Then add total view count
                systemChartInfo.setTotalViewCount(questionTotalViewInteger + articleTotalViewInteger);

                //================================== Adding total upvote ======================================//

                // Then adding info in this date
                Integer articleTotalUpvote = articleRepository.findTotalUpvoteOfArticlesByYear(calendarDateNeedToFind.get(Calendar.YEAR));

                Integer questionTotalUpvote = questionRepository.findTotalUpvoteOfQuestionsByYear(calendarDateNeedToFind.get(Calendar.YEAR));

                Integer answerTotalUpvote = answerRepository.findTotalUpvoteOfAnswersByYear(calendarDateNeedToFind.get(Calendar.YEAR));

                Integer commentTotalUpvote = commentRepository.findTotalUpvoteOfCommentsByYear(calendarDateNeedToFind.get(Calendar.YEAR));

                // Then create an object of that date
                int articleTotalUpvoteInteger = 0;
                int questionTotalUpvoteInteger = 0;
                int answerTotalUpvoteInteger = 0;
                int commentTotalUpvoteInteger = 0;

                if (articleTotalUpvote != null) {
                    articleTotalUpvoteInteger = articleTotalUpvote;
                }

                if (questionTotalUpvote != null) {
                    questionTotalUpvoteInteger = questionTotalUpvote;
                }

                if (answerTotalUpvote != null) {
                    answerTotalUpvoteInteger = answerTotalUpvote;
                }

                if (commentTotalUpvote != null) {
                    commentTotalUpvoteInteger = commentTotalUpvote;
                }

                // Then add total upvote count
                systemChartInfo.setTotalUpvoteCount(articleTotalUpvoteInteger + questionTotalUpvoteInteger + answerTotalUpvoteInteger + commentTotalUpvoteInteger);

                //================================== Adding total new account ======================================//
                // new accounts = from previous week to startTime

                // Then adding info in this date
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(dateNeedToFind);
                calendar2.add(Calendar.DATE, -7);
                Date lastWeekStartPoint = sdf_yyyy.parse(sdf_yyyy.format(calendar2.getTime()));

                // =================================================================================From (7 days ago) to (the time we're counting)
                Integer totalNewAccounts = appUserRepository.findTotalNewAccountsByUtilTimestampBetween(lastWeekStartPoint, dateNeedToFind);

                int totalNewAccountsInteger = 0;

                if (totalNewAccounts != null) {
                    totalNewAccountsInteger = totalNewAccounts;
                }

                // Then add total view count
                systemChartInfo.setTotalNewAccount(totalNewAccountsInteger);

                //================================== Adding total inactive account (in one month) ======================================//
                // Then adding info in this date
                Calendar calendar3 = Calendar.getInstance();
                calendar3.setTime(dateNeedToFind);
                calendar3.add(Calendar.MONTH, -1);
                Date lastMonthStartPoint = sdf_yyyy.parse(sdf_yyyy.format(calendar3.getTime()));
                Integer totalInactiveAccounts = appUserRepository.findTotalInactiveAccountsByUtilTimestampBefore(lastMonthStartPoint);

                int totalInactiveAccountsInteger = 0;

                if (totalInactiveAccounts != null) {
                    totalInactiveAccountsInteger = totalInactiveAccounts;
                }

                // Then add total view count
                systemChartInfo.setTotalInactiveAccount(totalInactiveAccountsInteger);

                listSystemChartInfo.add(systemChartInfo);
            }

            return listSystemChartInfo;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Delete a question
     *
     * @return
     */
    @DeleteMapping("/deleteQuestion/{questionId}")
    @Transactional
    public Map<String, String> deleteQuestion(/*@RequestBody AppUser appUser,*/
            @PathVariable Long questionId,
            HttpServletRequest request) {
        try {
            String methodName = "AdminController.deleteQuestion";

//            controllerUtils.validateAppUser(appUser, methodName, true);

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new Exception(methodName + ": Not found question with id: " + questionId));

            // Remove the edited versions first
            List<EditedQuestion> editedQuestions = question.getEditedQuestions();
            Iterator<EditedQuestion> editedQuestionIterator = editedQuestions.iterator();

            while (editedQuestionIterator.hasNext()) {
                EditedQuestion editedQuestion = editedQuestionIterator.next();
                editedQuestionRepository.delete(editedQuestion);
            }

            // Remove the comments
            List<Comment> comments = question.getComments();
            Iterator<Comment> commentIterator = comments.iterator();

            while (commentIterator.hasNext()) {
                Comment comment = commentIterator.next();
                commentRepository.delete(comment);
            }

            // Remove the answers
            List<Answer> answers = question.getAnswers();
            Iterator<Answer> answerIterator = answers.iterator();

            while (answerIterator.hasNext()) {
                Answer answer = answerIterator.next();

                // Remove the comment of the answer first
                List<Comment> commentsOfAnswer = commentRepository.findByAnswer_AnswerId(answer.getAnswerId());
                for (Comment comment : commentsOfAnswer) {
                    commentRepository.delete(comment);
                }

                // Then delete the answer
                answerRepository.delete(answer);
            }

            // Delete UploadedFile both from GG cloud and DB
            List<UploadedFile> uploadedFiles = question.getUploadedFiles();
            for (UploadedFile uploadedFile : uploadedFiles) {
                fileController.deleteFile(uploadedFile);
            }

            // Delete the reports of this question
            List<Report> reports = question.getReports();
            for (Report report : reports) {
                reportRepository.delete(report);
            }

            // Notify the author of the question
            AppUser authorOfThisQuestion = question.getAppUser();
            Notification notification = new Notification();
            notification.setUtilTimestamp(new Date());
//            notification.setFromAdmin(true);
            notification.setDeleteQuestion(true);
            notification.setAppUserReceiver(authorOfThisQuestion);
            notification.setMessage("Admin vừa xóa câu hỏi của bạn với tiêu đề: " + question.getTitle() + " do vi phạm nội quy diễn đàn.");
            notificationRepository.save(notification);

            List<Tag> tags = question.getTags();
            for (Tag tag : tags) {
                AppUserTag appUserTag = appUserTagRepository
                        .findAppUserTagByAppUser_UserIdAndTag_TagId(question.getAppUser().getUserId(), tag.getTagId());

                if (appUserTag != null) {
                    // Then reduce point of this user by this question upvote count
                    int currentPoint = appUserTag.getReputation();
                    int resultPoint = 0;
                    if (question.getUpvoteCount() == null) {
                        resultPoint = currentPoint - 0;
                    } else {
                        resultPoint = currentPoint - question.getUpvoteCount();
                    }

                    if (resultPoint < 0) {
                        resultPoint = 0;
                    }

                    appUserTag.setReputation(resultPoint);
                    appUserTagRepository.save(appUserTag);
                }
            }

            // Delete all notifications related to this question
            List<Notification> notifications = notificationRepository.findByQuestion_QuestionId(questionId);

            for (Notification noti: notifications) {
                notificationRepository.delete(noti);
            }

            // Then remove the question
            questionRepository.delete(question);

            // After deleting question check if this tag has related questions / articles or not
            for (Tag tag : tags) {
                controllerUtils.removeAppUserTagAndTagIfHasNoRelatedQuestionsOrArticle(tag.getTagId());
            }

            Map<String, String> map = new HashMap<>();
            map.put("questionId", "" + questionId);
            map.put("deleted", "true");
            return map;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Delete answer
     *
     * @param answerId
     * @return
     */
    @DeleteMapping("/deleteAnswerToQuestion/{answerId}")
    @Transactional
    public Map<String, String> deleteAnswerToQuestion(/*@RequestBody AppUser appUser,*/
            @PathVariable Long answerId,
            HttpServletRequest request) {
        try {
            String methodName = "Admin.deleteAnswerToQuestion";

//            controllerUtils.validateAppUser(appUser, methodName, false);
//
//            if (appUser.isAnonymous()) {
//                appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
//            } else {
//                controllerUtils.validateAppUser(appUser, methodName, true);
//            }

            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new Exception(methodName + ": Found no answer with id: " + answerId));

            // Delete comment in answer first
            List<Comment> comments = answer.getComments();
            Iterator<Comment> commentIterator = comments.iterator();

            while (commentIterator.hasNext()) {
                Comment comment = commentIterator.next();
                commentRepository.delete(comment);
            }

            Question question = answer.getQuestion();

            // Notify the author of the question
            AppUser authorOfThisQuestion = answer.getAppUser();
            Notification notification = new Notification();
            notification.setUtilTimestamp(new Date());
//            notification.setFromAdmin(true);
            notification.setDeleteAnswer(true);
            notification.setQuestion(question); // delete answer => link to question
            notification.setAppUserReceiver(authorOfThisQuestion);
            notification.setMessage("Admin vừa xóa câu trả lời của bạn: " + answer.getContent() + " do vi phạm nội quy diễn đàn.");
            notificationRepository.save(notification);

            // Then remove the answer
            answerRepository.delete(answer);

            List<Tag> tags = question.getTags();
            for (Tag tag : tags) {
                AppUserTag appUserTag = appUserTagRepository
                        .findAppUserTagByAppUser_UserIdAndTag_TagId(answer.getAppUser().getUserId(), tag.getTagId());

                if (appUserTag != null) {
                    // Then reduce point of this user by this question upvote count
                    int currentPoint = appUserTag.getReputation();
                    int resultPoint = 0;
                    if (answer.getUpvoteCount() == null) {
                        resultPoint = currentPoint;
                    } else {
                        resultPoint = currentPoint - answer.getUpvoteCount();
                    }

                    if (resultPoint < 0) {
                        resultPoint = 0;
                    }

                    appUserTag.setReputation(resultPoint);
                    appUserTagRepository.save(appUserTag);
                }
            }

            Map<String, String> map = new HashMap<>();
            map.put("answerId", "" + answerId);
            map.put("deleted", "true");
            return map;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Delete comment
     *
     * @param id
     * @return
     */
    @DeleteMapping("/deleteComment/{id}")
    @Transactional
    public Map<String, String> deleteComment(/*@RequestBody AppUser appUser,*/
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            String methodName = "AdminController.deleteComment";

            Comment comment = commentRepository.findById(id)
                    .orElseThrow(() -> new Exception(methodName + ": Found no answer with id: " + id));

//            controllerUtils.validateAppUser(appUser, methodName, false);
//
//            if (appUser.isAnonymous()) {
//                appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
//                comment.setAppUser(appUser);
//            } else {
//                controllerUtils.validateAppUser(appUser, methodName, true);
//            }

            Article article = comment.getArticle();

            // Notify the author of the question
            AppUser authorOfThisQuestion = comment.getAppUser();
            Notification notification = new Notification();
            notification.setUtilTimestamp(new Date());
//            notification.setFromAdmin(true);
            notification.setDeleteComment(true);
            if (article != null) {
                notification.setArticle(article); // delete comment => link to article
            }
            notification.setAppUserReceiver(authorOfThisQuestion);
            notification.setMessage("Admin vừa xóa bình luận của bạn: " + comment.getContent() + " do vi phạm nội quy diễn đàn.");
            notificationRepository.save(notification);

            commentRepository.delete(comment);

            Map<String, String> map = new HashMap<>();
            map.put("commentId", "" + id);
            map.put("deleted", "true");
            return map;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
