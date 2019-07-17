package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.controller.FileController;
import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.pagination.QuestionPagination;
import com.son.CapstoneProject.entity.search.GenericClass;
import com.son.CapstoneProject.entity.search.QuestionSearch;
import com.son.CapstoneProject.repository.*;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
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

import static com.son.CapstoneProject.common.ConstantValue.EDITED_APPROVE_POINT;
import static com.son.CapstoneProject.common.ConstantValue.QUESTION;
import static com.son.CapstoneProject.common.ConstantValue.QUESTIONS_PER_PAGE;

@RestController
@RequestMapping("/question")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class QuestionController {

    private Logger logger = Logger.getLogger(QuestionController.class.getSimpleName());

    @Autowired
    private FileController fileController;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

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
    private AppUserRepository appUserRepository;

    @Autowired
    private HibernateSearchRepository hibernateSearchRepository;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
    }

    @GetMapping("/viewNumberOfQuestions")
    public long viewNumberOfQuestions() {
        return questionRepository.count();
    }

    @GetMapping("/viewTop3QuestionsByViewCount")
    public QuestionPagination viewTop3QuestionsByViewCount() {
        List<Question> questions = questionRepository.findTop3ByOrderByViewCountDesc();
        QuestionPagination questionPagination = new QuestionPagination();
        questionPagination.setQa(questions);
        questionPagination.setNumberOfPages(1);
        return questionPagination;
    }

    @GetMapping("/viewNumberOfPages")
    public long viewNumberOfPages() {
        long numberOfQuestion = questionRepository.count();
        if (numberOfQuestion % QUESTIONS_PER_PAGE == 0) {
            return numberOfQuestion / QUESTIONS_PER_PAGE;
        } else {
            return (numberOfQuestion / QUESTIONS_PER_PAGE) + 1;
        }
    }

    @GetMapping("/viewQuestions/{pageNumber}")
    public QuestionPagination viewQuestionsByPageIndex(@PathVariable int pageNumber) {
        PageRequest pageNumWithElements = PageRequest.of(pageNumber, QUESTIONS_PER_PAGE, Sort.by("utilTimestamp"));
        Page<Question> questionPage = questionRepository.findAll(pageNumWithElements);
        QuestionPagination questionPagination = new QuestionPagination();
        questionPagination.setQa(questionPage.getContent());
        questionPagination.setNumberOfPages(Integer.parseInt("" + viewNumberOfPages()));
        return questionPagination;
    }

    @GetMapping("/viewQuestion/{id}")
    public Question viewQuestionById(@PathVariable Long id, HttpServletRequest request) throws Exception {
        String ipAddress = HttpRequestResponseUtils.getClientIpAddress(request);
        // Execute asynchronously
        countingService.countView(id, ipAddress, QUESTION);
        return questionRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));
    }

    @PostMapping("/searchQuestions/{pageNumber}")
    public QuestionPagination searchQuestions(@RequestBody QuestionSearch questionSearch, @PathVariable int pageNumber) {
        return (QuestionPagination) hibernateSearchRepository.search2(questionSearch.getTextSearch(),
                QUESTION,
                new String[]{"title", "content"},
                null,
                pageNumber);
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
        question = questionRepository.save(question);

        // Note: this uploaded file are already saved on GG Cloud
        // This requested question will have UploadedFile objects => save info of this question to that UploadedFile
        List<UploadedFile> uploadedFiles = question.getUploadedFiles();

        if (uploadedFiles != null) {
            for (UploadedFile uploadedFile : uploadedFiles) {
                // We still need to save question for this uploaded file
                uploadedFile.setQuestion(question);
                uploadedFileRepository.save(uploadedFile);
            }
        }

        return question;
    }

    /**
     * Update a question
     *
     * @param updatedQuestion
     * @return
     * @throws Exception
     */
    @PutMapping("/updateQuestion/{questionId}")
    @Transactional
    public ResponseEntity<Question> updateQuestion(
            @RequestBody Question updatedQuestion,
            @PathVariable Long questionId)
            throws Exception {

        String methodName = "UserController.updateQuestion";

        Question oldQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new Exception(methodName + ": Not found any question with id: " + questionId));

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

        // Delete old images from DB and delete file on google cloud storage
        List<UploadedFile> oldUploadedFiles = oldQuestion.getUploadedFiles();
        for (UploadedFile oldUploadedFile: oldUploadedFiles) {
            fileController.deleteFile(oldUploadedFile);
        }

        // This requested question will have UploadedFile objects => save info of this question to that UploadedFile
        List<UploadedFile> newUploadedFiles = updatedQuestion.getUploadedFiles();

        // Set new uploaded file
        oldQuestion.setUploadedFiles(newUploadedFiles);
        Question resultQuestion = questionRepository.save(oldQuestion);

        // Set question_id for these new uploaded files
        if (newUploadedFiles != null) {
            for (UploadedFile uploadedFile : newUploadedFiles) {
                uploadedFile.setQuestion(resultQuestion);
                uploadedFileRepository.save(uploadedFile);
            }
        }

        return ResponseEntity.ok(resultQuestion);
    }

    /**
     * Delete a question
     *
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteQuestion/{questionId}")
    @Transactional
    public Map<String, String> deleteQuestion(/*@RequestBody AppUser appUser,*/
                                              @PathVariable Long questionId,
                                              HttpServletRequest request) throws Exception {

        String methodName = "UserController.deleteQuestion";

//        controllerUtils.validateAppUser(appUser, methodName, false);
//
//        if (appUser.isAnonymous()) {
//            appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
//        } else {
//            controllerUtils.validateAppUser(appUser, methodName, true);
//        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new Exception(methodName + ": Not found question with id: " + questionId));

        // Cannot delete other questions
//        if (!appUser.getUserId().equals(question.getAppUser().getUserId())) {
//            String message = methodName + ": You cannot delete other questions";
//            logger.info(message);
//            throw new Exception(message);
//        }

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

            // Remove the comment of the answer first
            List<Comment> commentsOfAnswer = commentRepository.findByAnswer_AnswerId(answer.getAnswerId());
            for (Comment comment: commentsOfAnswer) {
                commentRepository.delete(comment);
            }

            // Then delete the answer
            answerRepository.delete(answer);
        }

        // Delete UploadedFile both from GG cloud and DB
        List<UploadedFile> uploadedFiles = question.getUploadedFiles();
        for (UploadedFile uploadedFile: uploadedFiles) {
            fileController.deleteFile(uploadedFile);
        }

        // Delete the reports of this question
        List<Report> reports = question.getReports();
        for (Report report: reports) {
            reportRepository.delete(report);
        }

        // Then remove the question
        questionRepository.delete(question);

        Map<String, String> map = new HashMap<>();
        map.put("questionId", "" + questionId);
        map.put("deleted", "true");
        return map;
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
     * View all previous edited versions from other users (on your question only)
     * <p>
     * Delete this edited version
     * <p>
     * Count point for the one who edited your post
     *
     * @return
     */
    @PostMapping("/approveEditedVersion/{originalQuestionId}")
    @Transactional
    public Question approveEditedVersion(
            @RequestBody EditedQuestion editedQuestion,
            @PathVariable Long originalQuestionId) throws Exception {

        String methodName = "UserController.approveEditedVersion";

        if (editedQuestion.getEditedQuestionId() == null) {
            String message = methodName + "You must include edited question id";
            logger.info(message);
            throw new Exception(message);
        }

        Question question = questionRepository.findById(originalQuestionId).
                orElseThrow(() -> new Exception(methodName + ": cannot find any originalQuestion with id: " + originalQuestionId));

        EditedQuestion editedQuestionFullData = editedQuestionRepository.findById(editedQuestion.getEditedQuestionId()).
                orElseThrow(() -> new Exception(methodName + ": cannot find any edited question with id: " + editedQuestion.getEditedQuestionId()));

        AppUser userCreatedQuestion = question.getAppUser();
        AppUser userEditedQuestion = editedQuestion.getAppUser();

        // If this is not your question
        if (!userCreatedQuestion.getUserId().equals(userEditedQuestion.getUserId())) {
            String message = methodName + "You cannot approve your own edited question versions";
            logger.info(message);
            throw new Exception(message);
        }

        // Remove edited version
        editedQuestionRepository.delete(editedQuestionFullData);

        // The user who edited receive reputation if the author approves
        userEditedQuestion.setReputation(userEditedQuestion.getReputation() + EDITED_APPROVE_POINT);
        appUserRepository.save(userEditedQuestion);

        // Save tags first (distinct name)
        List<Tag> tags = controllerUtils.saveDistinctiveTags(editedQuestion.getTags());

        // Update original question
        question.setTitle(editedQuestion.getTitle());
        question.setContent(editedQuestion.getContent());
        question.setTags(tags);
        question.setUtilTimestamp(new Date());
        return questionRepository.save(question);
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
        report.setUtilTimestamp(new Date());
        return reportRepository.save(report);
    }
}
