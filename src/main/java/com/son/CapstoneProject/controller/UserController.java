package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.login.AppRole;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.form.AppUserForm;
import com.son.CapstoneProject.repository.*;
import com.son.CapstoneProject.repository.loginRepository.AppUserDAO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class UserController {

    private Logger logger = Logger.getLogger(UserController.class.getSimpleName());

    // This repository is for users to add, update, and delete questions
    @Autowired
    private QuestionRepository questionRepository;

    // This repository is for users to add, update, and delete questions
    @Autowired
    private EditedQuestionRepository editedQuestionRepository;

    // This repository is for users to add, update, and delete answers
    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppUserDAO appUserDAO;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
    }

    /**
     * Save new anonymous user based on his ip address or retrieve existed one
     *
     * @param ipAddress
     * @return
     */
    public AppUser saveOrReturnAnonymousUser(String ipAddress) {
        AppUserForm myForm = new AppUserForm();
        myForm.setPassword("defaultPassword");
        myForm.setAnonymous(true);
        myForm.setIpAddress(ipAddress);

        List<String> roleNames = new ArrayList<>();
        // By default every user has this role
        roleNames.add(AppRole.ROLE_USER);

        // Check if this anonymous user existed
        AppUser appUserByIpAddress = appUserRepository.findByIpAddress(ipAddress);
        if (appUserByIpAddress != null) {
            return appUserByIpAddress;
        }

        try {
            return appUserDAO.registerNewUserAccount(myForm, roleNames);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void validateAppUser(AppUser appUser, String methodName, boolean checkUserId) throws Exception {
        if (appUser == null) {
            String message = methodName + ": Request body has no appUser";
            logger.info(message);
            throw new Exception(message);
        }

        if (checkUserId) {
            if (appUser.getUserId() == null) {
                String message = methodName + ": AppUser from request body has no user id";
                logger.info(message);
                throw new Exception(message);
            }
        }
    }

    /**
     * Add a question
     *
     * @param question
     * @return
     */
    @PostMapping(value = "/addQuestion",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public Question addQuestion(@RequestBody Question question, HttpServletRequest request) throws Exception {

        String methodName = "UserController.addQuestion";

        AppUser appUser = question.getAppUser();

        validateAppUser(appUser, methodName, false);

        // If the request contain anonymous = true
        if (appUser.isAnonymous()) {
            String ipAddress = HttpRequestResponseUtils.getClientIpAddress(request);
            logger.info(methodName + ": Anonymous user with ip address: " + ipAddress);
            // To generate Id of that user to allow that comment save to DB
            appUser = saveOrReturnAnonymousUser(ipAddress);
            question.setAppUser(appUser);
            logger.info(methodName + ": created anonymous user " + appUser);
        } else {
            // If they has logged in
            validateAppUser(appUser, methodName, true);
        }

        // Save tags first (distinct name)
        List<Tag> tags = saveQuestionTags(question.getTags());
        question.setTags(tags);

        // add date
        question.setUtilTimestamp(new Date());
        return questionRepository.save(question);
    }

    /**
     * Update a question
     *
     * @param updatedQuestion
     * @param id
     * @return
     * @throws Exception
     */
    @PutMapping("/updateQuestion/{id}")
    @Transactional
    public ResponseEntity<Question> updateQuestion(
            @RequestBody Question updatedQuestion,
            @PathVariable Long id)
            throws Exception {

        String methodName = "UserController.updateQuestion";

        Question oldQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new Exception(methodName + ": Not found any question with id: " + id));

        AppUser appUser = updatedQuestion.getAppUser();

        validateAppUser(appUser, methodName, true);

        // Cannot update other questions
        if (!appUser.getUserId().equals(oldQuestion.getAppUser().getUserId())) {
            String message = methodName + ": You cannot update other user question";
            logger.info(message);
            throw new Exception(message);
        }

        // Save tags first (distinct name)
        List<Tag> tags = saveQuestionTags(updatedQuestion.getTags());
        updatedQuestion.setTags(tags);

        // Update values
        oldQuestion.setTitle(updatedQuestion.getTitle());
        oldQuestion.setContent(updatedQuestion.getContent());
        oldQuestion.setTags(tags);

        Question question = questionRepository.save(oldQuestion);
        return ResponseEntity.ok(question);
    }

    /**
     * Edit question of other users
     *
     * @param editedQuestion
     * @param originalQuestionId
     * @return
     * @throws Exception
     */
    @PutMapping("/editOtherUserQuestion/{originalQuestionId}")
    @Transactional
    public ResponseEntity<EditedQuestion> editOtherUserQuestion(
            @RequestBody EditedQuestion editedQuestion,
            @PathVariable Long originalQuestionId,
            HttpServletRequest request) throws Exception {

        String methodName = "UserController.editOtherUserQuestion";

        Question oldQuestion = questionRepository.findById(originalQuestionId)
                .orElseThrow(() -> new Exception(methodName + ": Not found any question with id: " + originalQuestionId));

        // Check if this is anonymous user or not
        AppUser appUser = editedQuestion.getAppUser();

        validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            editedQuestion.setAppUser(appUser);
        } else {
            // validate userId in request
            validateAppUser(appUser, methodName, true);
        }

        // Cannot edit your question (this function is to edit other user question)
        if (appUser.getUserId().equals(oldQuestion.getAppUser().getUserId())) {
            String message = methodName + ": This method is to edit other user question. Please do not edit your question";
            logger.info(message);
            throw new Exception(message);
        }

        editedQuestion.setQuestion(oldQuestion);
        editedQuestion.setUtilTimestamp(new Date());

        // Also add edited versions to that question
        List<EditedQuestion> listEditedQuestions = oldQuestion.getEditedQuestions();
        if (listEditedQuestions == null) {
            listEditedQuestions = new ArrayList<>();
        }
        listEditedQuestions.add(editedQuestion);
        oldQuestion.setEditedQuestions(listEditedQuestions);
        // ================================================

        // Save tags first (distinct name)
        List<Tag> tags = saveQuestionTags(editedQuestion.getTags());
        editedQuestion.setTags(tags);

        EditedQuestion savedEditedQuestion = editedQuestionRepository.save(editedQuestion);

        // Then save the previous quesion
        questionRepository.save(oldQuestion);

        return ResponseEntity.ok(savedEditedQuestion);
    }

    /**
     * View all previous edited versions from other users (on your question only)
     *
     * @param originalQuestionId
     * @return
     */
    @PostMapping("/viewEditedVersions/{originalQuestionId}")
    @Transactional
    public List<EditedQuestion> viewEditedVersions(
            @RequestBody AppUser appUser,
            @PathVariable Long originalQuestionId,
            HttpServletRequest request) throws Exception {

        String methodName = "UserController.viewEditedVersions";

        validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
        } else {
            validateAppUser(appUser, methodName, true);
        }

        Question question = questionRepository.findById(originalQuestionId).get();

        // If this is not your question
        if (!appUser.getUserId().equals(question.getAppUser().getUserId())) {
            String message = methodName + "You cannot view other edited question versions";
            logger.info(message);
            throw new Exception(message);
        }

        return question.getEditedQuestions();
    }

    /**
     * Delete a question
     *
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteQuestion/{id}")
    @Transactional
    public Map<String, String> deleteQuestion(@RequestBody AppUser appUser,
                                              @PathVariable Long id,
                                              HttpServletRequest request) throws Exception {

        String methodName = "UserController.deleteQuestion";

        validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
        } else {
            validateAppUser(appUser, methodName, true);
        }

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new Exception(methodName + ": Not found question with id: " + id));

        // Cannot delete other questions
        if (!appUser.getUserId().equals(question.getAppUser().getUserId())) {
            String message = methodName + ": You cannot delete other questions";
            logger.info(message);
            throw new Exception(message);
        }

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
            answerRepository.delete(answer);
        }

        // Then remove the question
        questionRepository.delete(question);

        Map<String, String> map = new HashMap<>();
        map.put("questionId", "" + id);
        map.put("deleted", "true");
        return map;
    }

    // ============================================================================ //

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

        validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            answer.setAppUser(appUser);
        } else {
            validateAppUser(appUser, methodName, true);
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

        validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            updatedAnswer.setAppUser(appUser);
        } else {
            validateAppUser(appUser, methodName, true);
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

        validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
        } else {
            validateAppUser(appUser, methodName, true);
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

    // ============================= Add/update/delete comments to article =================================== //

    /**
     * This method can add comment to all articles, questions and answers
     *
     * @param comment
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/addComment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public Comment addComment(@RequestBody Comment comment, HttpServletRequest request) throws Exception {

        String methodName = "UserController.addComment";

        AppUser appUser = comment.getAppUser();

        validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            comment.setAppUser(appUser);
        } else {
            validateAppUser(appUser, methodName, true);
        }

        comment.setUtilTimestamp(new Date());
        return commentRepository.save(comment);
    }

    @PutMapping("/updateComment/{id}")
    @Transactional
    public ResponseEntity<Comment> updateComment(@RequestBody Comment updatedComment,
                                                 @PathVariable Long id,
                                                 HttpServletRequest request)
            throws Exception {

        String methodName = "UserController.updateComment";

        Comment oldComment = commentRepository.findById(id)
                .orElseThrow(() -> new Exception(methodName + ": Not found any article with id: " + id));

        AppUser appUser = updatedComment.getAppUser();

        validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            updatedComment.setAppUser(appUser);
        } else {
            validateAppUser(appUser, methodName, true);
        }

        // Cannot update other comment
        if (!appUser.getUserId().equals(oldComment.getAppUser().getUserId())) {
            String message = "UserController.updateComment: You cannot update others' comments";
            logger.info(message);
            throw new Exception(message);
        }

        oldComment.setContent(updatedComment.getContent());
        return ResponseEntity.ok(commentRepository.save(oldComment));
    }

    /**
     * Delete comment
     *
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteComment/{id}")
    @Transactional
    public Map<String, String> deleteComment(@RequestBody AppUser appUser,
                                             @PathVariable Long id,
                                             HttpServletRequest request) throws Exception {

        String methodName = "UserController.deleteComment";

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new Exception(methodName + ": Found no answer with id: " + id));

        validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            comment.setAppUser(appUser);
        } else {
            validateAppUser(appUser, methodName, true);
        }

        // Cannot delete other questions
        if (!appUser.getUserId().equals(comment.getAppUser().getUserId())) {
            String message = methodName + ": You cannot delete others' comment";
            logger.info(message);
            throw new Exception(message);
        }

        commentRepository.delete(comment);

        Map<String, String> map = new HashMap<>();
        map.put("commentId", "" + id);
        map.put("deleted", "true");
        return map;
    }

    // ============================================================================ //

    private static final int LIKE_POINT = 1;
    private static final int UNLIKE_POINT = -1;

    /**
     * An user hits like
     *
     * @param questionId
     * @throws Exception
     */
    @PostMapping(value = "/likeQuestion/{questionId}")
    @Transactional
    public Question likeQuestion(@RequestBody AppUser userLikesQuestion,
                                 @PathVariable Long questionId,
                                 HttpServletRequest request) throws Exception {

        String methodName = "UserController.likeQuestion";

        validateAppUser(userLikesQuestion, methodName, false);

        if (userLikesQuestion.isAnonymous()) {
            userLikesQuestion = saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
        } else {
            validateAppUser(userLikesQuestion, methodName, true);
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new Exception("UserController.likeQuestion: Not found question by id: " + questionId));

        AppUser questionAuthor = question.getAppUser();

        // You cannot like your own question
        if (userLikesQuestion.getUserId().equals(questionAuthor.getUserId())) {
            String message = methodName + ": You cannot like our own question";
            logger.info(message);
            throw new Exception(message);
        }

        // Increase 1 reputation for the one who receives your like
        List<Long> appUserList = question.getUserIdLikedPost();

        if (appUserList == null) {
            appUserList = new ArrayList<>();
            appUserList.add(userLikesQuestion.getUserId());
            // Decrease tag point of that post
            updateReputation(question.getTags(), questionAuthor, LIKE_POINT);
        } else {
            // Click like again => dislike
            Iterator<Long> iterator = appUserList.iterator();

            boolean userAlreadyLikedPost = false;
            while (iterator.hasNext()) {
                Long existedId = iterator.next();
                // If they click again means they dislike this
                if (existedId.equals(userLikesQuestion.getUserId())) {
                    userAlreadyLikedPost = true;

                    // Decrease tag point of that post
                    updateReputation(question.getTags(), questionAuthor, UNLIKE_POINT);

                    // Then remove that userId
                    iterator.remove();
                }
            }

            // He hasn't liked yet
            if (!userAlreadyLikedPost) {

                appUserList.add(userLikesQuestion.getUserId());

                // Increase tag point of that post
                updateReputation(question.getTags(), questionAuthor, LIKE_POINT);
            }
        }

        question.setUserIdLikedPost(appUserList);
        return questionRepository.save(question);
    }

    /**
     * This method is to increase question's author reputation including:
     *
     * Increase whole tag points
     * Increase reputation of that user
     * Increase specific tag points of that user
     * <p>
     * updatedPoint = +1 => an user hits like
     * updatedPoint = -1 => an user hits like again => dislike
     */
    private void updateReputation(List<Tag> tags, AppUser appUser, int updatedPoint) {
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
            appUser.setReputation(appUser.getReputation() + updatedPoint);
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

    /**
     * An user hits like
     *
     * @param questionId
     * @throws Exception
     */
    @PostMapping(value = "/reportQuestion/{questionId}")
    @Transactional
    public Report reportQuestion(
            @RequestBody Report report,
            @PathVariable Long questionId,
            HttpServletRequest request) throws Exception {

        String methodName = "UserController.reportQuestion";

        AppUser appUser = report.getAppUser();

        validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            report.setAppUser(appUser);
        } else {
            validateAppUser(appUser, methodName, true);
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new Exception(methodName + ": Not found question by id: " + questionId));

        // You cannot report your own question
        if (appUser.getUserId().equals(question.getAppUser().getUserId())) {
            String message = methodName + ": You cannot report our own question";
            logger.info(message);
            throw new Exception(message);
        }

        report.setQuestion(question);
        reportRepository.save(report);

        return report;
    }

    private List<Tag> saveQuestionTags(List<Tag> tags) {
        List<Tag> processedList = new ArrayList<>();
        if (tags != null) {
            for (Tag tag : tags) {
                tag.setName(tag.getName().toLowerCase().trim());
                tag.setDescription(tag.getDescription().toLowerCase().trim());

                // Do not save if that tag existed
                if (tagRepository.findByName(tag.getName()).size() > 0) {
                    continue;
                }

                processedList.add(tag);
                tagRepository.save(tag);
            }
        }

        return processedList;
    }
}
