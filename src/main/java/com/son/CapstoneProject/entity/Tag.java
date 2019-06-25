package com.son.CapstoneProject.entity;

import com.fasterxml.jackson.annotation.*;
import com.son.CapstoneProject.entity.login.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
//        property  = "tagId",
//        scope     = Long.class)
public class Tag {

    @Id
    @GeneratedValue
    private Long tagId;

    @Column(columnDefinition = "nvarchar(20)")
    private String name;

    @Column(columnDefinition = "ntext")
    private String description;

//    @JsonIgnore
    @JsonBackReference(value = "questions")
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
    private List<Question> questions;

//    @JsonIgnore
    @JsonBackReference(value = "articles")
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
    private List<Article> articles;

    private int totalPoint;
}
