package com.son.CapstoneProject.repository.searchRepository;

import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.pagination.ArticlePagination;
import com.son.CapstoneProject.entity.pagination.Pagination;
import com.son.CapstoneProject.entity.pagination.QuestionPagination;
import com.son.CapstoneProject.entity.pagination.TagPagination;
import com.son.CapstoneProject.entity.search.GenericClass;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.hibernate.Cache;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Index;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.son.CapstoneProject.common.ConstantValue.*;

@Repository
public class HibernateSearchRepository {

    //    @PersistenceContext allows you to specify which persistence unit you want to use.
//    Your project might have multiple data sources connected to different DBs
//    and @PersistenceContext allows you to say which one you want to operate on
    @Autowired
    private EntityManager entityManager;

    private FullTextQuery getJpaQuery(org.apache.lucene.search.Query luceneQuery, GenericClass genericClass) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        return fullTextEntityManager.createFullTextQuery(luceneQuery, genericClass.getMyType());
    }

    private QueryBuilder getQueryBuilder(GenericClass genericClass) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        return fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(genericClass.getMyType())
                .get();
    }

    /*
     * it relieves the programmer of having to use transaction.begin() and commit(). If you
     * have a method that calls two DAO methods which normally would each have a transaction.begin and
     * transaction.commit encompassing the real operations and call them it would result in two transactions
     * ( and there might be rollback issues if the previous dao method was supposed to be rolled back too).
     * But if you use @transactional on your method then al those DAO calls will be wrapped in a single begin()-
     * commit() cycle. Of course in case you use @transactional the DAOs must not use the begin() and commit() methods
     * */
//    @Transactional
//    public List search(String searchedText, GenericClass genericClass, String[] fields) {
//        try {
//            org.apache.lucene.search.Query luceneQuery = getQueryBuilder(genericClass)
//                    .keyword()
//                    .onFields(fields) // list to vararg
//                    .matching(searchedText)
//                    .createQuery();
//
//            FullTextQuery jpaFullTextQuery = getJpaQuery(luceneQuery, genericClass);
//
//            return jpaFullTextQuery.getResultList();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return new ArrayList<>();
//    }

    /**
     * This method is used to search for title or content
     * Example: cay sen -> search "cay" AND "sen"
     * <p>
     * if start with double quotes => search exactly
     *
     * @param searchedText
     * @return
     */

    public Pagination search2(String searchedText, String className, String[] fields, String articleCategory, int pageIndex) {
        GenericClass genericClass = null;

        if (ARTICLE.equalsIgnoreCase(className)) {
            genericClass = new GenericClass(Article.class);
        } else if (QUESTION.equalsIgnoreCase(className)) {
            genericClass = new GenericClass(Question.class);
        } else if (TAG.equalsIgnoreCase(className)) {
            genericClass = new GenericClass(Tag.class);
        }

        List<Article> finalArticles = new ArrayList<>();
        List<Question> finalQuestions = new ArrayList<>();
        List<Tag> finalTags = new ArrayList<>();

        try {
            // If it starts with double quotes then search exactly
            if (searchedText.startsWith("\"") && searchedText.endsWith("\"")) {

                // Search for each field
                for (String field : fields) {
                    List<org.apache.lucene.search.Query> queryList = new ArrayList<>();
                    org.apache.lucene.search.Query phraseQuery = getQueryBuilder(genericClass)
                            .phrase()
                            .withSlop(0) // match exactly
                            .onField(field)
                            .sentence(searchedText)
                            .createQuery();
                    queryList.add(phraseQuery);

                    addDistinctValueToList(
                            finalArticles, finalQuestions, finalTags,
                            queryList, className, genericClass, articleCategory);

                }

                // At the end of the loop return result
                return returnFinalListByClassName(className, finalArticles, finalQuestions, finalTags, pageIndex);

            }
            // Else search with 'AND' operator
            else {
                String[] arrKeywords = searchedText.split(" ");

                for (String field : fields) {
                    List<org.apache.lucene.search.Query> queryList = new ArrayList<>();
                    for (String keyword : arrKeywords) {
                        org.apache.lucene.search.Query query = getQueryBuilder(genericClass)
                                .keyword()
                                .onField(field)
                                .matching(keyword.trim())
                                .createQuery();
                        queryList.add(query);
                    }

                    addDistinctValueToList(
                            finalArticles, finalQuestions, finalTags,
                            queryList, className, genericClass, articleCategory);

                }

                // At the end of the loop return result
                return returnFinalListByClassName(className, finalArticles, finalQuestions, finalTags, pageIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Pagination();
    }


    private void addDistinctValueToList(List<Article> finalArticles,
                                        List<Question> finalQuestions,
                                        List<Tag> finalTags,
                                        List<Query> queryList, String className, GenericClass genericClass, String articleCategory) {

        // Search in category of article
        // Only search with not null article
        if (articleCategory != null && articleCategory.trim().length() > 0) {
            org.apache.lucene.search.Query querySearchForArticleCategory = getQueryBuilder(genericClass)
                    .keyword()
                    .onField("category")
                    .matching(articleCategory.trim())
                    .createQuery();
            queryList.add(querySearchForArticleCategory);
        }

        // Build an "and" finalQuery
        BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();

        for (org.apache.lucene.search.Query query : queryList) {
            finalQueryBuilder.add(query, BooleanClause.Occur.MUST);
        }

        FullTextQuery fullTextQuery = getJpaQuery(finalQueryBuilder.build(), genericClass);

        if (ARTICLE.equalsIgnoreCase(className)) {
            List<Article> articles = (List<Article>) fullTextQuery.getResultList();
            for (Article article : articles) {
                if (!finalArticles.contains(article)) {
                    finalArticles.add(article);
                }
            }
        } else if (QUESTION.equalsIgnoreCase(className)) {
            List<Question> questions = (List<Question>) fullTextQuery.getResultList();
            for (Question question : questions) {
                if (!finalQuestions.contains(question)) {
                    finalQuestions.add(question);
                }
            }
        } else if (TAG.equalsIgnoreCase(className)) {
            List<Tag> tags = (List<Tag>) fullTextQuery.getResultList();
            for (Tag tag : tags) {
                if (!finalTags.contains(tag)) {
                    finalTags.add(tag);
                }
            }
        }
    }

    private Pagination returnFinalListByClassName(String className,
                                                  List<Article> finalArticles,
                                                  List<Question> finalQuestions,
                                                  List<Tag> finalTags,
                                                  int pageIndex) {
        // At the end of the loop return result
        if (ARTICLE.equalsIgnoreCase(className)) {
            // Sort article by date
            Collections.sort(finalArticles, (article1, article2) -> {
                if (article1.getUtilTimestamp() != null && article2.getUtilTimestamp() != null) {
                    if (article1.getUtilTimestamp().after(article2.getUtilTimestamp())) {
                        return 1;
                    } else if (article1.getUtilTimestamp().before(article2.getUtilTimestamp())) {
                        return -1;
                    } else {
                        return 0;
                    }
                }

                return 0;
            });

            // Return by page index
            int start = pageIndex * ARTICLES_PER_PAGE;
            int end = start + ARTICLES_PER_PAGE;

            List<Article> articlesByPageIndex = new ArrayList<>();

            int totalSize = finalArticles.size();

            // If start = 5 or 6
            // Array has 0, 1, 2, 3, 4 => error
            if (totalSize <= start) {
                return new ArticlePagination();
            } else {
                for (int i = start; i < end; i++) {
                    try {
                        articlesByPageIndex.add(finalArticles.get(i));
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }
                }
            }

            ArticlePagination articlePagination = new ArticlePagination();
            articlePagination.setArticlesByPageIndex(articlesByPageIndex);
            articlePagination.setNumberOfPages(finalArticles.size());
            return articlePagination;

        } else if (QUESTION.equalsIgnoreCase(className)) {
            Collections.sort(finalQuestions, (question1, question2) -> {
                if (question1.getUtilTimestamp() != null && question2.getUtilTimestamp() != null) {
                    if (question1.getUtilTimestamp().after(question2.getUtilTimestamp())) {
                        return 1;
                    } else if (question1.getUtilTimestamp().before(question2.getUtilTimestamp())) {
                        return -1;
                    } else {
                        return 0;
                    }
                }

                return 0;
            });

            // Return by page index
            int start = pageIndex * QUESTIONS_PER_PAGE;
            int end = start + QUESTIONS_PER_PAGE;

            List<Question> questionsByPageIndex = new ArrayList<>();

            int totalSize = finalQuestions.size();

            // If start = 5 or 6
            // Array has 0, 1, 2, 3, 4 => error
            if (totalSize <= start) {
                return new QuestionPagination();
            } else {
                for (int i = start; i < end; i++) {
                    try {
                        questionsByPageIndex.add(finalQuestions.get(i));
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }
                }
            }

            QuestionPagination questionPagination = new QuestionPagination();
            questionPagination.setQa(questionsByPageIndex);
            questionPagination.setNumberOfPages(finalQuestions.size());

            return questionPagination;
        } else if (TAG.equalsIgnoreCase(className)) {
            Collections.sort(finalTags, (tag1, tag2) -> {
                if (tag1.getReputation() > tag2.getReputation()) {
                    return 1;
                } else if (tag1.getReputation() < tag2.getReputation()) {
                    return -1;
                } else {
                    return 0;
                }
            });

            // Return by page index
            int start = pageIndex * TAGS_PER_PAGE;
            int end = start + TAGS_PER_PAGE;

            List<Tag> tagsByPageIndex = new ArrayList<>();

            int totalSize = finalTags.size();

            // If start = 5 or 6
            // Array has 0, 1, 2, 3, 4 => error
            if (totalSize <= start) {
                return new TagPagination();
            } else {
                for (int i = start; i < end; i++) {
                    try {
                        tagsByPageIndex.add(finalTags.get(i));
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }
                }
            }

            TagPagination tagPagination = new TagPagination();
            tagPagination.setTagsByPageIndex(tagsByPageIndex);
            tagPagination.setNumberOfPages(finalTags.size());

            return tagPagination;
        }
        return new Pagination();
    }

}
