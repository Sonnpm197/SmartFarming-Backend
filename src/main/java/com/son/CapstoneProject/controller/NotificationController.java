package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.Comment;
import com.son.CapstoneProject.entity.Notification;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.pagination.NotificationPagination;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.CommentRepository;
import com.son.CapstoneProject.repository.NotificationRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.son.CapstoneProject.common.ConstantValue.NOTIFICATION_PER_PAGE;

@RestController
@RequestMapping("/notification")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
    }

    @DeleteMapping("/delete/{id}")
    @Transactional
    public Map<String, String> deleteNotification(@PathVariable Long id) {
        try {
            String methodName = "NotificationController.deleteNotification";

            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new Exception(methodName + ": Found no notification with id: " + id));

            notificationRepository.delete(notification);

            Map<String, String> map = new HashMap<>();
            map.put("notificationId", "" + id);
            map.put("deleted", "true");
            return map;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/unsubscribe")
    @Transactional
    public Notification unsubscribe(@RequestBody Notification notification) {
        try {
            String methodName = "NotificationController.deleteNotification";

            Notification fullDataNotification = notificationRepository.findById(notification.getNotificationId())
                    .orElseThrow(() -> new Exception(methodName + ": Found no notification with id: " + notification.getNotificationId()));

            AppUser receiver = fullDataNotification.getAppUserReceiver();

            // Unsubscribe this question
            if (fullDataNotification.getQuestion() != null) {
                Question question = fullDataNotification.getQuestion();
                question.getSubscribers().remove(receiver);
                questionRepository.save(question);
            }
            // Unsubscribe this article
            else if (fullDataNotification.getArticle() != null) {
                Article article = fullDataNotification.getArticle();
                article.getSubscribers().remove(receiver);
                articleRepository.save(article);
            }

            return fullDataNotification;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * When they click on this to view, that means they know this notification
     * @param pageNumber
     * @return
     */
    @GetMapping("/viewNotificationsByPageIndex/{userId}/{pageNumber}")
    public NotificationPagination viewNotificationsByPageIndex(@PathVariable Long userId, @PathVariable int pageNumber) {
        try {
            logger.info("pageNumber: {}", pageNumber);
            PageRequest pageNumWithElements = PageRequest.of(pageNumber, NOTIFICATION_PER_PAGE, Sort.by("utilTimestamp"));
            Page<Notification> notificationPage = notificationRepository.findByAppUserReceiver_UserId(userId, pageNumWithElements);

            // Return pagination objects
            NotificationPagination notificationPagination = new NotificationPagination();
            notificationPagination.setNotificationsByPageIndex(notificationPage.getContent());

            List<Notification> notifications = notificationPage.getContent();
            int numberOfNotifications = notifications.size();

            if (numberOfNotifications % NOTIFICATION_PER_PAGE == 0) {
                logger.info("numberOfNotificationPages : {}", numberOfNotifications / NOTIFICATION_PER_PAGE);
                notificationPagination.setNumberOfPages(numberOfNotifications / NOTIFICATION_PER_PAGE);
            } else {
                logger.info("numberOfNotificationPages : {}", (numberOfNotifications / NOTIFICATION_PER_PAGE) + 1);
                notificationPagination.setNumberOfPages(numberOfNotifications / NOTIFICATION_PER_PAGE + 1);
            }

            // Update seen data
            for (Notification notification : notifications) {
                notification.setSeen(true);
                notificationRepository.save(notification);
            }

            return notificationPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

}
