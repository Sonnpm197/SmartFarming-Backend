package com.son.CapstoneProject.service;

import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.repository.loginRepository.AppRoleDAO;
import com.son.CapstoneProject.repository.loginRepository.AppUserDAO;
import com.son.CapstoneProject.social.SocialUserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Primary
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AppUserDAO appUserDAO;

    @Autowired
    private AppRoleDAO appRoleDAO;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        System.out.println("UserDetailsServiceImpl.loadUserByUsername=" + userName);

        AppUser appUser = this.appUserDAO.findAppUserByUserName(userName);

        if (appUser == null) {
            System.out.println("User not found! " + userName);
            throw new UsernameNotFoundException("User " + userName + " was not found in the database");
        }

        System.out.println("Found User: " + appUser);

        // [ROLE_USER, ROLE_ADMIN,..]
        List<String> roleNames = this.appRoleDAO.getRoleNamesByUserId(appUser.getUserId());

        SocialUserDetailsImpl userDetails = new SocialUserDetailsImpl(appUser, roleNames);

        return userDetails;
    }

}