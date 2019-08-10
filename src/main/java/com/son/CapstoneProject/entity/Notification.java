package com.son.CapstoneProject.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.son.CapstoneProject.entity.login.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
/**
 * This class is used when users want to report inappropriate questions
 */
public class Notification {

    @Id
    @GeneratedValue
    private Long notificationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "FK_NOTIFICATION_APPUSER"))
    private AppUser appUserReceiver;

    @Column(columnDefinition = "ntext")
    private String message;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionId", foreignKey = @ForeignKey(name = "FK_NOTIFICATION_QUESTION"))
    private Question question;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articleId", foreignKey = @ForeignKey(name = "FK_NOTIFICATION_QUESTION"))
    private Article article;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date utilTimestamp;

    // This is for delete Question, Answer, Comment from admin
    private boolean isFromAdmin;

    private boolean isSeen;
}
