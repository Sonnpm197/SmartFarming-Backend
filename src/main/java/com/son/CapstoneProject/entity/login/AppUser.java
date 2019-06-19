package com.son.CapstoneProject.entity.login;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.son.CapstoneProject.entity.Answer;
import com.son.CapstoneProject.entity.Question;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "App_User",
        uniqueConstraints = {
                @UniqueConstraint(name = "APP_USER_UK", columnNames = "User_Name"),
                @UniqueConstraint(name = "APP_USER_UK2", columnNames = "Email")
        }
)
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AppUser {

    @Id
    @GeneratedValue
    @Column(name = "User_Id", nullable = false)
    private Long userId;

    @Column(name = "User_Name", length = 36, nullable = false)
    private String userName;

    @Column(name = "Email", length = 128, nullable = false)
    private String email;

    @Column(name = "First_Name", length = 36, nullable = true)
    private String firstName;

    @Column(name = "Last_Name", length = 36, nullable = true)
    private String lastName;

    @Column(name = "Encrypted_Password", length = 128, nullable = false)
    private String encryptedPassword;

    @Column(name = "Enabled", length = 1, nullable = false)
    private boolean enabled;

    // An user can ask many questions
    @JsonIgnore
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "appUser")
    private List<Question> questions = new ArrayList<>();

    // An user can have many answers
    @JsonIgnore
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "appUser")
    private List<Answer> answers = new ArrayList<>();
}