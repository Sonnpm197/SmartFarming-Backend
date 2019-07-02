package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.search.GenericClass;
import com.son.CapstoneProject.repository.*;
import com.son.CapstoneProject.repository.searchRepository.HibernateSearchRepository;
import com.son.CapstoneProject.service.ViewCountingService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/question")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class QuestionController {

    private Logger logger = Logger.getLogger(QuestionController.class.getSimpleName());

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private EditedQuestionRepository editedQuestionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ViewCountingService countingService;

    @Autowired
    private HibernateSearchRepository hibernateSearchRepository;

    private static final int QUESTIONS_PER_PAGE = 2;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
    }

    @GetMapping("/viewNumberOfQuestions")
    public long viewNumberOfQuestions() {
        return questionRepository.count();
    }

    @GetMapping("/viewQuestions/{pageNumber}")
    public Page<Question> viewQuestions(@PathVariable int pageNumber) {
        PageRequest pageNumWithElements = PageRequest.of(pageNumber, QUESTIONS_PER_PAGE, Sort.by("utilTimestamp"));
        return questionRepository.findAll(pageNumWithElements);
    }

    @GetMapping("/viewQuestion/{id}")
    public Question viewQuestion(@PathVariable Long id, HttpServletRequest request) throws Exception {
        String ipAddress = HttpRequestResponseUtils.getClientIpAddress(request);
        // Execute asynchronously
        countingService.countView(id, ipAddress);
        return questionRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));
    }

    @GetMapping("/searchQuestions/{textSearch}")
    public List<Question> searchQuestions(@PathVariable String textSearch) {
        return (List<Question>) hibernateSearchRepository.search2(
                textSearch,
                new GenericClass(Question.class),
                new String[]{"title", "content"} //  fields
        );
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

        controllerUtils.validateAppUser(appUser, methodName, false);

        // If the request contain anonymous = true
        if (appUser.isAnonymous()) {
            String ipAddress = HttpRequestResponseUtils.getClientIpAddress(request);
            logger.info(methodName + ": Anonymous user with ip address: " + ipAddress);
            // To generate Id of that user to allow that comment save to DB
            appUser = controllerUtils.saveOrReturnAnonymousUser(ipAddress);
            question.setAppUser(appUser);
            logger.info(methodName + ": created anonymous user " + appUser);
        } else {
            // If they has logged in
            controllerUtils.validateAppUser(appUser, methodName, true);
        }

        // Save tags first (distinct name)
        List<Tag> tags = controllerUtils.saveDistinctiveTags(question.getTags());
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

        controllerUtils.validateAppUser(appUser, methodName, true);

        // Cannot update other questions
        if (!appUser.getUserId().equals(oldQuestion.getAppUser().getUserId())) {
            String message = methodName + ": You cannot update other user question";
            logger.info(message);
            throw new Exception(message);
        }

        // Save tags first (distinct name)
        List<Tag> tags = controllerUtils.saveDistinctiveTags(updatedQuestion.getTags());
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

        controllerUtils.validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            editedQuestion.setAppUser(appUser);
        } else {
            // validate userId in request
            controllerUtils.validateAppUser(appUser, methodName, true);
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
        List<Tag> tags = controllerUtils.saveDistinctiveTags(editedQuestion.getTags());
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

        controllerUtils.validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
        } else {
            controllerUtils.validateAppUser(appUser, methodName, true);
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

        controllerUtils.validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
        } else {
            controllerUtils.validateAppUser(appUser, methodName, true);
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

    /**
     * Report question
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

        controllerUtils.validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            report.setAppUser(appUser);
        } else {
            controllerUtils.validateAppUser(appUser, methodName, true);
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
}