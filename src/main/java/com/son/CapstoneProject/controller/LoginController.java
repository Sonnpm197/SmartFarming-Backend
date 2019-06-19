package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.entity.login.AppRole;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.form.AppUserForm;
import com.son.CapstoneProject.repository.loginRepository.AppUserDAO;
import com.son.CapstoneProject.social.SocialUserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@Transactional
//@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class LoginController {

    @Autowired
    private AppUserDAO appUserDAO;

    @Autowired
    private ConnectionFactoryLocator connectionFactoryLocator;

    @Autowired
    private UsersConnectionRepository userConnectionRepository;

    @Autowired
    private HttpSession httpSession;

    @GetMapping({"/", "/test"})
    public String test() {
        return "Welcome to my project";
    }

    @GetMapping(value = "/userInfo", produces="application/json")
    public String adminPage(Principal principal) {

        // After user login successfully.
        String userName = principal.getName();

        System.out.println("User Name: " + userName);

        UserDetails loggedIn = (UserDetails) ((Authentication) principal).getPrincipal();

        String userInfo = userDetailAsString(loggedIn);
        
        return userInfo;
    }

    @GetMapping(value = "/logoutSuccessful")
    public String logoutSuccessfulPage() {
        return "Logout successfully";
    }

    @GetMapping("/403")
    public String accessDenied(Principal principal) {

        if (principal != null) {
            UserDetails loggedIn = (UserDetails) ((Authentication) principal).getPrincipal();

            String userInfo = userDetailAsString(loggedIn);

            String message = "Hi " + principal.getName() //
                    + "<br> You do not have permission to access this page!";

            return message;
        }

       return "Permission Denied";
    }

    @GetMapping("/login")
    public String login() {
        return "/auth/facebook\n/auth/google";
    }

    @GetMapping(value = "/signin")
    public String signIn(@RequestParam(value = "error", required = false) String error) {
        return "Signin";
    }

    @GetMapping("/signup")
    public AppUserForm signupPage(WebRequest request, HttpServletResponse response) {

        ProviderSignInUtils providerSignInUtils
                = new ProviderSignInUtils(connectionFactoryLocator, userConnectionRepository);

        // Retrieve social networking information.
        Connection<?> connection = providerSignInUtils.getConnectionFromSession(request);

        AppUserForm myForm = null;

        if (connection != null) {
            myForm = new AppUserForm(connection);
        } else {
            myForm = new AppUserForm();
        }

        myForm.setPassword("defaultPassword");

        List<String> roleNames = new ArrayList<String>();
        // By default every user has this role
        roleNames.add(AppRole.ROLE_USER);

        AppUser registered = null;

        try {
            registered = appUserDAO.registerNewUserAccount(myForm, roleNames);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (myForm.getSignInProvider() != null) {
            // (Spring Social API): If user login by social networking.
            // This method saves social networking information to the UserConnection table.
            providerSignInUtils.doPostSignUp(registered.getUserName(), request);
        }

        // After registration is complete, automatic login.
        logInUser(registered, roleNames);

        try {
            response.sendRedirect("/userInfo");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return myForm;
    }

    private void logInUser(AppUser user, List<String> roleNames) {

        SocialUserDetails userDetails = new SocialUserDetailsImpl(user, roleNames);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String userDetailAsString(UserDetails user) {
        StringBuilder sb = new StringBuilder();

        sb.append("UserName:").append(user.getUsername());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        if (authorities != null && !authorities.isEmpty()) {
            sb.append(" (");
            boolean first = true;
            for (GrantedAuthority a : authorities) {
                if (first) {
                    sb.append(a.getAuthority());
                    first = false;
                } else {
                    sb.append(", ").append(a.getAuthority());
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }
}