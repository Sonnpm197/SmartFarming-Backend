package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends PagingAndSortingRepository<Tag, Long> {

    List<Tag> findAll();

    Tag findByName(String name);

    Page<Tag> findAll(Pageable pageable);

    Page<Tag> findAllByOrderByViewCountDesc(Pageable pageable);

    Page<Tag> findAllByOrderByReputationDesc(Pageable pageable);

    Page<Tag> findAllByOrderByIncreasementOneWeekAgoTillNowDesc(Pageable pageable);

    List<Tag> findTop5ByOrderByViewCountDesc();

    List<Tag> findTop10ByOrderByViewCountDesc();

    List<Tag> findTop10ByOrderByReputationDesc();

    List<Tag> findByArticles_articleId(Long articleId);

    List<Tag> findByQuestions_questionId(Long questionId);

    @Query(
            value = "select t1.totalQuestionView, t2.totalArticleView\n" +
                    "from (select sum(q.view_count) as 'totalQuestionView'\n" +
                    "      from question q\n" +
                    "             join question_tags qt on q.question_id = qt.questions_question_id\n" +
                    "      where q.util_timestamp <= :utilTimeStamp and qt.tags_tag_id = :tagId) as t1,\n" +
                    "     (select sum(a.view_count) as 'totalArticleView'\n" +
                    "      from article a\n" +
                    "             join article_tags ats on a.article_id = ats.articles_article_id\n" +
                    "      where a.util_timestamp <= :utilTimeStamp and ats.tags_tag_id = :tagId) as t2",
            nativeQuery = true
    )
    List<Object[]> countTotalQuestionViewAndArticleViewBeforeDate(@Param("utilTimeStamp") String utilTimeStamp, @Param("tagId") Long tagId);
}
