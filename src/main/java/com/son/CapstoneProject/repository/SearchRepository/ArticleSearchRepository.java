package com.son.CapstoneProject.repository.SearchRepository;

import com.son.CapstoneProject.domain.Article;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class ArticleSearchRepository {

//    @PersistenceContext allows you to specify which persistence unit you want to use.
//    Your project might have multiple data sources connected to different DBs
//    and @PersistenceContext allows you to say which one you want to operate on
    @Autowired
    private EntityManager entityManager;

    private FullTextQuery getJpaQuery(org.apache.lucene.search.Query luceneQuery) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        return fullTextEntityManager.createFullTextQuery(luceneQuery, Article.class);
    }

    private QueryBuilder getQueryBuilder() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        return fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Article.class)
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
    public List<Article> searchArticles(String searchedText) {
        org.apache.lucene.search.Query luceneQuery = getQueryBuilder()
                .keyword()
                .onFields("title", "content")
                .matching(searchedText)
                .createQuery();

        FullTextQuery jpaFullTextQuery = getJpaQuery(luceneQuery);

        List<Article> articles = jpaFullTextQuery.getResultList();

        return articles;
    }

}
