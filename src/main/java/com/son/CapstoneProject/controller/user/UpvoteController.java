package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.login.UserRole;
import com.son.CapstoneProject.repository.*;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import com.son.CapstoneProject.repository.loginRepository.UserRoleRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.son.CapstoneProject.entity.login.AppRole.ROLE_USER;

@RestController
@RequestMapping("/upvote")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class UpvoteController {

    private Logger logger = Logger.getLogger(UpvoteController.class.getSimpleName());

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    private static final int ARTICLE_UPVOTE_POINT = 1;
    private static final int ARTICLE_DOWNVOTE_POINT = -1;

    private static final int QUESTION_UPVOTE_POINT = 1;
    private static final int QUESTION_DOWNVOTE_POINT = -1;

    private static final int ANSWER_UPVOTE_POINT = 1;
    private static final int ANSWER_DOWNVOTE_POINT = -1;

    private static final int COMMENT_UPVOTE_POINT = 1;
    private static final int COMMENT_DOWNVOTE_POINT = -1;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
    }

    @PostMapping(value = "/{type}/{id}")
    @Transactional
    public void upvote(@RequestBody AppUser userUpvote,
                       @PathVariable String type,
                       @PathVariable Long id,
                       HttpServletRequest request) throws Exception {

        String methodName = "UpvoteController.upvote";

        Article article = null;
        Question question = null;
        Answer answer = null;
        Comment comment = null;
        AppUser author = null;
        List<Long> appUserList = null;
        List<Tag> tags = null;

        controllerUtils.validateAppUser(userUpvote, methodName, false);

        if (userUpvote.isAnonymous()) {
            userUpvote = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
        } else {
            controllerUtils.validateAppUser(userUpvote, methodName, true);
        }

        if ("article".equalsIgnoreCase(type)) {
            article = articleRepository.findById(id)
                    .orElseThrow(() -> new Exception(methodName + ": Not found article by id: " + id));
            author = article.getAppUser();
            appUserList = article.getUpvotedUserIds();

        } else if ("question".equalsIgnoreCase(type)) {

            question = questionRepository.findById(id)
                    .orElseThrow(() -> new Exception(methodName + ": Not found question by id: " + id));
            author = question.getAppUser();
            appUserList = question.getUpvotedUserIds();
            tags = question.getTags();

        } else if ("answer".equalsIgnoreCase(type)) {

            answer = answerRepository.findById(id)
                    .orElseThrow(() -> new Exception(methodName + ": Not found answer by id: " + id));
            author = answer.getAppUser();
            appUserList = answer.getUpvotedUserIds();
            tags = answer.getQuestion().getTags();

        } else if ("comment".equalsIgnoreCase(type)) {

            comment = commentRepository.findById(id)
                    .orElseThrow(() -> new Exception(methodName + ": Not found comment by id: " + id));
            author = comment.getAppUser();
            appUserList = comment.getUpvotedUserIds();

            // Comment can be from article, question, answer
            // Find the origin of this comment then increase vote for the tags
            if (comment.getArticle() != null) {
                tags = comment.getArticle().getTags();
            } else if (comment.getQuestion() != null) {
                tags = comment.getQuestion().getTags();
            } else if (comment.getAnswer() != null) {
                tags = comment.getAnswer().getQuestion().getTags();
            }

        }

        if (author == null) {
            String message = methodName + " cannot find the appropriate author";
            logger.info(message);
            throw new Exception(message);
        }

        if (tags == null) {
            String message = methodName + " cannot find the relative tags";
            logger.info(message);
            throw new Exception(message);
        }

        // You cannot like your own article, question, answer or comment
        if (userUpvote.getUserId().equals(author.getUserId())) {
            String message = methodName + ": You cannot like our own question";
            logger.info(message);
            throw new Exception(message);
        }

        // Increase 1 reputation for the one who receives your like

        if (appUserList == null) {
            appUserList = new ArrayList<>();
            appUserList.add(userUpvote.getUserId());
            // Decrease tag point of that post
            updateReputation(tags, author, type, true);
        } else {
            // Click like again => dislike
            Iterator<Long> iterator = appUserList.iterator();

            boolean userAlreadyLikedPost = false;
            while (iterator.hasNext()) {
                Long existedId = iterator.next();
                // If they click again means they dislike this
                if (existedId.equals(userUpvote.getUserId())) {
                    userAlreadyLikedPost = true;

                    // Decrease tag point of that post
                    updateReputation(tags, author, type, false);

                    // Then remove that userId
                    iterator.remove();
                }
            }

            // He hasn't liked yet
            if (!userAlreadyLikedPost) {

                appUserList.add(userUpvote.getUserId());

                // Increase tag point of that post
                updateReputation(tags, author, type, true);
            }
        }

        if ("article".equalsIgnoreCase(type)) {
            article.setUpvotedUserIds(appUserList);
            articleRepository.save(article);
        } else if ("question".equalsIgnoreCase(type)) {
            question.setUpvotedUserIds(appUserList);
            questionRepository.save(question);
        } else if ("answer".equalsIgnoreCase(type)) {
            answer.setUpvotedUserIds(appUserList);
            answerRepository.save(answer);
        } else if ("comment".equalsIgnoreCase(type)) {
            comment.setUpvotedUserIds(appUserList);
            commentRepository.save(comment);
        }
    }

    /**
     * This method is to increase question's author reputation including:
     * Only increase tag point of admin.
     * <p>
     * Increase whole tag points
     * Increase reputation of that user
     * Increase specific tag points of that user
     * <p>
     * updatedPoint = +1 => an user hits like
     * updatedPoint = -1 => an user hits like again => dislike
     */
    private void updateReputation(List<Tag> tags, AppUser appUser, String type, boolean isUpVote) {
        int updatedPoint = 0;
        if ("article".equalsIgnoreCase(type)) {
            if (isUpVote) {
                updatedPoint = ARTICLE_UPVOTE_POINT;
            } else {
                updatedPoint = ARTICLE_DOWNVOTE_POINT;
            }
        } else if ("question".equalsIgnoreCase(type)) {
            if (isUpVote) {
                updatedPoint = QUESTION_UPVOTE_POINT;
            } else {
                updatedPoint = QUESTION_DOWNVOTE_POINT;
            }
        } else if ("answer".equalsIgnoreCase(type)) {
            if (isUpVote) {
                updatedPoint = ANSWER_UPVOTE_POINT;
            } else {
                updatedPoint = ANSWER_DOWNVOTE_POINT;
            }
        } else if ("comment".equalsIgnoreCase(type)) {
            if (isUpVote) {
                updatedPoint = COMMENT_UPVOTE_POINT;
            } else {
                updatedPoint = COMMENT_DOWNVOTE_POINT;
            }
        }

        // Increase whole tag points
        for (Tag tag : tags) {
            if (tag.getReputation() == 0 && updatedPoint < 0) {
                tag.setReputation(0);
            } else {
                tag.setReputation(tag.getReputation() + updatedPoint);
            }
            tagRepository.save(tag);
        }

        // Then increase AppUser reputation
        if (appUser.getReputation() == 0 && updatedPoint < 0) {
            appUser.setReputation(0);
        } else {
            UserRole userRole = userRoleRepository.findByAppUser_UserId(appUser.getUserId());
            if (userRole != null) {
                // Only increase reputation for users
                if (userRole.getAppRole().getRoleName().equalsIgnoreCase(ROLE_USER)) {
                    appUser.setReputation(appUser.getReputation() + updatedPoint);
                }
            }
            appUserRepository.save(appUser);

            // Increase specific tag points of that user
            Long userId = appUser.getUserId();
            for (Tag tag : tags) {
                Long tagId = tag.getTagId();
                AppUserTag appUserTag = appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(userId, tagId);
                if (appUserTag != null) {
                    // Increase appUser tags point if it exists
                    if (appUserTag.getReputation() == 0 && updatedPoint < 0) {
                        appUserTag.setReputation(0);
                    } else {
                        appUserTag.setReputation(appUserTag.getReputation() + updatedPoint);
                    }
                    appUserTagRepository.save(appUserTag);
                } else {
                    // Add new
                    AppUserTag newAppUserTag = new AppUserTag();
                    newAppUserTag.setAppUser(appUser);
                    newAppUserTag.setTag(tag);
                    newAppUserTag.setReputation(1);
                    appUserTagRepository.save(newAppUserTag);
                }
            }
        }
    }
}
