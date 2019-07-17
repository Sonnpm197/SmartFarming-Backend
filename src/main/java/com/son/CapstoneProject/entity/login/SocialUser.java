package com.son.CapstoneProject.entity.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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

    private String name;

    private String photoUrl;

    private String firstName;

    private String lastName;

    @Column(columnDefinition = "ntext")
    private String authToken;

    @Column(columnDefinition = "ntext")
    private String idToken;

    private String authorizationCode;

}