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
import org.apache.log4j.Logger;
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

    private Logger logger = Logger.getLogger(ViewCountingService.class.getSimpleName());

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
    private Map<Long, Map<String, Long>> contentIdWithIpAddressAndTime = new ConcurrentHashMap<>();

    /**
     * This method is called in a separated thread to count view of the question (not in the same thread in the @RestController)
     * It doesn't matter when this method finishes its work, and users can still have the count value after receiving the question/article
     *
     * @param id
     * @param ipAddress
     */
    @Async("specificTaskExecutor")
    @Transactional
    public void countView(Long id, String ipAddress, String type) {
//        System.out.println("ViewCountingService.countView Delayed");
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println("ViewCountingService.countView get called");
        Map<String, Long> ipAddressWithTime = contentIdWithIpAddressAndTime.get(id);
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
            contentIdWithIpAddressAndTime.put(id, ipAddressWithTime);

            // Update to database
            updateViewCount(id, type);

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
                        updateViewCount(id, type);

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
                updateViewCount(id, type);
            }

        }
    }

    private void updateViewCount(Long id, String type) {
        List<Tag> tags = null;
        if (QUESTION.equalsIgnoreCase(type)) {
            Optional<Question> optionalQuestion = questionRepository.findById(id);
            if (optionalQuestion.isPresent()) {
                Question question = optionalQuestion.get();
                question.setViewCount(question.getViewCount() + VIEW_COUNT);
                tags = question.getTags();
                questionRepository.save(question);

                // Increase total view for user (question only)
                AppUser questionAuthor = question.getAppUser();
                questionAuthor.setViewCount(questionAuthor.getViewCount() + VIEW_COUNT);
                appUserRepository.save(questionAuthor);

                // Increase view count in AppUserTag
                for (Tag tag : tags) {
                    AppUserTag appUserTag = appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(questionAuthor.getUserId(), tag.getTagId());
                    appUserTag.setViewCount(appUserTag.getViewCount() + VIEW_COUNT);
                    appUserTagRepository.save(appUserTag);
                }

            }
        } else if (ARTICLE.equalsIgnoreCase(type)) {
            Optional<Article> optionalArticle = articleRepository.findById(id);
            if (optionalArticle.isPresent()) {
                Article article = optionalArticle.get();
                article.setViewCount(article.getViewCount() + VIEW_COUNT);
                tags = article.getTags();
                articleRepository.save(article);
            }
        }

        if (tags == null) {
            logger.info("No tags found fot type: " + type + " with id: " + id);
            return;
        }

        // Increase view for tags
        for (Tag tag : tags) {
            tag.setViewCount(tag.getViewCount() + VIEW_COUNT);
            tagRepository.save(tag);
        }

    }

}
