package com.son.CapstoneProject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.son.CapstoneProject.entity.login.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

//import org.codehaus.jackson.annotate.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Answer implements Serializable {

    @Id
    @GeneratedValue
    private Long answerId;

    @Column(columnDefinition = "ntext")
    private String content;

    // Many answers can be replied by an user
//    @JsonBackReference(value = "appUser")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "FK_ANSWER_APPUSER"))
    private AppUser appUser;

    // Many answers for 1 questions
    @JsonBackReference(value = "question")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionId", foreignKey = @ForeignKey(name = "FK_ANSWER_QUESTION"))
    private Question question;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date utilTimestamp;

//    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "answer")
    private List<Comment> comments;

    @ElementCollection
    private List<Long> upvotedUserIds;

    private boolean isAccepted;
}
