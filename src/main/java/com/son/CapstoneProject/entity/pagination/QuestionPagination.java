package com.son.CapstoneProject.entity.pagination;

import com.son.CapstoneProject.entity.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuestionPagination extends Pagination {

    private List<Question> Qa = new ArrayList<>();

}
