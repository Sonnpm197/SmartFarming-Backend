package com.son.CapstoneProject.entity.pagination;

import com.son.CapstoneProject.entity.Report;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReportPagination extends Pagination {

    private List<Report> reportsByPageIndex;

}
