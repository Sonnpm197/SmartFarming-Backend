package com.son.CapstoneProject.entity.pagination;

import com.son.CapstoneProject.entity.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionPagination {

    private List<Question> Qa;
    private int numberOfPages;

}
