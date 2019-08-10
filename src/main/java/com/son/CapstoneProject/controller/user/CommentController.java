package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.Comment;
import com.son.CapstoneProject.entity.Notification;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.CommentRepository;
import com.son.CapstoneProject.repository.NotificationRepository;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/comment")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
    }

    /**
     * This method can add comment to all articles, questions and answers
     *
     * @param comment
     * @param request
     * @return
     */
    @PostMapping(value = "/addComment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public Comment addComment(@RequestBody Comment comment, HttpServletRequest request) {
        try {
            String methodName = "UserController.addComment";

            // User who comments
            AppUser appUser = comment.getAppUser();

            controllerUtils.validateAppUser(appUser, methodName, false);

            if (appUser.isAnonymous()) {
                appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
                comment.setAppUser(appUser);
            } else {
                controllerUtils.validateAppUser(appUser, methodName, true);

                // Get full data from request
                Long appUserId = appUser.getUserId();
                appUser = appUserRepository.findById(appUserId)
                        .orElseThrow(() -> new Exception(methodName + ": cannot find any user of this comment by id: " + appUserId));
            }

            // When this user comment on an article, that means he is subscribing
            if (comment.getArticle() != null && comment.getArticle().getArticleId() != null) {
                Long articleId = comment.getArticle().getArticleId();
                // Check this article
                Article article = articleRepository.findById(articleId)
                        .orElseThrow(() -> new Exception(methodName + " cannot find any article with id: " + articleId));

                // Create a message of this user
                StringBuilder stringBuilder = new StringBuilder();
                if (ConstantValue.Role.ANONYMOUS.getValue().equalsIgnoreCase(appUser.getRole())) {
                    stringBuilder.append("AnonymousUser")
                            .append(appUser.getUserId());
                } else {
                    if (appUser.getSocialUser() != null) {
                        stringBuilder.append(appUser.getSocialUser().getName());
                    }
                }

                stringBuilder.append(" vừa trả lời bài viết: ")
                        .append(article.getTitle());

                // Tt will send notifications to all other subscribers
                List<AppUser> subscribers = article.getSubscribers();
                for (AppUser subscriber : subscribers) {
                    if (subscriber.equals(appUser)) {
                        continue;
                    }
                    Notification notification = new Notification();
                    notification.setUtilTimestamp(new Date());
                    notification.setArticle(article);
                    notification.setFromAdmin(false);
                    notification.setAppUserReceiver(subscriber);
                    notification.setMessage(stringBuilder.toString());
                    notificationRepository.save(notification);
                }

                // Then add this user as a subscriber
                if (!article.getSubscribers().contains(appUser)) {
                    article.getSubscribers().add(appUser);
                }
                comment.setArticle(articleRepository.save(article));
            }

            comment.setUtilTimestamp(new Date());
            return commentRepository.save(comment);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PutMapping("/updateComment/{id}")
    @Transactional
    public ResponseEntity<Comment> updateComment(@RequestBody Comment updatedComment,
                                                 @PathVariable Long id,
                                                 HttpServletRequest request) {
        try {
            String methodName = "UserController.updateComment";

            Comment oldComment = commentRepository.findById(id)
                    .orElseThrow(() -> new Exception(methodName + ": Not found any article with id: " + id));

            AppUser appUser = updatedComment.getAppUser();

            controllerUtils.validateAppUser(appUser, methodName, false);

            if (appUser.isAnonymous()) {
                appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
                updatedComment.setAppUser(appUser);
            } else {
                controllerUtils.validateAppUser(appUser, methodName, true);
            }

            // Cannot update other comment
            if (!appUser.getUserId().equals(oldComment.getAppUser().getUserId())) {
                String message = "UserController.updateComment: You cannot update others' comments";
                // logger.info(message);
                throw new Exception(message);
            }

            oldComment.setContent(updatedComment.getContent());
            return ResponseEntity.ok(commentRepository.save(oldComment));
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
            String methodName = "UserController.deleteComment";

            Comment comment = commentRepository.findById(id)
                    .orElseThrow(() -> new Exception(methodName + ": Found no answer with id: " + id));

            controllerUtils.validateAppUser(appUser, methodName, false);

            if (appUser.isAnonymous()) {
                appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
                comment.setAppUser(appUser);
            } else {
                controllerUtils.validateAppUser(appUser, methodName, true);
            }

            // Cannot delete other questions
            if (!appUser.getUserId().equals(comment.getAppUser().getUserId())) {
                String message = methodName + ": You cannot delete others' comment";
                // logger.info(message);
                throw new Exception(message);
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
