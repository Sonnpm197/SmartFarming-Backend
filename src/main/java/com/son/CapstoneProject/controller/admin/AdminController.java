package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.search.GenericClass;
import com.son.CapstoneProject.repository.*;
import com.son.CapstoneProject.repository.searchRepository.HibernateSearchRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.son.CapstoneProject.common.ConstantValue.RANKING_VIEW_COUNT_GAP;
import static com.son.CapstoneProject.common.ConstantValue.TAG;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class AdminController {

    private Logger logger = Logger.getLogger(AdminController.class.getSimpleName());

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
    private ControllerUtils controllerUtils;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private HibernateSearchRepository hibernateSearchRepository;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an administrator";
    }

    /**
     * Admins can add a new article
     * ** Tag from articles do not count any points to admins
     *
     * @param article
     * @return
     */
    @Transactional
    @PostMapping(value = "/addArticle",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Article> addArticle(@RequestBody Article article) throws Exception {
        String methodName = "AdminController.addArticle";

        AppUser appUser = article.getAppUser();

        controllerUtils.validateAppUser(appUser, methodName, true);

        article.setUtilTimestamp(new Date());

        // Save tags first (distinctive name)
        List<Tag> tags = controllerUtils.saveDistinctiveTags(article.getTags());
        article.setTags(tags);

        return ResponseEntity.ok(articleRepository.save(article));
    }

    /**
     * Admins can update an article
     *
     * @param updatedArticle
     * @return
     * @throws Exception
     */
    @PutMapping("/updateArticle/{articleId}")
    @Transactional
    public ResponseEntity<Article> updateArticle(
            @RequestBody Article updatedArticle,
            @PathVariable Long articleId)
            throws Exception {
        String methodName = "AdminController.updateArticle";

        Article oldArticle = articleRepository.findById(articleId)
                .orElseThrow(() -> new Exception(methodName + ": Not found any article with id: " + articleId));

        // Save tags first
        List<Tag> tags = controllerUtils.saveDistinctiveTags(updatedArticle.getTags());

        // Update values
        oldArticle.setTitle(updatedArticle.getTitle());
        oldArticle.setContent(updatedArticle.getContent());
        oldArticle.setTags(tags);
        oldArticle.setFileDownloadUris(updatedArticle.getFileDownloadUris());
        oldArticle.setUtilTimestamp(new Date());

        // Save to database
        Article question = articleRepository.save(oldArticle);
        return ResponseEntity.ok(question);
    }

    /**
     * Admins can delete an article
     *
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteArticle/{id}")
    public Map<String, String> deleteArticle(@PathVariable Long id) throws Exception {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new Exception("AdminController.deleteArticle: Not found any article with id: " + id));

        // Delete article
        articleRepository.delete(article);

        Map<String, String> map = new HashMap<>();
        map.put("articleId", ("" + id));
        map.put("deleted", "true");
        return map;
    }

    @GetMapping("/viewAllReports")
    public List<Report> viewAllReports() {
        return reportRepository.findAll();
    }

    /**
     * ADMIN delete a question
     *
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteQuestion/{id}")
    public Map<String, String> deleteQuestion(@PathVariable Long id) throws Exception {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new Exception("AdminController.deleteQuestion: Not found question with id: " + id));

        questionRepository.delete(question);
        Map<String, String> map = new HashMap<>();
        map.put("questionId", "" + id);
        map.put("deleted", "true");
        return map;
    }

    @GetMapping("/searchTopUsersByKeyword/{keyword}")
    public List<AppUserTag> searchTopUserByKeyword(@PathVariable String keyword) {

        // Admin search tags first
        List<Tag> tags = (List<Tag>) hibernateSearchRepository.search2(
                keyword,
                TAG,
                new String[]{"name", "description"},
                null
        );

        List<AppUserTag> finalAppUserTags = new ArrayList<>();

        // After receiving tags, use that Id to search for user in AppUserTag
        for (Tag tag : tags) {
            Long tagId = tag.getTagId();
            List<AppUserTag> appUserTagsByTagId = appUserTagRepository.findByTag_TagId(tagId);

            // Do not count anonymous user
            for (AppUserTag appUserTag : appUserTagsByTagId) {
                if (!appUserTag.getAppUser().isAnonymous()) {
                    finalAppUserTags.add(appUserTag);
                }
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
                else if (appUserTag1.getReputation() >= appUserTag2.getReputation()
                        && appUserTag1.getViewCount() - appUserTag2.getViewCount() > RANKING_VIEW_COUNT_GAP
                        ||
                        appUserTag1.getReputation() < appUserTag2.getReputation()
                                && appUserTag1.getViewCount() - appUserTag2.getViewCount() > RANKING_VIEW_COUNT_GAP
                ) {
                    return 1; // appUserTag1 > appUserTag2
                } else if (appUserTag1.getReputation() < appUserTag2.getReputation()
                        && appUserTag2.getViewCount() - appUserTag1.getViewCount() > RANKING_VIEW_COUNT_GAP
                ) {
                    return -1; // appUserTag1 < appUserTag2
                }

                return 0;
            }
        });

        return finalAppUserTags;
    }

    /**
     * ADMIN view the number of view in website
     * Total of article + question
     *
     * @return
     * @throws Exception
     */
    @GetMapping("/totalWebSiteViewCount")
    public int totalView() {
        int articleTotalView = articleRepository.getTotalViewCount();
        int questionTotalView = questionRepository.getTotalViewCount();
        return articleTotalView + questionTotalView;
    }

    @GetMapping("/userChartInfo/{userId}")
    public Map<String, UserChartInfo> detailUserActivitiesByDays(@PathVariable Long userId) {
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

        Map<String, UserChartInfo> userChartInfoByDate = new HashMap<>();
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        // After get distinc date then select to get the list data for each day
        for (String date : distinctDate) {
            try {
                Date startDate = simpleDateFormat1.parse(date + " 00:00");
                Date endDate = simpleDateFormat1.parse(date + " 23:59");

                List<Question> questions = questionRepository.findAllByUtilTimestampBetween(startDate, endDate);
                List<Answer> answers = answerRepository.findAllByUtilTimestampBetween(startDate, endDate);
                List<Comment> comments = commentRepository.findAllByUtilTimestampBetween(startDate, endDate);

                UserChartInfo userChartInfo = new UserChartInfo();
                userChartInfo.setNumberOfQuestion(questions.size());
                for (Question question : questions) {
                    if (question.getUpvotedUserIds() != null && question.getUpvotedUserIds().size() > 0) {
                        userChartInfo.setTotalQuestionReputation(userChartInfo.getTotalQuestionReputation() + question.getUpvotedUserIds().size());
                    }
                }

                userChartInfo.setNumberOfAnswer(answers.size());
                for (Answer answer : answers) {
                    if (answer.getUpvotedUserIds() != null && answer.getUpvotedUserIds().size() > 0) {
                        userChartInfo.setTotalAnswerReputation(userChartInfo.getTotalAnswerReputation() + answer.getUpvotedUserIds().size());
                    }
                }

                userChartInfo.setNumberOfComment(comments.size());
                for (Comment comment : comments) {
                    if (comment.getUpvotedUserIds() != null && comment.getUpvotedUserIds().size() > 0) {
                        userChartInfo.setTotalCommentReputation(userChartInfo.getTotalCommentReputation() + comment.getUpvotedUserIds().size());
                    }
                }

                // Add to map
                userChartInfoByDate.put(date, userChartInfo);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return userChartInfoByDate;
    }
}
