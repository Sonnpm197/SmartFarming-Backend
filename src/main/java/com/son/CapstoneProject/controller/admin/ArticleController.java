package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.common.StringUtils;
import com.son.CapstoneProject.common.entity.*;
import com.son.CapstoneProject.common.entity.login.AppUser;
import com.son.CapstoneProject.common.entity.pagination.ArticlePagination;
import com.son.CapstoneProject.common.entity.search.ArticleSearch;
import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.controller.FileController;
import com.son.CapstoneProject.repository.*;
import com.son.CapstoneProject.repository.searchRepository.HibernateSearchRepository;
import com.son.CapstoneProject.service.ViewCountingService;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.*;

import static com.son.CapstoneProject.common.ConstantValue.*;

@RestController
@RequestMapping("/article")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private HibernateSearchRepository hibernateSearchRepository;

    @Autowired
    private ViewCountingService countingService;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    @Autowired
    private FileController fileController;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/viewNumberOfArticles")
    @Transactional
    public long viewNumberOfArticles() {
        return articleRepository.count();
    }

    @GetMapping("/viewDistinctCategories")
    @Transactional
    public List<String> viewDistinctCategories() {
        try {
            return articleRepository.findDistinctCategory();
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewNumberOfPages")
    @Transactional
    public long viewNumberOfPages() {
        try {
            long numberOfArticle = articleRepository.count();
            if (numberOfArticle % ARTICLES_PER_PAGE == 0) {
                return numberOfArticle / ARTICLES_PER_PAGE;
            } else {
                return (numberOfArticle / ARTICLES_PER_PAGE) + 1;
            }
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewArticles/{type}/{pageNumber}")
    @Transactional
    public ArticlePagination viewArticles(@PathVariable String type, @PathVariable int pageNumber) {
        try {
            PageRequest pageNumWithElements;

            if (SORT_DATE.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("utilTimestamp").descending());
            } else if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("viewCount").descending());
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("upvoteCount").descending());
            } else {
                throw new Exception("ArticleController.viewArticles unknown type: " + type);
            }

            Page<Article> articlePage = articleRepository.findAll(pageNumWithElements);
            ArticlePagination articlePagination = new ArticlePagination();
            articlePagination.setArticlesByPageIndex(articlePage.getContent());
            articlePagination.setNumberOfPages(Integer.parseInt("" + viewNumberOfPages()));
            articlePagination.setNumberOfContents(Integer.parseInt("" + articleRepository.count()));
            return articlePagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/viewArticlesByCategory/{type}/{pageNumber}")
    @Transactional
    public ArticlePagination viewArticlesByCategory(@RequestBody ArticleSearch articleSearch,
                                                    @PathVariable String type,
                                                    @PathVariable int pageNumber) {
        try {
            PageRequest pageNumWithElements;

            if (SORT_DATE.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("utilTimestamp").descending());
            } else if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("viewCount").descending());
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("upvoteCount").descending());
            } else {
                throw new Exception("ArticleController.viewArticles unknown type: " + type);
            }

            Page<Article> articlePage = articleRepository.findByCategory(articleSearch.getCategory(), pageNumWithElements);
            ArticlePagination articlePagination = new ArticlePagination();
            articlePagination.setArticlesByPageIndex(articlePage.getContent());

            Integer numberOfArticlesByCategory = articleRepository.findNumberOfArticlesByCategory(articleSearch.getCategory());

            int numberOfArticlesByInt = 0;
            if (numberOfArticlesByCategory == null) {
                numberOfArticlesByInt = 0;
            } else {
                numberOfArticlesByInt = numberOfArticlesByCategory;
            }

            if (numberOfArticlesByInt % ARTICLES_PER_PAGE == 0) {
                articlePagination.setNumberOfPages(numberOfArticlesByInt / ARTICLES_PER_PAGE);
            } else {
                articlePagination.setNumberOfPages(numberOfArticlesByInt / ARTICLES_PER_PAGE + 1);
            }

            articlePagination.setNumberOfContents(numberOfArticlesByInt);
            return articlePagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewArticle/{userId}/{articleId}")
    @Transactional
    public Article viewArticle(@PathVariable Long userId, @PathVariable Long articleId, HttpServletRequest request) {
        try {
            String ipAddress = HttpRequestResponseUtils.getClientIpAddress(request);
            // Execute asynchronously
//            countingService.countViewByIpAddress(contentId, ipAddress, ARTICLE);
            countingService.countViewByUserId(articleId, userId, ARTICLE);
            return articleRepository.findById(articleId)
                    .orElseThrow(() -> new Exception("ArticleController.viewArticle: Not found any article with id: " + articleId));
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/searchArticles/{type}/{pageNumber}")
    @Transactional
    public ArticlePagination searchArticles(@RequestBody ArticleSearch articleSearch,
                                            @PathVariable String type,
                                            @PathVariable int pageNumber) {
        try {
            if (StringUtils.isNullOrEmpty(articleSearch.getTextSearch()) && StringUtils.isNullOrEmpty(articleSearch.getCategory())) {
                // return by previous status
                return viewArticles(type, pageNumber);
            }

//            // Search by tag first
//            TagPagination tagPagination = (TagPagination) hibernateSearchRepository.search3(
//                    articleSearch.getTextSearch(),
//                    TAG,
//                    new String[]{"name"}, // search tag by name
//                    null,
//                    SORT_UPVOTE_COUNT,
//                    0,
//                    false
//            );
//
//            List<Tag> tags = tagPagination.getTagsByPageIndex();
//
//            if (tags == null || tags.size() == 0) {
            return (ArticlePagination) hibernateSearchRepository.search3(
                    articleSearch.getTextSearch(),
                    ARTICLE,
                    new String[]{"title", "contentWithoutHtmlTags"},
                    articleSearch.getCategory(),
                    type,
                    pageNumber,
                    false
            );
//            }
//            // Search by list tags
//            else {
//                ArticlePagination articlePagination = new ArticlePagination();
//                List<Long> tagIds = new ArrayList<>();
//
//                for (Tag tag : tags) {
//                    if (!tagIds.contains(tag.getTagId())) {
//                        tagIds.add(tag.getTagId());
//                    }
//                }
//
//                // We have to select all questionIds in these tagIds
//                List<BigInteger> articleIdsResult = articleRepository.findDistinctArticleIdsByTags_tagIdIn(tagIds);
//                int numberOfContents = articleRepository.countDistinctNumberOfArticlesByTags_tagIdIn(tagIds);
//
//                List<Article> finalArticles = new ArrayList<>();
//                for (BigInteger articleIdBigInteger : articleIdsResult) {
//                    try {
//                        finalArticles.add(articleRepository.findById(articleIdBigInteger.longValue()).get());
//                    } catch (Exception e) {
//                        logger.error("An error has occurred", e);
//                        continue;
//                    }
//                }
//
//                // Then sort them
//                if (SORT_DATE.equalsIgnoreCase(type)) {
//                    Collections.sort(finalArticles, (article1, article2) -> {
//                        if (article1.getUtilTimestamp() != null && article2.getUtilTimestamp() != null) {
//                            if (article1.getUtilTimestamp().after(article2.getUtilTimestamp())) {
//                                return -1;
//                            } else if (article1.getUtilTimestamp().before(article2.getUtilTimestamp())) {
//                                return 1;
//                            } else {
//                                return 0;
//                            }
//                        }
//                        return 0;
//                    });
//                } else if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
//                    Collections.sort(finalArticles, (article1, article2) -> {
//                        if (article1.getViewCount() >= 0 && article2.getViewCount() >= 0) {
//                            if (article1.getViewCount() > article2.getViewCount()) {
//                                return -1;
//                            } else if (article1.getViewCount() < article2.getViewCount()) {
//                                return 1;
//                            } else {
//                                return 0;
//                            }
//                        }
//                        return 0;
//                    });
//                } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
//                    Collections.sort(finalArticles, (article1, article2) -> {
//                        if (article1.getUpvoteCount() >= 0 && article2.getUpvoteCount() >= 0) {
//                            if (article1.getUpvoteCount() > article2.getUpvoteCount()) {
//                                return -1;
//                            } else if (article1.getUpvoteCount() < article2.getUpvoteCount()) {
//                                return 1;
//                            } else {
//                                return 0;
//                            }
//                        }
//                        return 0;
//                    });
//                } else {
//                    throw new Exception("ArticleController.searchQuestions unknown type: " + type);
//                }
//
//                // Add number of pages
//                if (numberOfContents % ARTICLES_PER_PAGE == 0) {
//                    articlePagination.setNumberOfPages(numberOfContents / ARTICLES_PER_PAGE);
//                } else {
//                    articlePagination.setNumberOfPages(numberOfContents / ARTICLES_PER_PAGE + 1);
//                }
//
//                // Then get result i = start; i <= end from the above array
//                int start = pageNumber * ARTICLES_PER_PAGE;
//                int end = pageNumber * ARTICLES_PER_PAGE + ARTICLES_PER_PAGE - 1;
//
//                List<Article> articleShownOnUI = new ArrayList<>();
//                for (int i = start; i <= end; i++) {
//                    try {
//                        articleShownOnUI.add(finalArticles.get(i));
//                    } catch (Exception e) {
//                        // Prevent index out of bound
////                        logger.error("An error has occurred", e);
//                        continue;
//                    }
//                }
//
//                // Finally add to questionPagination
//                articlePagination.setArticlesByPageIndex(articleShownOnUI);
//                return articlePagination;
//            }
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/searchArticlesOnHomePage/{type}/{pageNumber}")
    @Transactional
    public ArticlePagination searchArticlesOnHomePage(@RequestBody ArticleSearch articleSearch,
                                                      @PathVariable String type,
                                                      @PathVariable int pageNumber) {
        try {
            // Search by tag first
//            TagPagination tagPagination = (TagPagination) hibernateSearchRepository.search3(
//                    articleSearch.getTextSearch(),
//                    TAG,
//                    new String[]{"name"}, // search tag by name
//                    null,
//                    SORT_UPVOTE_COUNT,
//                    0,
//                    false
//            );
//
//            List<Tag> tags = tagPagination.getTagsByPageIndex();
//
//            if (tags == null || tags.size() == 0) {
            return (ArticlePagination) hibernateSearchRepository.search3(
                    articleSearch.getTextSearch(),
                    ARTICLE,
                    new String[]{"title", "contentWithoutHtmlTags"},
                    articleSearch.getCategory(),
                    type,
                    pageNumber,
                    true
            );
//            } else {
//                PageRequest pageNumWithElements;
//
//                if (SORT_DATE.equalsIgnoreCase(type)) {
//                    pageNumWithElements = PageRequest.of(pageNumber, HOME_PAGE_SEARCH_ARTICLES_PER_PAGE, Sort.by("utilTimestamp").descending());
//                } else if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
//                    pageNumWithElements = PageRequest.of(pageNumber, HOME_PAGE_SEARCH_ARTICLES_PER_PAGE, Sort.by("viewCount").descending());
//                } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
//                    pageNumWithElements = PageRequest.of(pageNumber, HOME_PAGE_SEARCH_ARTICLES_PER_PAGE, Sort.by("upvoteCount").descending());
//                } else {
//                    throw new Exception("ArticleController.searchArticlesOnHomePage unknown type: " + type);
//                }
//
//                ArticlePagination articlePagination = new ArticlePagination();
//                List<Article> finalArticles = new ArrayList<>();
//                int count = 0;
//
//                for (Tag tag : tags) {
//                    if (count == HOME_PAGE_SEARCH_ARTICLES_PER_PAGE) {
//                        break;
//                    }
//                    Page<Article> articlePage = articleRepository.findByTags_tagId(tag.getTagId(), pageNumWithElements);
//                    List<Article> articles = articlePage.getContent();
//                    for (Article article : articles) {
//                        if (!finalArticles.contains(article)) {
//                            finalArticles.add(article);
//                            count++;
//                        }
//                    }
//                }
//
//                articlePagination.setArticlesByPageIndex(finalArticles);
//                articlePagination.setNumberOfPages(1);
//                return articlePagination;
//            }
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Admins can add a new article
     * ** Tag from articles do not count any points to admins
     *
     * @param article
     * @return
     */
    @Transactional
    @PostMapping(value = "/addArticle",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Article> addArticle(@RequestBody Article article) {
        try {
            String methodName = "ArticleController.addArticle";

            AppUser appUser = article.getAppUser();

            controllerUtils.validateAppUser(appUser, methodName, true);

            article.setUtilTimestamp(new Date());

            // Save tags first (distinctive name)
            List<Tag> tags = controllerUtils.saveDistinctiveTags(article.getTags());
            article.setTags(tags);

            // Set raw content string to article
            article.setContentWithoutHtmlTags(Jsoup.parse(article.getContent()).text());

            article = articleRepository.save(article);

            // Note: this uploaded file are already saved on GG Cloud
            // This requested question will have UploadedFile objects => save info of this question to that UploadedFile
            List<UploadedFile> uploadedFiles = article.getUploadedFiles();

            if (uploadedFiles != null) {
                for (UploadedFile uploadedFile : uploadedFiles) {
                    // We still need to save question for this uploaded file
                    uploadedFile.setArticle(article);
                    uploadedFileRepository.save(uploadedFile);
                }
            }

            return ResponseEntity.ok(article);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Admins can update an article
     *
     * @param updatedArticle
     * @return
     */
    @PutMapping("/updateArticle/{articleId}")
    @Transactional
    public ResponseEntity<Article> updateArticle(
            @RequestBody Article updatedArticle,
            @PathVariable Long articleId) {
        try {
            String methodName = "ArticleController.updateArticle";

            Article oldArticle = articleRepository.findById(articleId)
                    .orElseThrow(() -> new Exception(methodName + ": Not found any article with id: " + articleId));

            // Save tags first
            List<Tag> tags = controllerUtils.saveDistinctiveTags(updatedArticle.getTags());

            // Update values
            oldArticle.setTitle(updatedArticle.getTitle());
            oldArticle.setContent(updatedArticle.getContent());
            oldArticle.setTags(tags);
            oldArticle.setUtilTimestamp(new Date());

            // Delete old images from DB and delete file on google cloud storage
            List<UploadedFile> oldUploadedFiles = oldArticle.getUploadedFiles();
            for (UploadedFile oldUploadedFile : oldUploadedFiles) {
                fileController.deleteFile(oldUploadedFile);
            }

            // This requested article will have UploadedFile objects => save info of this question to that UploadedFile
            List<UploadedFile> newUploadedFiles = updatedArticle.getUploadedFiles();
            oldArticle.setUploadedFiles(newUploadedFiles);

            // Save to database
            Article resultArticle = articleRepository.save(oldArticle);

            // Set article_id for these new uploaded files
            if (newUploadedFiles != null) {
                for (UploadedFile uploadedFile : newUploadedFiles) {
                    uploadedFile.setArticle(resultArticle);
                    uploadedFileRepository.save(uploadedFile);
                }
            }

            return ResponseEntity.ok(resultArticle);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Admins can delete an article
     *
     * @return
     */
    @DeleteMapping("/deleteArticle/{articleId}")
    @Transactional
    public Map<String, String> deleteArticle(@PathVariable Long articleId) {
        try {
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new Exception("ArticleController.deleteArticle: Not found any article with id: " + articleId));

            // Remove the comments
            List<Comment> comments = article.getComments();
            Iterator<Comment> commentIterator = comments.iterator();

            while (commentIterator.hasNext()) {
                Comment comment = commentIterator.next();
                commentRepository.delete(comment);
            }

            // Delete UploadedFile both from GG cloud and DB
            List<UploadedFile> uploadedFiles = article.getUploadedFiles();
            for (UploadedFile uploadedFile : uploadedFiles) {
                fileController.deleteFile(uploadedFile);
            }

            List<Notification> notifications = notificationRepository.findByArticle_ArticleId(articleId);

            for (Notification notification: notifications) {
                notificationRepository.delete(notification);
            }

            // Delete article
            articleRepository.delete(article);

            List<Tag> tags = article.getTags();

            // After deleting question check if this tag has related questions / articles or not
            for (Tag tag : tags) {
                controllerUtils.removeAppUserTagAndTagIfHasNoRelatedQuestionsOrArticle(tag.getTagId());
            }

            Map<String, String> map = new HashMap<>();
            map.put("articleId", ("" + articleId));
            map.put("deleted", "true");
            return map;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/getTop10ArticlesByUpvoteCount")
    @Transactional
    public ArticlePagination getTop10ArticlesByUpvoteCount() {
        try {
            ArticlePagination articlePagination = new ArticlePagination();
            articlePagination.setArticlesByPageIndex(articleRepository.findTop10ByOrderByUpvoteCountDesc());
            articlePagination.setNumberOfPages(1);
            return articlePagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/getTop10ArticlesByUploadDate")
    @Transactional
    public ArticlePagination getTop10ArticlesByUploadDate() {
        try {
            ArticlePagination articlePagination = new ArticlePagination();
            articlePagination.setArticlesByPageIndex(articleRepository.findTop10ByOrderByUtilTimestampDesc());
            articlePagination.setNumberOfPages(1);
            return articlePagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/usersCommentResubscribeArticles")
    @Transactional
    public void usersCommentResubscribeArticles() {
        try {
            List<Article> articles = articleRepository.findAll();

            for (Article article : articles) {

                // Get distince subscibers of that article
                List<Comment> comments = article.getComments();
                List<AppUser> distinctAppUsers = new ArrayList<>();
                for (Comment comment : comments) {
                    AppUser appUser = comment.getAppUser();

                    // Because admin post articles so he does not need to sub
                    if (!Role.ADMIN.getValue().equalsIgnoreCase(appUser.getRole())) {
                        if (!distinctAppUsers.contains(appUser)) {
                            distinctAppUsers.add(appUser);
                        }
                    }
                }

                List<AppUser> oldSubscribers = article.getSubscribers();
                List<AppUser> newSubscribers = new ArrayList<>();

                for (AppUser appUser : oldSubscribers) {
                    if (!newSubscribers.contains(appUser)) {
                        newSubscribers.add(appUser);
                    }
                }

                for (AppUser appUser : distinctAppUsers) {
                    if (!newSubscribers.contains(appUser)) {
                        newSubscribers.add(appUser);
                    }
                }

                // Then make them subscribe
                article.setSubscribers(newSubscribers);
                articleRepository.save(article);
            }

        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewArticlesByTag/{type}/{tagId}/{pageNumber}")
    @Transactional
    public ArticlePagination viewArticlesByTag(@PathVariable String type, @PathVariable Long tagId, @PathVariable int pageNumber) {
        try {
            String methodName = "Article.viewQuestionsByTag: ";

            tagRepository.findById(tagId)
                    .orElseThrow(() -> new Exception(methodName + "cannot find any tags by tagid: " + tagId));

            PageRequest pageNumWithElements;

            if (SORT_DATE.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("utilTimestamp").descending());
            } else if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("viewCount").descending());
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("upvoteCount").descending());
            } else {
                throw new Exception(methodName + " unknown type: " + type);
            }

            Page<Article> articles = articleRepository.findByTags_tagId(tagId, pageNumWithElements);

            // Return pagination objects
            ArticlePagination articlePagination = new ArticlePagination();
            long numberOfArticlesByTagId = articleRepository.countNumberOfArticlesByTagId(tagId);
            long numberOfPages = 0;
            if (numberOfArticlesByTagId % ARTICLES_PER_PAGE == 0) {
                numberOfPages = numberOfArticlesByTagId / ARTICLES_PER_PAGE;
            } else {
                numberOfPages = (numberOfArticlesByTagId / ARTICLES_PER_PAGE) + 1;
            }

            articlePagination.setArticlesByPageIndex(articles.getContent());
            articlePagination.setNumberOfPages(Integer.parseInt("" + numberOfPages));
            articlePagination.setNumberOfContents(Integer.parseInt("" + numberOfArticlesByTagId));

            return articlePagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewRelatedArticles/{articleId}")
    @Transactional
    public ArticlePagination viewRelatedArticles(@PathVariable Long articleId) {
        try {
            Article originArticle = articleRepository.findById(articleId)
                    .orElseThrow(() -> new Exception("ArticleController.viewRelatedArticles: cannot find any article with id: " + articleId));

            List<BigInteger> tagsByArticleId = tagRepository.listTagIdByArticleId(articleId);
            List<Article> recommendedArticles = new ArrayList<>();

            // This list previousIds to prevent choosing duplicate articles
            List<Long> previousArticleIds = new ArrayList<>();
            previousArticleIds.add(articleId);

            if (tagsByArticleId != null) {
                List<Long> listTagIdsHaveMoreThan2Articles = new ArrayList<>();
                for (BigInteger tagBigInteger : tagsByArticleId) {
                    Long tagId = tagBigInteger.longValue();
                    Integer numberOfArticlesByTagId = articleRepository.countNumberOfArticlesByTagId(tagId);

                    if (numberOfArticlesByTagId != null && numberOfArticlesByTagId >= 2) {
                        listTagIdsHaveMoreThan2Articles.add(tagId);
                    }
                }

                Collections.shuffle(listTagIdsHaveMoreThan2Articles);

                listTagIdsHaveMoreThan2Articles:
                {
                    for (Long tagId : listTagIdsHaveMoreThan2Articles) {
                        List<Article> articlesByTagId =
                                articleRepository.findTop5ByTags_tagIdAndArticleIdNotInOrderByViewCountDescUpvoteCountDesc(tagId, previousArticleIds);

                        // If we cannot find any articles
                        if (articlesByTagId == null) {
                            continue;
                        }
                        // If we do find articles
                        else {
                            // If this tagId has 5 articles
                            if (articlesByTagId.size() == 5) {
                                recommendedArticles.addAll(articlesByTagId);
                                break;
                            }
                            // else continue searching other tags until reach 5
                            else {
                                for (Article article : articlesByTagId) {
                                    previousArticleIds.add(article.getArticleId());
                                    recommendedArticles.add(article);
                                    if (recommendedArticles.size() == NUMBER_OF_RECOMMENDED_ARTICLES) {
                                        break listTagIdsHaveMoreThan2Articles;
                                    }
                                }
                            }
                        }
                    }
                }

            }

            ArticlePagination articlePagination = new ArticlePagination();
            articlePagination.setArticlesByPageIndex(recommendedArticles);
            articlePagination.setNumberOfPages(1);

            return articlePagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/addContentWithoutHtmlTagsAndUpvoteCount")
    @Transactional
    public void addContentWithoutHtmlTags() {
        try {
            List<Article> articles = articleRepository.findAll();

            for (Article article : articles) {
                article.setContentWithoutHtmlTags(Jsoup.parse(article.getContent()).text());
                article.setUpvoteCount(article.getUpvotedUserIds().size());
                articleRepository.save(article);
            }
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
