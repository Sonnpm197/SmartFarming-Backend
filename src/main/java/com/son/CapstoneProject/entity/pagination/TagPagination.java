package com.son.CapstoneProject.entity.pagination;

import com.son.CapstoneProject.entity.Tag;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TagPagination extends Pagination {

    private List<Tag> tagsByPageIndex = new ArrayList<>();

}
