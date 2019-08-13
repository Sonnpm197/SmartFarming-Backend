package com.son.CapstoneProject.service;

import com.son.CapstoneProject.controller.user.AnswerController;
import com.son.CapstoneProject.entity.AppUserTag;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.repository.AppUserTagRepository;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.TagRepository;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.son.CapstoneProject.common.ConstantValue.*;

@Service
public class ViewCountingService {

    private static final Logger logger = LoggerFactory.getLogger(ViewCountingService.class);

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private TagRepository tagRepository;

    // Provide thread safe map
    // View count for user by ip address
    private Map<Long, Map<String, Long>> contentIdWithIpAddressAndTime = new ConcurrentHashMap<>();

    // View count by userId for question
    private Map<Long, Map<Long, Long>> contentIdWithUserIdAndTimeForQuestion = new ConcurrentHashMap<>();

    // View count by userId for question
    private Map<Long, Map<Long, Long>> contentIdWithUserIdAndTimeForArticle = new ConcurrentHashMap<>();

    /**
     * This method is called in a separated thread to count view of the question
     * (not in the same thread in the @RestController)
     * It doesn't matter when this method finishes its work,
     * and users can still have the count value after receiving the question/article
     *
     * @param contentId
     * @param ipAddress
     */
    @Async("specificTaskExecutor")
    @Transactional
    public void countViewByIpAddress(Long contentId, String ipAddress, String type) {
//        System.out.println("ViewCountingService.countView Delayed");
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("ViewCountingService.countView get called");
        Map<String, Long> ipAddressWithTime = contentIdWithIpAddressAndTime.get(contentId);
        Long currentTime = System.currentTimeMillis();

        // Remove the previous ip address for this question if it has been for 24 hours
        if (ipAddressWithTime != null) {
            for (Map.Entry<String, Long> entry : ipAddressWithTime.entrySet()) {
                Long timeGap = currentTime - entry.getValue(); // currentTime - previousTime
//                if (TimeUnit.MILLISECONDS.toSeconds(timeGap) >= 30) { // for test
                if (TimeUnit.MILLISECONDS.toHours(timeGap) >= MAXIMUM_TIME_SAVED_FOR_IP_IN_HOUR) {
                    ipAddressWithTime.remove(entry.getKey());
                }
            }
        }

        // First time this question is seen
        if (ipAddressWithTime == null) {
            ipAddressWithTime = new ConcurrentHashMap<>();
            ipAddressWithTime.put(ipAddress, System.currentTimeMillis());
            contentIdWithIpAddressAndTime.put(contentId, ipAddressWithTime);

            // Update to database
            updateViewCount(contentId, type);

        } else {
            boolean foundIp = false;
            for (Map.Entry<String, Long> entry : ipAddressWithTime.entrySet()) {
                // If this questionId contains ipAddress of an user who has viewed -> see previous time
                if (entry.getKey().equalsIgnoreCase(ipAddress)) {
                    Long previousTime = entry.getValue();
                    Long timeGap = System.currentTimeMillis() - previousTime;

                    // Update view of each user each 15 minutes
//                    if (TimeUnit.MILLISECONDS.toSeconds(timeGap) >= 10) { // for test
                    if (TimeUnit.MILLISECONDS.toMinutes(timeGap) >= INCREASE_COUNT_PER_MINUTES) {
                        // Update to database
                        updateViewCount(contentId, type);

                        // Remove the previous time
                        entry.setValue(currentTime);
                    }

                    foundIp = true;
                    break;
                }
            }

            // if this ipAddress hasn't come into this post
            if (!foundIp) {
                ipAddressWithTime.put(ipAddress, System.currentTimeMillis());

                // Update to database
                updateViewCount(contentId, type);
            }

        }
    }

    @Async("specificTaskExecutor")
    @Transactional
    public void countViewByUserId(Long contentId, Long userId, String type) {
        logger.info("UserId: {} counting for type: {} with contentId: {}", userId, type, contentId);
        Map<Long, Long> userIdWithTime = null;

        if (QUESTION.equalsIgnoreCase(type)) {
            userIdWithTime = contentIdWithUserIdAndTimeForQuestion.get(contentId);
        } else if (ARTICLE.equalsIgnoreCase(type)) {
            userIdWithTime = contentIdWithUserIdAndTimeForArticle.get(contentId);
        } else {
            logger.error("Unknown type: {}", type);
            return;
        }

        Long currentTime = System.currentTimeMillis();

        // Remove the previous userId for this question if it has been for 24 hours
        if (userIdWithTime != null) {
            for (Map.Entry<Long, Long> entry : userIdWithTime.entrySet()) {
                Long timeGap = currentTime - entry.getValue(); // currentTime - previousTime

                // TODO: Start uncomment this for production
//                if (TimeUnit.MILLISECONDS.toHours(timeGap) >= MAXIMUM_TIME_SAVED_FOR_IP_IN_HOUR) {
//                    userIdWithTime.remove(entry.getKey());
//                }
                // TODO: End uncomment this for production

                // Delete this after 5 minutes
                if (TimeUnit.MILLISECONDS.toMinutes(timeGap) >= 3) {
                    logger.info("This userId: {} has been in counting map for 3 mins", entry.getKey());
                    logger.info("Remove this user from userIdWithTime of type: {} and contentId: {}", type, contentId);
                    userIdWithTime.remove(entry.getKey());
                }
            }
        }

        // First time this question is seen
        if (userIdWithTime == null) {
            logger.info("Found no map userIdWithTime for type: {} and contentId: {}", type, contentId);
            userIdWithTime = new ConcurrentHashMap<>();
            userIdWithTime.put(userId, System.currentTimeMillis());

            if (QUESTION.equalsIgnoreCase(type)) {
                contentIdWithUserIdAndTimeForQuestion.put(contentId, userIdWithTime);
            } else if (ARTICLE.equalsIgnoreCase(type)) {
                contentIdWithUserIdAndTimeForArticle.put(contentId, userIdWithTime);
            }

            // Update to database
            updateViewCount(contentId, type);

        } else {
            boolean foundUserId = false;
            for (Map.Entry<Long, Long> entry : userIdWithTime.entrySet()) {
                // If this questionId contains userId of an user who has viewed -> see previous time
                if (entry.getKey().equals(userId)) {
                    Long previousTime = entry.getValue();
                    Long timeGap = System.currentTimeMillis() - previousTime;

                    // TODO: Start uncomment this for production
//                    if (TimeUnit.MILLISECONDS.toMinutes(timeGap) >= INCREASE_COUNT_PER_MINUTES) {
                    // TODO: End uncomment this for production

                    // Increase view after 1 minute of that user
                    if (TimeUnit.MILLISECONDS.toMinutes(timeGap) >= INCREASE_COUNT_PER_MINUTES_FOR_TEST) {
                        // Update to database
                        updateViewCount(contentId, type);

                        // Replace the previous time
                        entry.setValue(currentTime);
                    }

                    foundUserId = true;
                    break;
                }
            }

            // if this userId hasn't come into this post
            if (!foundUserId) {
                userIdWithTime.put(userId, System.currentTimeMillis());

                // Update to database
                updateViewCount(contentId, type);
            }

        }
    }

    private void updateViewCount(Long contentId, String type) {
        List<Tag> tags = null;
        if (QUESTION.equalsIgnoreCase(type)) {
            Optional<Question> optionalQuestion = questionRepository.findById(contentId);
            if (optionalQuestion.isPresent()) {
                Question question = optionalQuestion.get();
                question.setViewCount(question.getViewCount() + VIEW_COUNT);
                tags = question.getTags();
                questionRepository.save(question);

                // Increase total view for user (question only)
                AppUser questionAuthor = question.getAppUser();

                if (Role.USER.getValue().equalsIgnoreCase(questionAuthor.getRole())) {
                    questionAuthor.setViewCount(questionAuthor.getViewCount() + VIEW_COUNT);
                    appUserRepository.save(questionAuthor);
                }

                // Increase view count in AppUserTag
                for (Tag tag : tags) {
                    AppUserTag appUserTag = appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(questionAuthor.getUserId(), tag.getTagId());
                    if (appUserTag != null) {
                        appUserTag.setViewCount(appUserTag.getViewCount() + VIEW_COUNT);
                        appUserTagRepository.save(appUserTag);
                    } else {
                        if (Role.USER.getValue().equalsIgnoreCase(questionAuthor.getRole())) {
                            appUserTag = new AppUserTag();
                            appUserTag.setTag(tag);
                            appUserTag.setAppUser(questionAuthor);
                            appUserTag.setViewCount(VIEW_COUNT);
                            appUserTagRepository.save(appUserTag);
                        }
                    }
                }

            }
        } else if (ARTICLE.equalsIgnoreCase(type)) {
            Optional<Article> optionalArticle = articleRepository.findById(contentId);
            if (optionalArticle.isPresent()) {
                Article article = optionalArticle.get();
                article.setViewCount(article.getViewCount() + VIEW_COUNT);
                tags = article.getTags();
                articleRepository.save(article);
            }
        }

        if (tags == null) {
            // logger.info("No tags found fot type: " + type + " with id: " + id);
            return;
        }

        // Increase view for tags
        for (Tag tag : tags) {
            tag.setViewCount(tag.getViewCount() + VIEW_COUNT);
            tagRepository.save(tag);
        }

    }

}
