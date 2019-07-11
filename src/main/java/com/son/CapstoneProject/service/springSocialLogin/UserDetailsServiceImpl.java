package com.son.CapstoneProject.service.springSocialLogin;

//import com.son.CapstoneProject.entity.login.AppUser;
//import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
//import com.son.CapstoneProject.repository.loginRepository.AppRoleDAO;
//import com.son.CapstoneProject.social.SocialUserDetailsImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Primary;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Since this will be executed on UI => comment this class

//@Service
//@Transactional
//@Primary
public class UserDetailsServiceImpl
        /*implements UserDetailsService*/ {

//    @Autowired
//    private AppUserRepository appUserRepository;
//
//    @Autowired
//    private AppRoleDAO appRoleDAO;
//
//    @Override
//    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
//
//        System.out.println("UserDetailsServiceImpl.loadUserByUsername=" + userName);
//
//        AppUser appUser = appUserRepository.findByUserName(userName);
//
//        if (appUser == null) {
//            System.out.println("User not found! " + userName);
//            throw new UsernameNotFoundException("User " + userName + " was not found in the database");
//        }
//
//        System.out.println("Found User: " + appUser);
//
//        // [ROLE_USER, ROLE_ADMIN,..]
//        List<String> roleNames = this.appRoleDAO.getRoleNamesByUserId(appUser.getUserId());
//
//        SocialUserDetailsImpl userDetails = new SocialUserDetailsImpl(appUser, roleNames);
//
//        return userDetails;
//    }

}