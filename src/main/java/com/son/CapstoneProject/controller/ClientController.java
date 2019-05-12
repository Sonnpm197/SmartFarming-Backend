package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.domain.Question;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.QuestionSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/client")
// Clients (users) are able to view asked questions, ask, update, and delete questions
public class ClientController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionSearchRepository questionSearchRepository;

    @GetMapping("/viewQuestions/{clientEmail}")
    public List<Question> viewAskedQuestions(@PathVariable String clientEmail) {
        return questionRepository.getQuestionsByClient_Email(clientEmail);
    }

    @GetMapping("/searchQuestions/{textSearch}")
    public List<Question> searchQuestions(@PathVariable String textSearch) {
        return questionSearchRepository.searchQuestions(textSearch);
    }

    // User ask a question
    @PostMapping(value = "/addQuestion",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Question addQuestion(@RequestBody Question question) {
        return questionRepository.save(question);
    }

    // User update a question
    @PutMapping("/updateQuestion/{id}")
    public ResponseEntity<Question> updateQuestion(@RequestBody Question updatedQuestion, @PathVariable Long id)
            throws Exception {
        Question oldQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));
        oldQuestion.setContent(updatedQuestion.getContent());
        Question question = questionRepository.save(oldQuestion);
        return ResponseEntity.ok(question);
    }

    // User delete a question
    @DeleteMapping("/deleteQuestion/{id}")
    public Map<String, Boolean> deleteQuestion(@PathVariable Long id) throws Exception {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found to delete"));
        questionRepository.delete(question);
        Map<String, Boolean> map = new HashMap<>();
        map.put("deleted", Boolean.TRUE);
        return map;
    }
}
