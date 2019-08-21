package com.son.CapstoneProject.repository.searchRepository;

import com.son.CapstoneProject.common.StringUtils;
import com.son.CapstoneProject.controller.FileController;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.pagination.ArticlePagination;
import com.son.CapstoneProject.entity.pagination.Pagination;
import com.son.CapstoneProject.entity.pagination.QuestionPagination;
import com.son.CapstoneProject.entity.pagination.TagPagination;
import com.son.CapstoneProject.entity.search.GenericClass;
import com.son.CapstoneProject.repository.TagRepository;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.hibernate.Cache;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Index;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.son.CapstoneProject.common.ConstantValue.*;

@Repository
public class HibernateSearchRepository {

    private static final Logger logger = LoggerFactory.getLogger(HibernateSearchRepository.class);

    //    @PersistenceContext allows you to specify which persistence unit you want to use.
//    Your project might have multiple data sources connected to different DBs
//    and @PersistenceContext allows you to say which one you want to operate on
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TagRepository tagRepository;

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

    /**
     * This method is used to search for title or content
     * Example: cay sen -> search "cay" AND "sen"
     * <p>
     * if start with double quotes => search exactly
     *
     * @param searchedText
     * @return
     */
//    public Pagination search2(String searchedText, String className, String[] fields,
//                              String articleCategory, String sortBy, int pageIndex, boolean isHomepageSearch) throws Exception {
//        try {
//            GenericClass genericClass = null;
//
//            if (ARTICLE.equalsIgnoreCase(className)) {
//                genericClass = new GenericClass(Article.class);
//            } else if (QUESTION.equalsIgnoreCase(className)) {
//                genericClass = new GenericClass(Question.class);
//            } else if (TAG.equalsIgnoreCase(className)) {
//                genericClass = new GenericClass(Tag.class);
//            }
//
//            // If it starts with double quotes then search exactly
//            if (searchedText.startsWith("\"") && searchedText.endsWith("\"")) {
//
//                // Search for each field
//                List<org.apache.lucene.search.Query> queryList = new ArrayList<>();
//                for (String field : fields) {
//                    org.apache.lucene.search.Query phraseQuery = getQueryBuilder(genericClass)
//                            .phrase()
//                            .withSlop(0) // match exactly
//                            .onField(field)
//                            .sentence(searchedText)
//                            .createQuery();
//                    queryList.add(phraseQuery);
//                }
//
//                // At the end of the loop return result
//                return returnFinalListByClassName(queryList,
//                        className,
//                        genericClass,
//                        articleCategory,
//                        sortBy,
//                        pageIndex,
//                        isHomepageSearch,
//                        true);
//
//            }
//            // Else search with 'AND' operator
//            else {
//                String[] arrKeywords = searchedText.split(" ");
//
//                List<org.apache.lucene.search.Query> queryList = new ArrayList<>();
//                for (String keyword : arrKeywords) {
//                    if (!StringUtils.isNullOrEmpty(keyword)) {
//                        org.apache.lucene.search.Query query = getQueryBuilder(genericClass)
//                                .keyword()
//                                .onFields(fields)
//                                .matching(keyword.trim())
//                                .createQuery();
//                        queryList.add(query);
//                    }
//                }
//
//                // At the end of the loop return result
//                return returnFinalListByClassName(queryList,
//                        className,
//                        genericClass,
//                        articleCategory,
//                        sortBy,
//                        pageIndex,
//                        isHomepageSearch,
//                        false);
//            }
//        } catch (Exception e) {
//            logger.error("An error has occurred", e);
//            throw e;
//        }
//    }
    public Pagination search3(String searchedText, String className, String[] fields,
                              String articleCategory, String sortBy, int pageIndex, boolean isHomepageSearch) throws Exception {
        try {
            GenericClass genericClass = null;

            if (ARTICLE.equalsIgnoreCase(className)) {
                genericClass = new GenericClass(Article.class);
            } else if (QUESTION.equalsIgnoreCase(className)) {
                genericClass = new GenericClass(Question.class);
            } else if (TAG.equalsIgnoreCase(className)) {
                genericClass = new GenericClass(Tag.class);
            }

            // If it starts with double quotes then search exactly
            BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();

            if (searchedText.startsWith("\"") && searchedText.endsWith("\"")) {
                org.apache.lucene.search.Query phraseQuery = null;
                if (QUESTION.equalsIgnoreCase(className)) {
                    phraseQuery = getQueryBuilder(genericClass)
                            .simpleQueryString()
                            .onFields(fields[0], fields[1], "tags.name") // title and contentWithoutHtmlTags for question & article
                            .matching(searchedText)
                            .createQuery();

                } else if (ARTICLE.equalsIgnoreCase(className)) {
                    if (!StringUtils.isNullOrEmpty(articleCategory)) {
                        phraseQuery = getQueryBuilder(genericClass)
                                .bool()
                                .must(getQueryBuilder(genericClass)
                                        .simpleQueryString()
                                        .onFields(fields[0], fields[1], "tags.name")
                                        .matching(searchedText)
                                        .createQuery())
                                .must(getQueryBuilder(genericClass)
                                        .simpleQueryString()
                                        .onField("category")
                                        .matching(articleCategory)
                                        .createQuery())
                                .createQuery();
                    } else {
                        phraseQuery = getQueryBuilder(genericClass)
                                .bool()
                                .must(getQueryBuilder(genericClass)
                                        .simpleQueryString()
                                        .onFields(fields[0], fields[1], "tags.name")
                                        .matching(searchedText)
                                        .createQuery())
                                .createQuery();
                    }
                } else if (TAG.equalsIgnoreCase(className)) {
                    phraseQuery = getQueryBuilder(genericClass)
                            .simpleQueryString()
                            .onField(fields[0]) // name for tag
                            .matching(searchedText)
                            .createQuery();
                }

                finalQueryBuilder.add(phraseQuery, BooleanClause.Occur.SHOULD);
            }
            // If searchedText passed from Angular does not start & end with double quotes
            else {
                if (ARTICLE.equalsIgnoreCase(className)) {
                    if (!StringUtils.isNullOrEmpty(articleCategory)) {
                        // If search text has more than 1 word
                        if (!StringUtils.isNullOrEmpty(searchedText)) {
                            for (String field : fields) {
                                org.apache.lucene.search.Query phraseQuery = getQueryBuilder(genericClass)
                                        .bool()
                                        .must(getQueryBuilder(genericClass)
                                                .phrase()
                                                .withSlop(2)
                                                .onField(field)
                                                .sentence(searchedText)
                                                .createQuery())
                                        .must(getQueryBuilder(genericClass)
                                                .simpleQueryString()
                                                .onField("category")
                                                .matching("\"" + articleCategory + "\"")
                                                .createQuery())
                                        .should(getQueryBuilder(genericClass)
                                                .phrase()
                                                .withSlop(2)
                                                .onField("tags.name")
                                                .sentence(searchedText)
                                                .createQuery())
                                        .createQuery();
                                finalQueryBuilder.add(phraseQuery, BooleanClause.Occur.SHOULD);
                            }
                        }
                        // If searchedText is null or "" and articleCategory is not null
                        else {
                            org.apache.lucene.search.Query phraseQuery = getQueryBuilder(genericClass)
                                    .simpleQueryString()
                                    .onField("category")
                                    .matching("\"" + articleCategory + "\"").createQuery();
                            finalQueryBuilder.add(phraseQuery, BooleanClause.Occur.SHOULD);
                        }
                    }
                    // Search articles without category
                    else {
                        for (String field : fields) {
                            // ======================================================== //
                            // If this is an article then we need to search for category
                            // Note**: title / content must go with category, but tags are not necessary
                            // ======================================================== //
                            org.apache.lucene.search.Query phraseQuery = getQueryBuilder(genericClass)
                                    .bool()
                                    .must(getQueryBuilder(genericClass)
                                            .phrase()
                                            .withSlop(2) // Allow (wrong) words between searchedText reach maximum of 2
                                            .onField(field)
                                            .sentence(searchedText)
                                            .createQuery())
                                    .should(getQueryBuilder(genericClass)
                                            .phrase()
                                            .withSlop(2)
                                            .onField("tags.name")
                                            .sentence(searchedText)
                                            .createQuery())
                                    .createQuery();
                            finalQueryBuilder.add(phraseQuery, BooleanClause.Occur.SHOULD);
                        }
                    }
                } else if (QUESTION.equalsIgnoreCase(className)) {
                    for (String field : fields) {
                        org.apache.lucene.search.Query phraseQuery = getQueryBuilder(genericClass)
                                .bool()
                                .must(getQueryBuilder(genericClass)
                                        .phrase()
                                        .withSlop(2)
                                        .onField(field)
                                        .sentence(searchedText)
                                        .createQuery())
                                .should(getQueryBuilder(genericClass)
                                        .phrase()
                                        .withSlop(2)
                                        .onField("tags.name")
                                        .sentence(searchedText)
                                        .createQuery())
                                .createQuery();
                        finalQueryBuilder.add(phraseQuery, BooleanClause.Occur.SHOULD);
                    }
                } else if (TAG.equalsIgnoreCase(className)) {
                    org.apache.lucene.search.Query phraseQuery = getQueryBuilder(genericClass)
                            .phrase()
                            .withSlop(2)
                            .onField(fields[0]) // name for tag
                            .sentence(searchedText)
                            .createQuery();
                    finalQueryBuilder.add(phraseQuery, BooleanClause.Occur.SHOULD);
                }
            }

            return returnFinalListByClassName(finalQueryBuilder,
                    className,
                    genericClass,
                    articleCategory,
                    sortBy,
                    pageIndex,
                    isHomepageSearch);

        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw e;
        }
    }

    private Pagination returnFinalListByClassName(
            /*List<Query> queryList,*/
            BooleanQuery.Builder finalQueryBuilder,
            String className,
            GenericClass genericClass,
            String articleCategory,
            String sortBy,
            int pageIndex,
            boolean isHomepageSearch
            /*, boolean isDoubleQuote*/) throws Exception {

        String methodName = "HibernateSearchRepository.returnFinalListByClassName";

        List<Article> finalArticles = new ArrayList<>();
        List<Question> finalQuestions = new ArrayList<>();
        List<Tag> finalTags = new ArrayList<>();

        // Build an "and" finalQuery
//        BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();

//        for (org.apache.lucene.search.Query query : queryList) {
//            if (isDoubleQuote) {
//                finalQueryBuilder.add(query, BooleanClause.Occur.SHOULD);
//            } else {
//                finalQueryBuilder.add(query, BooleanClause.Occur.MUST);
//            }
//
//            finalQueryBuilder.add(query, BooleanClause.Occur.SHOULD);
//        }

        // Search in category of article (Only with not null article)
//        if (articleCategory != null && articleCategory.trim().length() > 0) {
//            org.apache.lucene.search.Query querySearchForArticleCategory = getQueryBuilder(genericClass)
//                    .simpleQueryString()
//                    .onField("category")
//                    .matching("\"" + articleCategory.trim() + "\"")
//                    .createQuery();
//            finalQueryBuilder.add(querySearchForArticleCategory, BooleanClause.Occur.MUST);
////            queryList.add(querySearchForArticleCategory);
//        }

        FullTextQuery fullTextQuery = getJpaQuery(finalQueryBuilder.build(), genericClass);

//        FullTextQuery fullTextQuery = getJpaQuery(queryList.get(0), genericClass);
        if (ARTICLE.equalsIgnoreCase(className)) {
            if (!isHomepageSearch) {
                fullTextQuery.setFirstResult(pageIndex * ARTICLES_PER_PAGE); // start from this element
                fullTextQuery.setMaxResults(ARTICLES_PER_PAGE); // number of element
            } else {
                fullTextQuery.setFirstResult(pageIndex * HOME_PAGE_SEARCH_ARTICLES_PER_PAGE); // start from this element
                fullTextQuery.setMaxResults(HOME_PAGE_SEARCH_ARTICLES_PER_PAGE); // number of element
            }
        } else if (QUESTION.equalsIgnoreCase(className)) {
            if (!isHomepageSearch) {
                fullTextQuery.setFirstResult(pageIndex * QUESTIONS_PER_PAGE); // start from this element
                fullTextQuery.setMaxResults(QUESTIONS_PER_PAGE); // number of element
            } else {
                fullTextQuery.setFirstResult(pageIndex * HOME_PAGE_SEARCH_QUESTIONS_PER_PAGE); // start from this element
                fullTextQuery.setMaxResults(HOME_PAGE_SEARCH_QUESTIONS_PER_PAGE); // number of element
            }
        } else if (TAG.equalsIgnoreCase(className)) {
            fullTextQuery.setFirstResult(pageIndex * TAGS_PER_PAGE); // start from this element
            fullTextQuery.setMaxResults(TAGS_PER_PAGE); // number of element
        }

        int totalSize = fullTextQuery.getResultSize();

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

        // At the end of the loop return result
        if (ARTICLE.equalsIgnoreCase(className)) {
            if (SORT_DATE.equalsIgnoreCase(sortBy)) {
                Collections.sort(finalArticles, (article1, article2) -> {
                    if (article1.getUtilTimestamp() != null && article2.getUtilTimestamp() != null) {
                        if (article1.getUtilTimestamp().after(article2.getUtilTimestamp())) {
                            return -1;
                        } else if (article1.getUtilTimestamp().before(article2.getUtilTimestamp())) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                    return 0;
                });
            } else if (SORT_VIEW_COUNT.equalsIgnoreCase(sortBy)) {
                Collections.sort(finalArticles, (article1, article2) -> {
                    if (article1.getViewCount() >= 0 && article2.getViewCount() >= 0) {
                        if (article1.getViewCount() > article2.getViewCount()) {
                            return -1;
                        } else if (article1.getViewCount() < article2.getViewCount()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                    return 0;
                });
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(sortBy)) {
                Collections.sort(finalArticles, (article1, article2) -> {
                    if (article1.getUpvoteCount() >= 0 && article2.getUpvoteCount() >= 0) {
                        if (article1.getUpvoteCount() > article2.getUpvoteCount()) {
                            return -1;
                        } else if (article1.getUpvoteCount() < article2.getUpvoteCount()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                    return 0;
                });
            } else {
                throw new Exception(methodName + " unknown type: " + sortBy);
            }

            int numberOfContentsPerPage = 0;

            if (isHomepageSearch) {
                numberOfContentsPerPage = HOME_PAGE_SEARCH_ARTICLES_PER_PAGE;
            } else {
                numberOfContentsPerPage = ARTICLES_PER_PAGE;
            }

            int numberOfPages = 0;
            if (totalSize % numberOfContentsPerPage == 0) {
                numberOfPages = totalSize / numberOfContentsPerPage;
            } else {
                numberOfPages = totalSize / numberOfContentsPerPage + 1;
            }

            ArticlePagination articlePagination = new ArticlePagination();
            articlePagination.setArticlesByPageIndex(finalArticles);
            articlePagination.setNumberOfPages(numberOfPages);
            articlePagination.setNumberOfContents(totalSize);

            return articlePagination;
        } else if (QUESTION.equalsIgnoreCase(className)) {
            if (SORT_DATE.equalsIgnoreCase(sortBy)) {
                Collections.sort(finalQuestions, (question1, question2) -> {
                    if (question1.getUtilTimestamp() != null && question2.getUtilTimestamp() != null) {
                        if (question1.getUtilTimestamp().after(question2.getUtilTimestamp())) {
                            return -1;
                        } else if (question1.getUtilTimestamp().before(question2.getUtilTimestamp())) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                    return 0;
                });
            } else if (SORT_VIEW_COUNT.equalsIgnoreCase(sortBy)) {
                Collections.sort(finalQuestions, (question1, question2) -> {
                    if (question1.getViewCount() >= 0 && question2.getViewCount() >= 0) {
                        if (question1.getViewCount() > question2.getViewCount()) {
                            return -1;
                        } else if (question1.getViewCount() < question2.getViewCount()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                    return 0;
                });
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(sortBy)) {
                Collections.sort(finalQuestions, (question1, question2) -> {
                    if (question1.getUpvoteCount() >= 0 && question2.getUpvoteCount() >= 0) {
                        if (question1.getUpvoteCount() > question2.getUpvoteCount()) {
                            return -1;
                        } else if (question1.getUpvoteCount() < question2.getUpvoteCount()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                    return 0;
                });
            } else {
                throw new Exception(methodName + " unknown type: " + sortBy);
            }

            int numberOfContentsPerPage = 0;

            if (isHomepageSearch) {
                numberOfContentsPerPage = HOME_PAGE_SEARCH_QUESTIONS_PER_PAGE;
            } else {
                numberOfContentsPerPage = QUESTIONS_PER_PAGE;
            }

            int numberOfPages = 0;
            if (totalSize % numberOfContentsPerPage == 0) {
                numberOfPages = totalSize / numberOfContentsPerPage;
            } else {
                numberOfPages = totalSize / numberOfContentsPerPage + 1;
            }

            QuestionPagination questionPagination = new QuestionPagination();
            questionPagination.setQa(finalQuestions);
            questionPagination.setNumberOfPages(numberOfPages);
            questionPagination.setNumberOfContents(totalSize);

            return questionPagination;

        } else if (TAG.equalsIgnoreCase(className)) {

//            // Get 7 days ago
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(new Date());
//            calendar.add(Calendar.DATE, -7);
//            String searchDate = sdf.format(calendar.getTime());
//
//            // Then count view 7 days ago
//            for (Tag tag : finalTags) {
//                List<Object[]> viewCountQuestionAndArticle =
//                        tagRepository.countTotalQuestionViewAndArticleViewBeforeDate(searchDate, tag.getTagId());
//
//                int questionViewCountOneWeekAgo = viewCountQuestionAndArticle.get(0)[0] == null ? 0 : Integer.parseInt(viewCountQuestionAndArticle.get(0)[0].toString());
//                int articleViewCountOneWeekAgo = viewCountQuestionAndArticle.get(0)[1] == null ? 0 : Integer.parseInt(viewCountQuestionAndArticle.get(0)[1].toString());
//
//                tag.setViewCountOneWeekAgo(questionViewCountOneWeekAgo + articleViewCountOneWeekAgo);
//                tagRepository.save(tag);
//            }

            // Sort by which tags differ from those from one week ago the most
            if (SORT_VIEW_COUNT.equalsIgnoreCase(sortBy)) {
//                Collections.sort(finalTags, (tag1, tag2) -> {
//                    int tag1viewCountDifference = tag1.getViewCount() - tag1.getViewCountOneWeekAgo();
//                    int tag2viewCountDifference = tag2.getViewCount() - tag2.getViewCountOneWeekAgo();
//
//                    if (tag1viewCountDifference > tag2viewCountDifference) {
//                        return -1;
//                    } else if (tag1viewCountDifference < tag2viewCountDifference) {
//                        return 1;
//                    } else {
//                        return 0;
//                    }
//                });

                Collections.sort(finalTags, (tag1, tag2) -> {
                    if (tag1.getViewCount() > tag2.getViewCount()) {
                        return -1;
                    } else if (tag1.getViewCount() < tag2.getViewCount()) {
                        return 1;
                    } else {
                        return 0;
                    }
                });
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(sortBy)) {
                Collections.sort(finalTags, (tag1, tag2) -> {
                    if (tag1.getReputation() > tag2.getReputation()) {
                        return -1;
                    } else if (tag1.getReputation() < tag2.getReputation()) {
                        return 1;
                    } else {
                        return 0;
                    }
                });
            } else {
                throw new Exception("Unknown type to findAllTags: " + sortBy);
            }

            int numberOfPages = 0;
            if (totalSize % TAGS_PER_PAGE == 0) {
                numberOfPages = totalSize / TAGS_PER_PAGE;
            } else {
                numberOfPages = totalSize / TAGS_PER_PAGE + 1;
            }

            TagPagination tagPagination = new TagPagination();
            tagPagination.setTagsByPageIndex(finalTags);
            tagPagination.setNumberOfPages(numberOfPages);
            tagPagination.setNumberOfContents(totalSize);

            return tagPagination;
        }
        return new Pagination();
    }

    public Pagination recommendTagNameWhileTyping(String searchedText, String className, String field) throws Exception {
        try {
            GenericClass genericClass = null;

            if (TAG.equalsIgnoreCase(className)) {
                genericClass = new GenericClass(Tag.class);
            } else {
                throw new Exception("HibernateSearchRepository.recommendTagsWhileTyping: Unknown className: " + className);
            }

            List<Tag> finalTags = new ArrayList<>();

            // If it starts with double quotes then search exactly
            if (searchedText.startsWith("\"") && searchedText.endsWith("\"")) {

                List<org.apache.lucene.search.Query> queryList = new ArrayList<>();
                // Search for each field
                org.apache.lucene.search.Query phraseQuery = getQueryBuilder(genericClass)
                        .phrase()
                        .withSlop(0) // match exactly
                        .onField(field)
                        .sentence(searchedText)
                        .createQuery();
                queryList.add(phraseQuery);

                // At the end of the loop return result
                return returnFinalListRecommendedTag(className, finalTags, queryList, genericClass);

            }
            // Else search with 'AND' operator
            else {
                String[] arrKeywords = searchedText.split(" ");

                List<org.apache.lucene.search.Query> queryList = new ArrayList<>();
                for (String keyword : arrKeywords) {
                    if (!StringUtils.isNullOrEmpty(keyword)) {
                        org.apache.lucene.search.Query query = getQueryBuilder(genericClass)
                                .keyword()
                                .onField(field)
                                .matching(keyword.trim())
                                .createQuery();
                        queryList.add(query);
                    }
                }

                // At the end of the loop return result
                return returnFinalListRecommendedTag(className, finalTags, queryList, genericClass);
            }
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw e;
        }
    }

    private Pagination returnFinalListRecommendedTag(String className,
                                                     List<Tag> finalTags,
                                                     List<Query> queryList,
                                                     GenericClass genericClass) throws Exception {

        String methodName = "HibernateSearchRepository.returnFinalListRecommendedTag";

        // Build an "and" finalQuery
        BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();

        for (org.apache.lucene.search.Query query : queryList) {
            finalQueryBuilder.add(query, BooleanClause.Occur.MUST);
        }

        FullTextQuery fullTextQuery = getJpaQuery(finalQueryBuilder.build(), genericClass);
        fullTextQuery.setFirstResult(0);
        fullTextQuery.setMaxResults(5);

        if (TAG.equalsIgnoreCase(className)) {
            List<Tag> tags = (List<Tag>) fullTextQuery.getResultList();
            for (Tag tag : tags) {
                if (!finalTags.contains(tag)) {
                    finalTags.add(tag);
                }
            }
        }

        // At the end of the loop return result
        if (TAG.equalsIgnoreCase(className)) {
            // Sort by both view count and reputation
            Collections.sort(finalTags, (tag1, tag2) -> {
                if (tag1.getViewCount() > tag2.getViewCount()) {
                    return -1;
                } else if (tag1.getViewCount() < tag2.getViewCount()) {
                    return 1;
                } else {
                    return 0;
                }
            });

            TagPagination tagPagination = new TagPagination();
            tagPagination.setTagsByPageIndex(finalTags);
            tagPagination.setNumberOfPages(1);

            return tagPagination;
        }
        return new Pagination();
    }
}
