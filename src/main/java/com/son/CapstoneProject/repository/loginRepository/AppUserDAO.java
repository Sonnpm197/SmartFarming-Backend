package com.son.CapstoneProject.repository.loginRepository;

//import com.son.CapstoneProject.entity.login.AppUser;
//import com.son.CapstoneProject.form.AppUserForm;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

// Since this will be executed on UI => comment this class

//@Repository
public class AppUserDAO {

//    @Autowired
//    private EntityManager entityManager;
//
//    @Autowired
//    private AppRoleDAO appRoleDAO;
//
//    public AppUser registerNewUserAccount(AppUserForm appUserForm, List<String> roleNames) {
//        AppUser appUser = new AppUser();
//        appUser.setUserName(appUserForm.getUserName());
//        appUser.setEmail(appUserForm.getEmail());
//        appUser.setFirstName(appUserForm.getFirstName());
//        appUser.setLastName(appUserForm.getLastName());
//        appUser.setEnabled(true);
//
//        if (appUserForm.isAnonymous()) {
//            appUser.setAnonymous(true);
//            appUser.setIpAddress(appUserForm.getIpAddress());
//        } else {
//            appUser.setAnonymous(false);
//        }
//
//        String encryptedPassword = new BCryptPasswordEncoder().encode(appUserForm.getPassword());
//        appUser.setEncryptedPassword(encryptedPassword);
//        this.entityManager.persist(appUser);
//        this.entityManager.flush();
//
//        this.appRoleDAO.createRoleFor(appUser, roleNames);
//
//        return appUser;
//    }

}