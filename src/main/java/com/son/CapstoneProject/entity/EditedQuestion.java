package com.son.CapstoneProject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.son.CapstoneProject.entity.login.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EditedQuestion implements Serializable {

    @Id
    @GeneratedValue
    private Long editedQuestionId;

    @JsonBackReference(value = "question")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionId", foreignKey = @ForeignKey(name = "FK_EDITEDQUESTION_QUESTION"))
    private Question question; // original question

    @Column(columnDefinition = "nvarchar(100)")
    private String title;

    @Column(columnDefinition = "ntext")
    private String content;

    // Many questions can be asked by an user
    @JsonBackReference(value = "appUser")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "FK_EDITEDQUESTION_APPUSER"))
    private AppUser appUser;

//    @JsonManagedReference
    @ManyToMany(fetch = FetchType.LAZY/*, cascade = CascadeType.ALL*/)
    private List<Tag> tags;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date utilTimestamp;

//    boolean isApproved;
}

