package com.son.CapstoneProject.entity.pagination;

import com.son.CapstoneProject.entity.Article;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ArticlePagination extends Pagination {

    private List<Article> articlesByPageIndex = new ArrayList<>();

}
