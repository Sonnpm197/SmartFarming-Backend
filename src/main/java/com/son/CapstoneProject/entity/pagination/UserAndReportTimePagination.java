package com.son.CapstoneProject.entity.pagination;

import com.son.CapstoneProject.entity.UserAndReportTime;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserAndReportTimePagination extends Pagination {

    private List<UserAndReportTime> userAndReportTimes = new ArrayList<>();

}
