package com.son.CapstoneProject.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserChartInfo {

    private String date;

    private int numberOfQuestion;
    private int numberOfAnswer;
    private int numberOfComment;

    private int totalQuestionReputation;
    private int totalAnswerReputation;
    private int totalCommentReputation;

    private int totalQuestionViewCount;
}
