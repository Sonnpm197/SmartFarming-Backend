package com.son.CapstoneProject.entity.login;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "App_Role",
        uniqueConstraints = {
                @UniqueConstraint(name = "APP_ROLE_UK", columnNames = "Role_Name")
        }
)
@Getter
@Setter
public class AppRole {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    @Id
    @GeneratedValue
    @Column(name = "Role_Id", nullable = false)
    private Long roleId;

    @Column(name = "Role_Name", length = 30, nullable = false)
    private String roleName;

}