package com.son.CapstoneProject.entity.pagination;

import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.login.AppUser;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AppUserPagination extends Pagination {

    private List<AppUser> appUsersByPageIndex = new ArrayList<>();

}
