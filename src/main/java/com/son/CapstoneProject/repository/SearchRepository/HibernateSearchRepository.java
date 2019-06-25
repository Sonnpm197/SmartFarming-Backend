package com.son.CapstoneProject.repository.searchRepository;

import com.son.CapstoneProject.entity.search.GenericClass;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
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
    public List search2(String searchedText, GenericClass genericClass, String[] fields) {
        try {
            // If it starts with double quotes then search exactly
            if (searchedText.startsWith("\"") && searchedText.endsWith("\"")) {

                List results = new ArrayList<>();

                // Search for each field
                for (String field : fields) {
                    org.apache.lucene.search.Query phraseQuery = getQueryBuilder(genericClass)
                            .phrase()
                            .withSlop(0) // match exactly
                            .onField(field)
                            .sentence(searchedText)
                            .createQuery();

                    FullTextQuery fullTextQuery = getJpaQuery(phraseQuery, genericClass);
                    results.addAll(fullTextQuery.getResultList());
                }

                return results;
            }

            // Else search with 'AND' operator
            String[] arrKeywords = searchedText.split(" ");

            List<org.apache.lucene.search.Query> queryList = new ArrayList<>();

            for (String keyword : arrKeywords) {
                org.apache.lucene.search.Query query = getQueryBuilder(genericClass)
                        .keyword()
                        .onFields(fields)
                        .matching(keyword.trim())
                        .createQuery();
                queryList.add(query);
            }

            BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();

            for (org.apache.lucene.search.Query q : queryList) {
                finalQueryBuilder.add(q, BooleanClause.Occur.MUST);
            }

            BooleanQuery finalQuery = finalQueryBuilder.build();

            FullTextQuery fullTextQuery = getJpaQuery(finalQuery, genericClass);

            return fullTextQuery.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }


}
