package com.son.CapstoneProject.entity;

import com.son.CapstoneProject.entity.login.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AppUserTag implements Serializable {

    @Id
    @GeneratedValue
    private Long appUserTagId;

    @ManyToOne
    @JoinColumn
    private AppUser appUser;

    @ManyToOne
    @JoinColumn
    private Tag tag;

    private int reputation;

}
