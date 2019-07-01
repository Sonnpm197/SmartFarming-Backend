package com.son.CapstoneProject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.son.CapstoneProject.entity.login.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Comment implements Serializable {

    @Id
    @GeneratedValue
    private Long commentId;

    @Column(columnDefinition = "ntext")
    private String content;

    @JsonBackReference(value = "appUser")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "FK_COMMENT_APPUSER"))
    private AppUser appUser;

    @JsonBackReference(value = "article")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articleId", foreignKey = @ForeignKey(name = "FK_COMMENT_ARTICLE"))
    private Article article;

    @JsonBackReference(value = "question")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionId", foreignKey = @ForeignKey(name = "FK_COMMENT_QUESTION"))
    private Question question;

    @JsonBackReference(value = "answer")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answerId", foreignKey = @ForeignKey(name = "FK_COMMENT_ANSWER"))
    private Answer answer;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date utilTimestamp;
}
