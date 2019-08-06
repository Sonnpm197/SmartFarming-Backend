package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends PagingAndSortingRepository<Article, Long> {

    Page<Article> findAll(Pageable pageable);

    @Query("select sum(a.viewCount) from Article a")
    Integer getTotalViewCount();

    @Query("SELECT DISTINCT a.category FROM Article a")
    List<String> findDistinctCategory();

    List<Article> findTop10ByOrderByUtilTimestampDesc();

    List<Article> findTop10ByOrderByUpvoteCountDesc();

    Page<Article> findByCategory(String category, Pageable pageable);

}
