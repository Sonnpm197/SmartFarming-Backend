package com.son.CapstoneProject.controller.authenticated;

import com.son.CapstoneProject.domain.Answer;
import com.son.CapstoneProject.domain.Question;
import com.son.CapstoneProject.repository.AnswerRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is for both admins and clients
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    // This repository is for users to add, update, and delete questions
    @Autowired
    private QuestionRepository questionRepository;

    // This repository is for users to add, update, and delete answers
    @Autowired
    private AnswerRepository answerRepository;

    /**
     * Add a question
     * @param question
     * @return
     */
    @PostMapping(value = "/addQuestion",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Question addQuestion(@RequestBody Question question) {
        return questionRepository.save(question);
    }

    /**
     * Update a question
     * @param updatedQuestion
     * @param id
     * @return
     * @throws Exception
     */
    @PutMapping("/updateQuestion/{id}")
    public ResponseEntity<Question> updateQuestion(@RequestBody Question updatedQuestion, @PathVariable Long id)
            throws Exception {
        Question oldQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));

        // Update values
        oldQuestion.setTitle(updatedQuestion.getTitle());
        oldQuestion.setContent(updatedQuestion.getContent());

        Question question = questionRepository.save(oldQuestion);
        return ResponseEntity.ok(question);
    }

    /**
     * Delete a question
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteQuestion/{id}")
    public Map<String, Boolean> deleteQuestion(@PathVariable Long id) throws Exception {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found to delete"));
        questionRepository.delete(question);
        Map<String, Boolean> map = new HashMap<>();
        map.put("deleteQuestion", Boolean.TRUE);
        return map;
    }

    // ============================================================================//

    /**
     * Add answers
     * @param answer
     * @return
     */
    @PostMapping(value = "/addAnswer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Answer addAnswer(@RequestBody Answer answer) {
        return answerRepository.save(answer);
    }

    /**
     * Update answer
     * @param updatedQuestion
     * @param id
     * @return
     * @throws Exception
     */
    @PutMapping("/updateAnswer/{id}")
    public ResponseEntity<Answer> updateAnswer(@RequestBody Answer updatedQuestion, @PathVariable Long id)
            throws Exception {
        Answer oldAnswer = answerRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));
        oldAnswer.setContent(updatedQuestion.getContent());
        Answer answer = answerRepository.save(oldAnswer);
        return ResponseEntity.ok(answer);
    }

    /**
     * Delete answer
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteAnswer/{id}")
    public Map<String, Boolean> deleteAnswer(@PathVariable Long id) throws Exception {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found to delete"));
        answerRepository.delete(answer);
        Map<String, Boolean> map = new HashMap<>();
        map.put("deleteAnswer", Boolean.TRUE);
        return map;
    }
}
