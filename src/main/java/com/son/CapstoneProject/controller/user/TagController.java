package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.entity.Tag;
import com.son.CapstoneProject.common.entity.pagination.TagPagination;
import com.son.CapstoneProject.common.entity.search.TagSearch;
import com.son.CapstoneProject.repository.TagRepository;
import com.son.CapstoneProject.repository.searchRepository.HibernateSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.son.CapstoneProject.common.ConstantValue.*;

@RestController
@RequestMapping("/tag")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class TagController {

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private HibernateSearchRepository hibernateSearchRepository;

    @GetMapping("/viewTop5TagsByViewCount")
    @Transactional
    public TagPagination findTop5ByOrderByViewCount() {
        try {
            List<Tag> tags = tagRepository.findTop5ByOrderByViewCountDesc();
            TagPagination tagPagination = new TagPagination();
            tagPagination.setTagsByPageIndex(tags);
            tagPagination.setNumberOfPages(1);
            return tagPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewTop10Tags/{type}")
    @Transactional
    public TagPagination viewTop10Tags(@PathVariable String type) {
        try {
            List<Tag> tags;
            if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
                tags = tagRepository.findTop10ByOrderByViewCountDesc();
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
                tags = tagRepository.findTop10ByOrderByReputationDesc();
            } else {
                throw new Exception("Unknown type to view top 10 tags: " + type);
            }
            TagPagination tagPagination = new TagPagination();
            tagPagination.setTagsByPageIndex(tags);
            tagPagination.setNumberOfPages(1);
            return tagPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/findAllTags/{type}/{pageNumber}")
    @Transactional
    public TagPagination findAllTags(@PathVariable String type, @PathVariable int pageNumber) {
        try {
            PageRequest pageNumWithElements = PageRequest.of(pageNumber, TAGS_PER_PAGE);
            Page<Tag> tagPage;

            if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
                tagPage = tagRepository.findAllByOrderByViewCountDesc(pageNumWithElements);
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
                tagPage = tagRepository.findAllByOrderByReputationDesc(pageNumWithElements);
            } else if (SORT_TREND.equalsIgnoreCase(type)) {
                tagPage = tagRepository.findAllByOrderByIncreasementOneWeekAgoTillNowDesc(pageNumWithElements);
            } else {
                throw new Exception("Unknown type to findAllTags: " + type);
            }

            List<Tag> tags = tagPage.getContent();

            // Get 7 days ago
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, -7);
            String searchDate = sdf.format(calendar.getTime());

            // Then count view 7 days ago
            for (Tag tag : tags) {
                List<Object[]> viewCountQuestionAndArticle =
                        tagRepository.countTotalQuestionViewAndArticleViewBeforeDate(searchDate, tag.getTagId());

                int questionViewCountOneWeekAgo = viewCountQuestionAndArticle.get(0)[0] == null ? 0 : Integer.parseInt(viewCountQuestionAndArticle.get(0)[0].toString());
                int articleViewCountOneWeekAgo = viewCountQuestionAndArticle.get(0)[1] == null ? 0 : Integer.parseInt(viewCountQuestionAndArticle.get(0)[1].toString());

                tag.setViewCountOneWeekAgo(questionViewCountOneWeekAgo + articleViewCountOneWeekAgo);
                tag.setIncreasementOneWeekAgoTillNow(tag.getViewCount() - tag.getViewCountOneWeekAgo());
                tagRepository.save(tag);
            }

            Integer size = tagRepository.countNumberOfTags();
            if (size == null) {
                size = 0;
            }
            int numberOfPages;

            if (size % TAGS_PER_PAGE == 0) {
                numberOfPages = size / TAGS_PER_PAGE;
            } else {
                numberOfPages = size / TAGS_PER_PAGE + 1;
            }

            TagPagination tagPagination = new TagPagination();
            tagPagination.setTagsByPageIndex(tags);
            tagPagination.setNumberOfPages(numberOfPages);
            tagPagination.setNumberOfContents(size);
            return tagPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/findAllTagsForRecommend")
    @Transactional
    public List<Tag> findAllTagsForRecommend() {
        try {
            return tagRepository.findAll();
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/searchTagsWhileTyping")
    @Transactional
    public TagPagination searchTagsByPageIndex(@RequestBody TagSearch tagSearch) {
        try {
            return (TagPagination) hibernateSearchRepository.recommendTagNameWhileTyping(
                    tagSearch.getTextSearch(),
                    TAG,
                    "name" // search tag by name
            );
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/findTagById/{tagId}")
    @Transactional
    public Tag findTagById(@PathVariable("tagId") Long tagId) {
        try {
            return tagRepository.findById(tagId).get();
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
