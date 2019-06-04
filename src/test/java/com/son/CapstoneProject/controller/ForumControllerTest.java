package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.controller.individualRole.ClientController;
import com.son.CapstoneProject.domain.Question;
import com.son.CapstoneProject.repository.AnswerRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.QuestionSearchRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ForumController.class)
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
public class ForumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private QuestionSearchRepository questionSearchRepository;

    @Test
    public void viewAllAskedQuestions() {
    }

    @Test
    public void searchQuestions() {
        try {
            given(questionRepository.findAll()).willReturn(mockQuestionList());

            mockMvc.perform(get("/forum/viewAll")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Question> mockQuestionList() {
        List<Question> questions = new ArrayList<>();
        Question question = new Question();
        question.setId(123456L);
        question.setTitle("title");
        question.setContent("content");
        questions.add(question);
        return questions;
    }
}