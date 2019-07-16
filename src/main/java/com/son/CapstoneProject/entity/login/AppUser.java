package com.son.CapstoneProject.entity.login;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.son.CapstoneProject.entity.Answer;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Tag;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "App_User")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AppUser {

    @Id
    @GeneratedValue
    @Column(name = "User_Id", nullable = false)
    private Long userId;

    private String ipAddress;

    // Because we dont use Spring Social login => directly use SocialUser from angular
//    @Column(name = "User_Name", length = 36)
//    private String userName; // User Id from Facebook or google

    private boolean anonymous;

    @JoinColumn(name = "social_id", foreignKey = @ForeignKey(name = "FK_APPUSER_SOCIALUSER"))
    @OneToOne(fetch = FetchType.LAZY)
    private SocialUser socialUser;

//    @Column(name = "Email", length = 128)
//    private String email;
//
//    @Column(name = "First_Name", length = 36)
//    private String firstName;
//
//    @Column(name = "Last_Name", length = 36)
//    private String lastName;
//
//    @JsonIgnore
//    @Column(name = "Encrypted_Password", length = 128)
//    private String encryptedPassword;
//
//    @Column(name = "Enabled", length = 1)
//    private boolean enabled;

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

    private int reputation;

    private int viewCount;

//    private String profileImageUrl;

    private String cvUrl;

    // Because we dont use Spring Social login => an user needs only 1 role
    private String role;

    @Override
    public String toString() {
        return "{" +
                    "\"userId\":" + "\"" + userId + "\"" +
                    ", \"reputation\":" + "\"" + reputation + "\"" +
                "}";
    }
}