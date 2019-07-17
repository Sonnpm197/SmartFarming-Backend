package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.pagination.TagPagination;
import com.son.CapstoneProject.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tag")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    @GetMapping("/viewTop5TagsByViewCount")
    public TagPagination findTop5ByOrderByViewCount() {
        List<Tag> tags = tagRepository.findTop5ByOrderByViewCountDesc();
        TagPagination tagPagination = new TagPagination();
        tagPagination.setTagsByPageIndex(tags);
        tagPagination.setNumberOfPages(1);
        return tagPagination;
    }

}
