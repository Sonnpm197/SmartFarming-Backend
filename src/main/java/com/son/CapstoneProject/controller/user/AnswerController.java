package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.common.entity.*;
import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.common.entity.login.AppUser;
import com.son.CapstoneProject.repository.*;
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
import java.util.*;

import static com.son.CapstoneProject.common.ConstantValue.MARK_ACCEPTED_ANSWER_POINT;

@RestController
@RequestMapping("/answer")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class AnswerController {

    private static final Logger logger = LoggerFactory.getLogger(AnswerController.class);

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
    }


    /**
     * Add answers
     *
     * @param answer
     * @return
     */
    @PostMapping(value = "/addAnswerToQuestion",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public Answer addAnswerToQuestion(@RequestBody Answer answer, HttpServletRequest request) {
        try {
            String methodName = "UserController.addAnswerToQuestion";

            // User who answers the question
            AppUser appUser = answer.getAppUser();

            controllerUtils.validateAppUser(appUser, methodName, false);

            if (appUser.isAnonymous()) {
                appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
                answer.setAppUser(appUser);
            } else {
                controllerUtils.validateAppUser(appUser, methodName, true);

                // Get full data from request
                Long appUserId = appUser.getUserId();
                appUser = appUserRepository.findById(appUserId)
                        .orElseThrow(() -> new Exception(methodName + ": cannot find any user of this answer by id: " + appUserId));
            }

            answer.setUtilTimestamp(new Date());

            // Send notification to the author
            Question question = answer.getQuestion();
            if (question == null || question.getQuestionId() == null) {
                throw new Exception(methodName + " cannot find question in request body");
            }

            Question fullDataQuestion = questionRepository.findById(question.getQuestionId())
                    .orElseThrow(() -> new Exception(methodName + " cannot find question with id: " + question.getQuestionId()));

            AppUser authorOfTheQuestion = fullDataQuestion.getAppUser();

            StringBuilder stringBuilder = new StringBuilder();
            if (ConstantValue.Role.ANONYMOUS.getValue().equalsIgnoreCase(appUser.getRole())) {
                stringBuilder.append("AnonymousUser")
                        .append(appUser.getUserId());
            } else {
                if (appUser.getSocialUser() != null) {
                    stringBuilder.append(appUser.getSocialUser().getName());
                }
            }

            stringBuilder.append(" vừa trả lời câu hỏi: ")
                    .append(fullDataQuestion.getTitle());

            // Tt will send notifications to all other subscribers
            List<AppUser> subscribers = fullDataQuestion.getSubscribers();
            for (AppUser subscriber : subscribers) {
                if (subscriber.equals(appUser)) {
                    continue; // do not send notification to you
                }
                // Send notification to the author of the question
                Notification notification = new Notification();
                notification.setMessage(stringBuilder.toString());
                notification.setAppUserReceiver(subscriber);
//                notification.setFromAdmin(false);
                notification.setUtilTimestamp(new Date());
                notification.setQuestion(fullDataQuestion);
                notificationRepository.save(notification);
            }

            // If this user is new then add him as a subscriber
            // Then add this user as a subscriber
            if (!fullDataQuestion.getSubscribers().contains(appUser)) {
                fullDataQuestion.getSubscribers().add(appUser);
            }
            answer.setQuestion(questionRepository.save(fullDataQuestion));

            return answerRepository.save(answer);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Update answer
     *
     * @param updatedAnswer
     * @param answerId
     * @return
     */
    @PutMapping("/updateAnswerToQuestion/{answerId}")
    @Transactional
    public ResponseEntity<Answer> updateAnswerToQuestion(@RequestBody Answer updatedAnswer,
                                                         @PathVariable Long answerId,
                                                         HttpServletRequest request) {
        try {
            String methodName = "UserController.updateAnswerToQuestion";

            Answer oldAnswer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new Exception(methodName + ": Not found any answers with id: " + answerId));

            AppUser appUser = updatedAnswer.getAppUser();

            controllerUtils.validateAppUser(appUser, methodName, false);

            if (appUser.isAnonymous()) {
                appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
                updatedAnswer.setAppUser(appUser);
            } else {
                controllerUtils.validateAppUser(appUser, methodName, true);
            }

            // Cannot delete other questions
            if (!oldAnswer.getAppUser().getUserId().equals(appUser.getUserId())) {
                String message = methodName + ": You cannot update others' answers";
                // logger.info(message);
                throw new Exception(message);
            }

            oldAnswer.setContent(updatedAnswer.getContent());
            Answer answer = answerRepository.save(oldAnswer);
            return ResponseEntity.ok(answer);
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
            String methodName = "UserController.deleteAnswerToQuestion";

//            controllerUtils.validateAppUser(appUser, methodName, false);
//
//            if (appUser.isAnonymous()) {
//                appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
//            } else {
//                controllerUtils.validateAppUser(appUser, methodName, true);
//            }

            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new Exception(methodName + ": Found no answer with id: " + answerId));
//
//            // Cannot delete other questions
//            if (!appUser.getUserId().equals(answer.getAppUser().getUserId())) {
//                String message = methodName + ": You cannot delete others' answers";
//                // logger.info(message);
//                throw new Exception(message);
//            }

            // Delete comment in answer first
            List<Comment> comments = answer.getComments();
            Iterator<Comment> commentIterator = comments.iterator();

            while (commentIterator.hasNext()) {
                Comment comment = commentIterator.next();
                commentRepository.delete(comment);
            }

            Question question = answer.getQuestion();

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
                        resultPoint = currentPoint - 0;
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
     * Mark this answer is accepted (only used by the author)
     *
     * @return
     */
    @PutMapping("/markAcceptedAnswerToQuestion/{questionId}/{answerId}")
    @Transactional
    public ResponseEntity<Answer> markAcceptedAnswerToQuestion(@RequestBody AppUser questionAuthor,
                                                               @PathVariable Long questionId,
                                                               @PathVariable Long answerId) {
        try {
            String methodName = "UserController.markAcceptedAnswerToQuestion";

            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new Exception(methodName + ": Not found any answers with id: " + answerId));

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new Exception(methodName + ": Not found any questions with id: " + questionId));

            controllerUtils.validateAppUser(questionAuthor, methodName, true);

            // Check if you are the author
            if (questionAuthor.getUserId().equals(answer.getAppUser().getUserId())) {
                String message = methodName + ": You cannot mark accepted answer if you are not the author of the question";
                // logger.info(message);
                throw new Exception(message);
            }

            // Increase reputation point for author of the answer
            AppUser userAnswerQuestion = answer.getAppUser();
            userAnswerQuestion.setReputation(userAnswerQuestion.getReputation() + MARK_ACCEPTED_ANSWER_POINT);
            appUserRepository.save(userAnswerQuestion);

            // Increase AppUserTag of the user who answers the question and gets accepted
            List<Tag> questionTags = question.getTags();
            for (Tag tag : questionTags) {
                AppUserTag appUserTag = appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(userAnswerQuestion.getUserId(), tag.getTagId());
                if (appUserTag == null) {
                    appUserTag = new AppUserTag();
                    appUserTag.setTag(tag);
                    appUserTag.setAppUser(userAnswerQuestion);
                }

                // Increase point of that tag for user who gets accepted answer
                appUserTag.setReputation(appUserTag.getReputation() + MARK_ACCEPTED_ANSWER_POINT);

                // Save to databse
                appUserTagRepository.save(appUserTag);
            }

            // Mark this answer as accepted answer
            answer.setAccepted(true);
            answer = answerRepository.save(answer);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Unmark this answer is accepted (only used by the author)
     *
     * @return
     */
    @PutMapping("/unmarkAcceptedAnswerToQuestion/{questionId}/{answerId}")
    @Transactional
    public ResponseEntity<Answer> unmarkAcceptedAnswerToQuestion(/*@RequestBody AppUser questionAuthor,*/
            @PathVariable Long questionId,
            @PathVariable Long answerId) {
        try {
            String methodName = "UserController.unmarkAcceptedAnswerToQuestion";

            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new Exception(methodName + ": Not found any answers with id: " + answerId));

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new Exception(methodName + ": Not found any questions with id: " + questionId));

//            controllerUtils.validateAppUser(questionAuthor, methodName, true);
//
//            // Check if you are the author
//            if (questionAuthor.getUserId().equals(answer.getAppUser().getUserId())) {
//                String message = methodName + ": You cannot mark accepted answer if you are not the author of the question";
//                // logger.info(message);
//                throw new Exception(message);
//            }

            if (!answer.isAccepted()) {
                return null;
            }

            // Increase reputation point for author of the answer
            AppUser userAnswerQuestion = answer.getAppUser();
            if (userAnswerQuestion.getReputation() > 0) {
                userAnswerQuestion.setReputation(userAnswerQuestion.getReputation() - MARK_ACCEPTED_ANSWER_POINT);
            }
            appUserRepository.save(userAnswerQuestion);

            // Increase AppUserTag of the user who answers the question and gets accepted
            List<Tag> questionTags = question.getTags();
            for (Tag tag : questionTags) {
                AppUserTag appUserTag = appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(userAnswerQuestion.getUserId(), tag.getTagId());

                // Must continue since these AppUserTag have been saved
                if (appUserTag == null) {
                    continue;
                }

                // If found AppUserTag reputation
                if (appUserTag.getReputation() > 0) {
                    appUserTag.setReputation(appUserTag.getReputation() - MARK_ACCEPTED_ANSWER_POINT);
                }

                // Save to database
                appUserTagRepository.save(appUserTag);
            }

            // Mark this answer as accepted answer
            answer.setAccepted(false);
            answer = answerRepository.save(answer);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
