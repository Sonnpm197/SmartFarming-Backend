package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.pagination.TagPagination;
import com.son.CapstoneProject.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.son.CapstoneProject.common.ConstantValue.SORT_UPVOTE_COUNT;
import static com.son.CapstoneProject.common.ConstantValue.SORT_VIEW_COUNT;
import static com.son.CapstoneProject.common.ConstantValue.TAGS_PER_PAGE;

@RestController
@RequestMapping("/tag")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class TagController {

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);

    @Autowired
    private TagRepository tagRepository;

    @GetMapping("/viewTop5TagsByViewCount")
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
    public TagPagination findAllTags(@PathVariable String type, @PathVariable int pageNumber) {
        try {
            PageRequest pageNumWithElements = PageRequest.of(pageNumber, TAGS_PER_PAGE);
            Page<Tag> tagPage;

            if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
                tagPage = tagRepository.findAllByOrderByViewCountDesc(pageNumWithElements);
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
                tagPage = tagRepository.findAllByOrderByReputationDesc(pageNumWithElements);
            } else {
                throw new Exception("Unknown type to findAllTags: " + type);
            }

            List<Tag> tags = tagPage.getContent();
            int size = tags.size();
            int numberOfPages;

            if (size % TAGS_PER_PAGE == 0) {
                numberOfPages = size / TAGS_PER_PAGE;
            } else {
                numberOfPages = size / TAGS_PER_PAGE + 1;
            }

            TagPagination tagPagination = new TagPagination();
            tagPagination.setTagsByPageIndex(tags);
            tagPagination.setNumberOfPages(numberOfPages);
            return tagPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
