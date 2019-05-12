package com.son.CapstoneProject.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.*;
//import org.codehaus.jackson.annotate.*;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Client implements Serializable {

    @Id
    private String email;

    private String password;

    private String role;

    private String profileName;

    private int reputation;

    // An user can ask many questions
    @JsonIgnore
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "client")
    private List<Question> questions = new ArrayList<>();

    // An user can have many answers
    @JsonIgnore
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "client")
    private List<Answer> answers = new ArrayList<>();

}
