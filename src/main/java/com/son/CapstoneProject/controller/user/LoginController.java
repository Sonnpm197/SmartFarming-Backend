package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.entity.login.AppRole;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.form.AppUserForm;
import com.son.CapstoneProject.repository.loginRepository.AppUserDAO;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import com.son.CapstoneProject.social.SocialUserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@Transactional
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class LoginController {

    private List<String> adminEmails = new ArrayList<>(Arrays.asList("sonnpmse04810@fpt.edu.vn"));

    @Autowired
    private AppUserDAO appUserDAO;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ConnectionFactoryLocator connectionFactoryLocator;

    @Autowired
    private UsersConnectionRepository userConnectionRepository;

    @GetMapping({"/", "/test"})
    public String test() {
        return "Welcome to my project";
    }

    @GetMapping(value = "/userInfo", produces = "application/json")
    public String adminPage(Principal principal) {

        if (principal == null) {
            return null;
        }

        String userName = principal.getName();

        return appUserRepository.findByUserName(userName).toString();
    }

    @GetMapping(value = "/logoutSuccessful")
    public String logoutSuccessfulPage() {
        return "Logout successfully";
    }

    @GetMapping("/403")
    public String accessDenied(Principal principal) {

        if (principal != null) {
            UserDetails loggedIn = (UserDetails) ((Authentication) principal).getPrincipal();

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

        AppUserForm appUserForm = null;

        if (connection != null) {
            appUserForm = new AppUserForm(connection);
        } else {
            appUserForm = new AppUserForm();
        }

        appUserForm.setPassword("defaultPassword");

        List<String> roleNames = new ArrayList<String>();

        if (appUserForm.getEmail() != null) {
            if (adminEmails.contains(appUserForm.getEmail().toLowerCase())) {
                roleNames.add(AppRole.ROLE_ADMIN);
            }
        }

        // By default every user has this role
        roleNames.add(AppRole.ROLE_USER);

        AppUser registered = null;

        try {
            registered = appUserDAO.registerNewUserAccount(appUserForm, roleNames);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (appUserForm.getSignInProvider() != null) {
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

        return appUserForm;
    }

    private void logInUser(AppUser user, List<String> roleNames) {

        SocialUserDetails userDetails = new SocialUserDetailsImpl(user, roleNames);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}