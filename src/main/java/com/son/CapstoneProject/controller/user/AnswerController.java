package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.entity.Answer;
import com.son.CapstoneProject.entity.Comment;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.repository.AnswerRepository;
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
     * @param id
     * @return
     * @throws Exception
     */
    @PutMapping("/updateAnswerToQuestion/{id}")
    @Transactional
    public ResponseEntity<Answer> updateAnswerToQuestion(@RequestBody Answer updatedAnswer,
                                                         @PathVariable Long id,
                                                         HttpServletRequest request)
            throws Exception {

        String methodName = "UserController.updateAnswerToQuestion";

        Answer oldAnswer = answerRepository.findById(id)
                .orElseThrow(() -> new Exception(methodName + ": Not found any answers with id: " + id));

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
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteAnswerToQuestion/{id}")
    @Transactional
    public Map<String, String> deleteAnswerToQuestion(@RequestBody AppUser appUser,
                                                      @PathVariable Long id,
                                                      HttpServletRequest request) throws Exception {

        String methodName = "UserController.deleteAnswerToQuestion";

        controllerUtils.validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
        } else {
            controllerUtils.validateAppUser(appUser, methodName, true);
        }

        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new Exception(methodName + ": Found no answer with id: " + id));

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
        map.put("answerId", "" + id);
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

        Question question = questionRepository.findById(answerId)
                .orElseThrow(() -> new Exception(methodName + ": Not found any questions with id: " + questionId));

        controllerUtils.validateAppUser(questionAuthor, methodName, true);

        // Check if you are the author
        if (!questionAuthor.getUserId().equals(question.getAppUser().getUserId())) {
            String message = methodName + ": You cannot mark accepted answer if you are not the author of the question";
            logger.info(message);
            throw new Exception(message);
        }

        // Increase reputation point for author of the answer
        AppUser userAnswerQuestion = answer.getAppUser();
        userAnswerQuestion.setReputation(userAnswerQuestion.getReputation() + ConstantValue.MARK_ACCEPTED_ANSWER_POINT);
        appUserRepository.save(userAnswerQuestion);

        // Mark this answer as accepted answer
        answer.setAccepted(true);
        answer = answerRepository.save(answer);
        return ResponseEntity.ok(answer);
    }
}
