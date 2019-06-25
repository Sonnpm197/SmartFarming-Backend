package com.son.CapstoneProject.controller.authenticated;

import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.repository.*;
import com.son.CapstoneProject.repository.loginRepository.AppUserDAO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

/**
 * This class is for both admins and clients
 */
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
    private AppUserDAO appUserDAO;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
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
    public Question addQuestion(@RequestBody Question question) throws Exception {

        AppUser appUser = question.getAppUser();

        if (appUser == null) {
            String message = "UserController.addQuestion: Request body has no appUser";
            logger.info(message);
            throw new Exception(message);
        }

        Long userId = appUser.getUserId();

        if (userId == null) {
            String message = "UserController.addQuestion: AppUser from request body has no user id";
            logger.info(message);
            throw new Exception(message);
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
    public ResponseEntity<Question> updateQuestion(@RequestBody Question updatedQuestion, @PathVariable Long id)
            throws Exception {
        Question oldQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new Exception("UserController.updateQuestion: Not found any question with id: " + id));

        AppUser appUser = updatedQuestion.getAppUser();

        if (appUser == null) {
            String message = "UserController.updateQuestion: Request body has no appUser";
            logger.info(message);
            throw new Exception(message);
        }

        Long userId = appUser.getUserId();

        if (userId == null) {
            String message = "UserController.updateQuestion: AppUser from request body has no user id";
            logger.info(message);
            throw new Exception(message);
        }

        // Cannot update other questions
        if (!userId.equals(oldQuestion.getAppUser().getUserId())) {
            String message = "UserController.updateQuestion: You cannot update other questions";
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
    public ResponseEntity<EditedQuestion> editOtherUserQuestion(
            @RequestBody EditedQuestion editedQuestion, @PathVariable Long originalQuestionId) throws Exception {
        Question oldQuestion = questionRepository.findById(originalQuestionId)
                .orElseThrow(() -> new Exception("UserController.editOtherUserQuestion: Not found any question with id: " + originalQuestionId));

        editedQuestion.setQuestion(oldQuestion);
        editedQuestion.setUtilTimestamp(new Date());

        // Also add edited versions to that question
        List<EditedQuestion> editedQuestions = oldQuestion.getEditedQuestions();
        if (editedQuestions == null) {
            editedQuestions = new ArrayList<>();
        }
        editedQuestions.add(editedQuestion);
        oldQuestion.setEditedQuestions(editedQuestions);

        // Save tags first (distinct name)
        List<Tag> tags = saveQuestionTags(editedQuestion.getTags());
        editedQuestion.setTags(tags);

        EditedQuestion editedQuestion1 = editedQuestionRepository.save(editedQuestion);

        // Then save the previous quesion
        questionRepository.save(oldQuestion);

        return ResponseEntity.ok(editedQuestion1);
    }

    /**
     * View all previous edited versions from others
     *
     * @param originalQuestionId
     * @return
     */
    @GetMapping("/viewEditedVersions/{originalQuestionId}")
    public List<EditedQuestion> viewEditedVersions(@PathVariable Long originalQuestionId) {
        try {
            return questionRepository.findById(originalQuestionId).get().getEditedQuestions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Delete a question
     *
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteQuestion/{id}")
    public Map<String, String> deleteQuestion(@RequestBody AppUser appUser, @PathVariable Long id) throws Exception {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new Exception("UserController.deleteQuestion: Not found question with id: " + id));

        // Cannot delete other questions
        if (!appUser.getUserId().equals(question.getAppUser().getUserId())) {
            String message = "UserController.deleteQuestion: You cannot delete other questions";
            logger.info(message);
            throw new Exception(message);
        }

        questionRepository.delete(question);
        Map<String, String> map = new HashMap<>();
        map.put("questionId", "" + id);
        map.put("deleted", "true");
        return map;
    }

    // ============================================================================//

    /**
     * Add answers
     *
     * @param answer
     * @return
     */
    @PostMapping(value = "/addAnswer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Answer addAnswer(@RequestBody Answer answer) throws Exception {

        AppUser appUser = answer.getAppUser();

        if (appUser == null) {
            String message = "UserController.addAnswer: Request body has no appUser";
            logger.info(message);
            throw new Exception(message);
        }

        Long userId = appUser.getUserId();

        if (userId == null) {
            String message = "UserController.addAnswer: AppUser from request body has no user id";
            logger.info(message);
            throw new Exception(message);
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
    @PutMapping("/updateAnswer/{id}")
    public ResponseEntity<Answer> updateAnswer(@RequestBody Answer updatedAnswer, @PathVariable Long id)
            throws Exception {
        Answer oldAnswer = answerRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));

        AppUser appUser = updatedAnswer.getAppUser();

        if (appUser == null) {
            String message = "UserController.updateAnswer: Request body has no appUser";
            logger.info(message);
            throw new Exception(message);
        }

        Long userId = appUser.getUserId();

        if (userId == null) {
            String message = "UserController.updateAnswer: AppUser from request body has no user id";
            logger.info(message);
            throw new Exception(message);
        }

        // Cannot delete other questions
        if (!userId.equals(oldAnswer.getAppUser().getUserId())) {
            String message = "UserController.updateAnswer: You cannot update others' answers";
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
    @DeleteMapping("/deleteAnswer/{id}")
    public Map<String, String> deleteAnswer(@RequestBody AppUser appUser, @PathVariable Long id) throws Exception {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new Exception("UserController.deleteAnswer: Found no answer with id: " + id));

        // Cannot delete other questions
        if (!appUser.getUserId().equals(answer.getAppUser().getUserId())) {
            String message = "UserController.deleteAnswer: You cannot delete others' answers";
            logger.info(message);
            throw new Exception(message);
        }

        answerRepository.delete(answer);
        Map<String, String> map = new HashMap<>();
        map.put("answerId", "" + id);
        map.put("deleted", "true");
        return map;
    }

    /**
     * An user hits like
     *
     * @param questionId
     * @throws Exception
     */
    @PostMapping(value = "/likeQuestion/{questionId}")
    @Transactional
    public Question likeQuestion(@RequestBody AppUser appUser, @PathVariable Long questionId) throws Exception {

        if (appUser == null) {
            String message = "UserController.likeQuestion: Request body has no appUser";
            logger.info(message);
            throw new Exception(message);
        }

        Long userId = appUser.getUserId();

        if (userId == null) {
            String message = "UserController.likeQuestion: AppUser from request body has no user id";
            logger.info(message);
            throw new Exception(message);
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new Exception("UserController.likeQuestion: Not found question by id: " + questionId));

        List<Long> appUserList = question.getUserIdLikedPost();

        if (appUserList == null) {
            appUserList = new ArrayList<>();
            appUserList.add(appUser.getUserId());
        } else {
            // Click like again => dislike
            Iterator<Long> iterator = appUserList.iterator();

            boolean userAlreadyLikedPost = false;
            while (iterator.hasNext()) {
                Long existedId = iterator.next();
                // If they click again means they dislike this
                if (existedId.equals(appUser.getUserId())) {
                    userAlreadyLikedPost = true;

                    // Decrease tag point of that post
                    updateTagPoint(question.getTags(), appUser, -1);

                    // Then remove that userId
                    iterator.remove();
                }
            }

            // He hasn't liked yet
            if (!userAlreadyLikedPost) {

                appUserList.add(appUser.getUserId());

                // Increase tag point of that post
                updateTagPoint(question.getTags(), appUser, 1);
            }
        }

        question.setUserIdLikedPost(appUserList);
        return questionRepository.save(question);
    }

    /**
     * Increase whole tag points
     * Increase reputation of that user
     * Increase specific tag points of that user
     * <p>
     * updatedPoint = +1 => an user hits like
     * updatedPoint = -1 => an user hits like again => dislike
     */
    private void updateTagPoint(List<Tag> tags, AppUser appUser, int updatedPoint) {
        // Increase whole tag points
        for (Tag tag : tags) {
            int totalPoint = tag.getTotalPoint();
            if (totalPoint == 0 && updatedPoint < 0) {
                tag.setTotalPoint(0);
            } else {
                tag.setTotalPoint(totalPoint + updatedPoint);
            }
            tagRepository.save(tag);
        }

        // Increase reputation of that user
        int reputation = appUser.getReputation();
        if (reputation == 0 && updatedPoint < 0) {
            appUserDAO.increaseReputation(appUser.getUserId(), 0);
        } else {
            appUserDAO.increaseReputation(appUser.getUserId(), reputation + updatedPoint);
        }


        // Increase specific tag points of that user
        for (Tag tag : tags) {
            AppUserTag appUserTag = new AppUserTag();
            appUserTag.setAppUser(appUser);
            appUserTag.setTag(tag);
            int totalPoint = appUserTag.getTotalPoint();
            if (totalPoint == 0 && updatedPoint < 0) {
                appUserTag.setTotalPoint(0);
            } else {
                appUserTag.setTotalPoint(appUserTag.getTotalPoint() + updatedPoint);
            }
            appUserTag.setTotalPoint(appUserTag.getTotalPoint() + updatedPoint);
            appUserTagRepository.save(appUserTag);
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
            @RequestBody Report report, @PathVariable Long questionId) throws Exception {

        AppUser appUser = report.getAppUser();

        if (appUser == null) {
            String message = "UserController.reportQuestion: Request body has no appUser";
            logger.info(message);
            throw new Exception(message);
        }

        Long userId = appUser.getUserId();

        if (userId == null) {
            String message = "UserController.reportQuestion: AppUser from request body has no user id";
            logger.info(message);
            throw new Exception(message);
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new Exception("Not found question by id: " + questionId));

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
