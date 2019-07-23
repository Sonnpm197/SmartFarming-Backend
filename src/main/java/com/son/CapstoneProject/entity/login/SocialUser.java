package com.son.CapstoneProject.entity.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

// This social user from Angular login

@Entity
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SocialUser {

    @Id
    @GeneratedValue
    private Long socialUserId;

    private String id; // social user Id by facebook and google

    private String provider;

    private String email;

    @Column(columnDefinition = "nvarchar(255)")
    private String name;

    private String photoUrl;

    @Column(columnDefinition = "nvarchar(255)")
    private String firstName;

    @Column(columnDefinition = "nvarchar(255)")
    private String lastName;

    @Column(columnDefinition = "ntext")
    private String authToken;

    @Column(columnDefinition = "ntext")
    private String idToken;

    private String authorizationCode;

}
