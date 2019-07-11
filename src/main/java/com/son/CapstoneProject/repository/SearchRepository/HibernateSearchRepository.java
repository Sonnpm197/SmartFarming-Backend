package com.son.CapstoneProject.repository.searchRepository;

import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.search.GenericClass;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static com.son.CapstoneProject.common.ConstantValue.ARTICLE;
import static com.son.CapstoneProject.common.ConstantValue.QUESTION;
import static com.son.CapstoneProject.common.ConstantValue.TAG;

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
    @Transactional
    public List search(String searchedText, GenericClass genericClass, String[] fields) {
        try {
            org.apache.lucene.search.Query luceneQuery = getQueryBuilder(genericClass)
                    .keyword()
                    .onFields(fields) // list to vararg
                    .matching(searchedText)
                    .createQuery();

            FullTextQuery jpaFullTextQuery = getJpaQuery(luceneQuery, genericClass);

            return jpaFullTextQuery.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * This method is used to search for title or content
     * Example: cay sen -> search "cay" AND "sen"
     * <p>
     * if start with double quotes => search exactly
     *
     * @param searchedText
     * @return
     */

    public List search2(String searchedText, String className, String[] fields, String articleCategory) {
        GenericClass genericClass = null;

        if (ARTICLE.equalsIgnoreCase(className)) {
            genericClass = new GenericClass(Article.class);
        } else if (QUESTION.equalsIgnoreCase(className)) {
            genericClass = new GenericClass(Question.class);
        } else if (TAG.equalsIgnoreCase(className)) {
            genericClass = new GenericClass(Tag.class);
        }

        List<Article> articles = null;
        List<Article> finalArticles = new ArrayList<>();
        List<Question> questions = null;
        List<Question> finalQuestions = new ArrayList<>();
        List<Tag> tags = null;
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
                            articles, finalArticles,
                            questions, finalQuestions,
                            tags, finalTags,
                            queryList, className, genericClass, articleCategory);

                }

                // At the end of the loop return result
                returnFinalListByClassName(className, finalArticles, finalQuestions, finalTags);

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
                            articles, finalArticles,
                            questions, finalQuestions,
                            tags, finalTags,
                            queryList, className, genericClass, articleCategory);

                }

                // At the end of the loop return result
                return returnFinalListByClassName(className, finalArticles, finalQuestions, finalTags);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }


    private void addDistinctValueToList(List<Article> articles, List<Article> finalArticles,
                                        List<Question> questions, List<Question> finalQuestions,
                                        List<Tag> tags, List<Tag> finalTags,
                                        List<Query> queryList, String className, GenericClass genericClass, String articleCategory) {

        // Search in category of article
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
            articles = (List<Article>) fullTextQuery.getResultList();
            for (Article article : articles) {
                if (!finalArticles.contains(article)) {
                    finalArticles.add(article);
                }
            }
        } else if (QUESTION.equalsIgnoreCase(className)) {
            questions = (List<Question>) fullTextQuery.getResultList();
            for (Question question : questions) {
                if (!finalQuestions.contains(question)) {
                    finalQuestions.add(question);
                }
            }
        } else if (TAG.equalsIgnoreCase(className)) {
            tags = (List<Tag>) fullTextQuery.getResultList();
            for (Tag tag : tags) {
                if (!finalTags.contains(tag)) {
                    finalTags.add(tag);
                }
            }
        }
    }

    private List returnFinalListByClassName(String className, List<Article> finalArticles, List<Question> finalQuestions, List<Tag> finalTags) {
        // At the end of the loop return result
        if (ARTICLE.equalsIgnoreCase(className)) {
            return finalArticles;
        } else if (QUESTION.equalsIgnoreCase(className)) {
            return finalQuestions;
        } else if (TAG.equalsIgnoreCase(className)) {
            return finalTags;
        }
        return null;
    }
}
