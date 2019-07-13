package com.son.CapstoneProject.entity.login;

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
public class SocialUserInformation {

    @Id
    @GeneratedValue
    private Long socialUserInformationId;

    private String id;

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
