package com.son.CapstoneProject.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.*;
//import org.codehaus.jackson.annotate.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Answer implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "ntext")
    private String content;

    // Many answers can be replied by an user
    @JsonBackReference(value = "client")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientEmail", foreignKey = @ForeignKey(name = "FK_ANSWER_CLIENT"))
    private Client client;

    // Many answers for 1 questions
    @JsonBackReference(value = "question")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionId", foreignKey = @ForeignKey(name = "FK_ANSWER_QUESTION"))
    private Question question;

}
