package com.son.CapstoneProject.entity.adminChart;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SystemChartInfo {

    private String chartByDate;
    private String chartByMonth;
    private String chartByYear;

    private Integer totalViewCount;
    private Integer totalUpvoteCount;
    private Integer totalNewAccount; // in 1 week
    private Integer totalInactiveAccount; // inactive in 1 month
}
