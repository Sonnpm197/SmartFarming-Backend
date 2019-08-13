package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ArticleRepository extends PagingAndSortingRepository<Article, Long> {

    List<Article> findAll();

    Page<Article> findAll(Pageable pageable);

    @Query("select sum(a.viewCount) from Article a")
    Integer getTotalViewCount();

    @Query("SELECT DISTINCT a.category FROM Article a")
    List<String> findDistinctCategory();

    List<Article> findTop10ByOrderByUtilTimestampDesc();

    List<Article> findTop10ByOrderByUpvoteCountDesc();

    Page<Article> findByCategory(String category, Pageable pageable);

    @Query("select sum(q.viewCount) from Article q where q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    Integer findTotalViewOfArticlesByUtilTimestampBetween(@Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

    @Query("select sum(q.upvoteCount) from Article q where q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    Integer findTotalUpvoteOfArticlesByUtilTimestampBetween(@Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

    @Query(
            value = "select sum(q.view_count) from Article q where year(q.util_timestamp) = :yearParam and month(q.util_timestamp) = :monthParam",
            nativeQuery = true
    )
    Integer findTotalViewOfArticlesByYearAndMonth(@Param("yearParam") int yearParam, @Param("monthParam") int monthParam);

    @Query(
            value = "select sum(q.upvote_count) from Article q where year(q.util_timestamp) = :yearParam and month(q.util_timestamp) = :monthParam",
            nativeQuery = true
    )
    Integer findTotalUpvoteOfArticlesByYearAndMonth(@Param("yearParam") int yearParam, @Param("monthParam") int monthParam);

    @Query(
            value = "select sum(q.view_count) from Article q where year(q.util_timestamp) = :yearParam",
            nativeQuery = true
    )
    Integer findTotalViewOfArticlesByYear(@Param("yearParam") int yearParam);

    @Query(
            value = "select sum(q.upvote_count) from Article q where year(q.util_timestamp) = :yearParam",
            nativeQuery = true
    )
    Integer findTotalUpvoteOfArticlesByYear(@Param("yearParam") int yearParam);

    // For paging purpose
    Page<Article> findByTags_tagId(Long tagId, Pageable pageable);

    // Count number of article by tagId
    @Query(
            value = "select count(a.article_id) from article a join article_tags ats\n" +
                    "                        on a.article_id = ats.articles_article_id\n" +
                    "where ats.tags_tag_id = :tagId",
            nativeQuery = true
    )
    Integer countNumberOfArticlesByTagId(@Param("tagId") Long tagId);

    Article findTopByTags_tagIdOrderByViewCountDescUpvoteCountDesc(Long tagId);

}
