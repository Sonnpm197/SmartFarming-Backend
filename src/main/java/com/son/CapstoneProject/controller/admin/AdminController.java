package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.controller.FileController;
import com.son.CapstoneProject.entity.*;
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

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an administrator";
    }

//    @GetMapping("/viewAllReports")
//    public List<Report> viewAllReports() {
//        return reportRepository.findAll();
//    }

    @GetMapping("/viewNumberOfReportPages")
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
    public ReportPagination viewReportsByPageIndex(@PathVariable int pageNumber) {
        try {
            logger.info("pageNumber: {}", pageNumber);
            PageRequest pageNumWithElements = PageRequest.of(pageNumber, REPORTS_PER_PAGE, Sort.by("utilTimestamp"));
            Page<Report> reportPage = reportRepository.findAll(pageNumWithElements);

            // Return pagination objects
            ReportPagination reportPagination = new ReportPagination();
            reportPagination.setReportsByPageIndex(reportPage.getContent());

            reportPagination.setNumberOfPages(Integer.parseInt("" + viewNumberOfReportPages()));
            return reportPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewOneReport/{reportId}")
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
    public TagPagination searchTagsByPageIndex(@RequestBody TagSearch tagSearch,
                                               @PathVariable String type,
                                               @PathVariable int pageNumber) {
        try {
            // Admin search tags first
            TagPagination tagPagination = (TagPagination) hibernateSearchRepository.search2(
                    tagSearch.getTextSearch(),
                    TAG,
                    new String[]{"name", "description"},
                    null,
                    type,
                    pageNumber
            );

            return tagPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/searchTopUsersByTag/{tagId}")
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

    @GetMapping("/userChartInfo/{userId}")
    public List<UserChartInfo> detailUserActivitiesByDays(@PathVariable Long userId) {
        try {
            AppUser appUser = appUserRepository.findById(userId)
                    .orElseThrow(() -> new Exception("Cannot find any users with this id: " + userId));

            List<Question> questionsByUserId = questionRepository.findByAppUser_UserId(userId);
            List<Answer> answersByUserId = answerRepository.findByAppUser_UserId(userId);
            List<Comment> commentsByUserId = commentRepository.findByAppUser_UserId(userId);

            // Get distinct list date (from 00:00 - 24.00)
            List<String> distinctDate = new ArrayList<>();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (Question question : questionsByUserId) {
                Date date = question.getUtilTimestamp();
                if (date == null) {
                    continue;
                }

                String dateByText = simpleDateFormat.format(date);
                if (!distinctDate.contains(dateByText)) {
                    distinctDate.add(dateByText);
                }
            }

            for (Answer answer : answersByUserId) {
                Date date = answer.getUtilTimestamp();
                if (date == null) {
                    continue;
                }

                String dateByText = simpleDateFormat.format(date);
                if (!distinctDate.contains(dateByText)) {
                    distinctDate.add(dateByText);
                }
            }

            for (Comment comment : commentsByUserId) {
                Date date = comment.getUtilTimestamp();
                if (date == null) {
                    continue;
                }

                String dateByText = simpleDateFormat.format(date);
                if (!distinctDate.contains(dateByText)) {
                    distinctDate.add(dateByText);
                }
            }

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

    /**
     * Delete a question
     *
     * @return
     */
    @DeleteMapping("/deleteQuestion/{questionId}")
    @Transactional
    public Map<String, String> deleteQuestion(@RequestBody AppUser appUser,
                                              @PathVariable Long questionId,
                                              HttpServletRequest request) {
        try {
            String methodName = "AdminController.deleteQuestion";

            controllerUtils.validateAppUser(appUser, methodName, true);

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

            // Then remove the question
            questionRepository.delete(question);

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

            // Then remove the answer
            answerRepository.delete(answer);

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
    public Map<String, String> deleteComment(@RequestBody AppUser appUser,
                                             @PathVariable Long id,
                                             HttpServletRequest request) {
        try {
            String methodName = "AdminController.deleteComment";

            Comment comment = commentRepository.findById(id)
                    .orElseThrow(() -> new Exception(methodName + ": Found no answer with id: " + id));

            controllerUtils.validateAppUser(appUser, methodName, false);

            if (appUser.isAnonymous()) {
                appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
                comment.setAppUser(appUser);
            } else {
                controllerUtils.validateAppUser(appUser, methodName, true);
            }

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
