package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.repository.AnswerRepository;
import com.son.CapstoneProject.repository.AppUserTagRepository;
import com.son.CapstoneProject.repository.CommentRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.son.CapstoneProject.common.ConstantValue.MARK_ACCEPTED_ANSWER_POINT;

@RestController
@RequestMapping("/answer")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class AnswerController {

    private Logger logger = Logger.getLogger(AnswerController.class.getSimpleName());

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
    public Answer addAnswerToQuestion(@RequestBody Answer answer, HttpServletRequest request) throws Exception {

        String methodName = "UserController.addAnswerToQuestion";

        AppUser appUser = answer.getAppUser();

        controllerUtils.validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            answer.setAppUser(appUser);
        } else {
            controllerUtils.validateAppUser(appUser, methodName, true);
        }

        answer.setUtilTimestamp(new Date());
        return answerRepository.save(answer);
    }

    /**
     * Update answer
     *
     * @param updatedAnswer
     * @param answerId
     * @return
     * @throws Exception
     */
    @PutMapping("/updateAnswerToQuestion/{answerId}")
    @Transactional
    public ResponseEntity<Answer> updateAnswerToQuestion(@RequestBody Answer updatedAnswer,
                                                         @PathVariable Long answerId,
                                                         HttpServletRequest request)
            throws Exception {

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
            logger.info(message);
            throw new Exception(message);
        }

        oldAnswer.setContent(updatedAnswer.getContent());
        Answer answer = answerRepository.save(oldAnswer);
        return ResponseEntity.ok(answer);
    }

    /**
     * Delete answer
     *
     * @param answerId
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteAnswerToQuestion/{answerId}")
    @Transactional
    public Map<String, String> deleteAnswerToQuestion(@RequestBody AppUser appUser,
                                                      @PathVariable Long answerId,
                                                      HttpServletRequest request) throws Exception {

        String methodName = "UserController.deleteAnswerToQuestion";

        controllerUtils.validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
        } else {
            controllerUtils.validateAppUser(appUser, methodName, true);
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new Exception(methodName + ": Found no answer with id: " + answerId));

        // Cannot delete other questions
        if (!appUser.getUserId().equals(answer.getAppUser().getUserId())) {
            String message = methodName + ": You cannot delete others' answers";
            logger.info(message);
            throw new Exception(message);
        }

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
    }

    /**
     * Mark this answer is accepted (only used by the author)
     *
     * @return
     * @throws Exception
     */
    @PutMapping("/markAcceptedAnswerToQuestion/{questionId}/{answerId}")
    @Transactional
    public ResponseEntity<Answer> markAcceptedAnswerToQuestion(@RequestBody AppUser questionAuthor,
                                                               @PathVariable Long questionId,
                                                               @PathVariable Long answerId)
            throws Exception {

        String methodName = "UserController.markAcceptedAnswerToQuestion";

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new Exception(methodName + ": Not found any answers with id: " + answerId));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new Exception(methodName + ": Not found any questions with id: " + questionId));

        controllerUtils.validateAppUser(questionAuthor, methodName, true);

        // Check if you are the author
        if (questionAuthor.getUserId().equals(answer.getAppUser().getUserId())) {
            String message = methodName + ": You cannot mark accepted answer if you are not the author of the question";
            logger.info(message);
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
    }

    /**
     * Unmark this answer is accepted (only used by the author)
     *
     * @return
     * @throws Exception
     */
    @PutMapping("/unmarkAcceptedAnswerToQuestion/{questionId}/{answerId}")
    @Transactional
    public ResponseEntity<Answer> unmarkAcceptedAnswerToQuestion(@RequestBody AppUser questionAuthor,
                                                                 @PathVariable Long questionId,
                                                                 @PathVariable Long answerId)
            throws Exception {

        String methodName = "UserController.unmarkAcceptedAnswerToQuestion";

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new Exception(methodName + ": Not found any answers with id: " + answerId));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new Exception(methodName + ": Not found any questions with id: " + questionId));

        controllerUtils.validateAppUser(questionAuthor, methodName, true);

        // Check if you are the author
        if (questionAuthor.getUserId().equals(answer.getAppUser().getUserId())) {
            String message = methodName + ": You cannot mark accepted answer if you are not the author of the question";
            logger.info(message);
            throw new Exception(message);
        }

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
    }
}
