package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.login.SocialUserInformation;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import com.son.CapstoneProject.repository.loginRepository.SocialUserInformationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@Transactional
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class LoginController {

//    private List<String> adminEmails = new ArrayList<>(Arrays.asList("sonnpmse04810@fpt.edu.vn"));
//
//    @Autowired
//    private AppUserDAO appUserDAO;
//
    @Autowired
    private AppUserRepository appUserRepository;
//
//    @Autowired
//    private ConnectionFactoryLocator connectionFactoryLocator;
//
//    @Autowired
//    private UsersConnectionRepository userConnectionRepository;
//

    @Autowired
    private SocialUserInformationRepository socialUserInformationRepository;

    @GetMapping({"/", "/test"})
    public String test() {
        return "Welcome to my project";
    }

    /**
     * Save socialUserInformation from Angular
     * @param socialUserInformation
     * @return
     */
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<AppUser> login(@RequestBody SocialUserInformation socialUserInformation) throws Exception {
        String methodName = "LoginController.login";
        if (socialUserInformation == null) {
            throw new Exception(methodName + ": socialUserInformation from request body is null");
        }

        // Check if this social user is existed
        SocialUserInformation existedSocialUser = socialUserInformationRepository.findById(socialUserInformation.getId());

        // This user has existed => return appUser
        if (existedSocialUser != null) {
            Long socialUserInformationId = socialUserInformation.getSocialUserInformationId();
            AppUser appUser = appUserRepository.findBySocialUserInformation_SocialUserInformationId(socialUserInformationId);
            return ResponseEntity.ok(appUser);
        }

        // Save socialUser from angular js first
        socialUserInformation = socialUserInformationRepository.save(socialUserInformation);

        // Then create an appUser
        AppUser appUser = new AppUser();
        appUser.setSocialUserInformation(socialUserInformation);

        // Check if this is an admin or not
        if (socialUserInformation.getEmail() != null
                && ConstantValue.listAdmin.contains(socialUserInformation.getEmail().toLowerCase())) {
            appUser.setRole(ConstantValue.Role.ADMIN.getValue());
        } else {
            appUser.setRole(ConstantValue.Role.USER.getValue());
        }

        return ResponseEntity.ok(appUserRepository.save(appUser));
    }
//
//    @GetMapping(value = "/userInfo", produces = "application/json")
//    public String adminPage(Principal principal) {
//
//        if (principal == null) {
//            return null;
//        }
//
//        String userName = principal.getName();
//
//        return appUserRepository.findByUserName(userName).toString();
//    }
//
//    @GetMapping(value = "/logoutSuccessful")
//    public String logoutSuccessfulPage() {
//        return "Logout successfully";
//    }
//
//    @GetMapping("/403")
//    public String accessDenied(Principal principal) {
//
//        if (principal != null) {
//            UserDetails loggedIn = (UserDetails) ((Authentication) principal).getPrincipal();
//
//            String message = "Hi " + principal.getName() //
//                    + "<br> You do not have permission to access this page!";
//
//            return message;
//        }
//
//        return "Permission Denied";
//    }
//
//    @GetMapping("/login")
//    public String login() {
//        return "/auth/facebook\n/auth/google";
//    }
//
//    @GetMapping(value = "/signin")
//    public String signIn(@RequestParam(value = "error", required = false) String error) {
//        return "Signin";
//    }
//
//    @GetMapping("/signup")
//    public AppUserForm signupPage(WebRequest request, HttpServletResponse response) {
//
//        ProviderSignInUtils providerSignInUtils
//                = new ProviderSignInUtils(connectionFactoryLocator, userConnectionRepository);
//
//        // Retrieve social networking information.
//        Connection<?> connection = providerSignInUtils.getConnectionFromSession(request);
//
//        AppUserForm appUserForm = null;
//
//        if (connection != null) {
//            appUserForm = new AppUserForm(connection);
//        } else {
//            appUserForm = new AppUserForm();
//        }
//
//        appUserForm.setPassword("defaultPassword");
//
//        List<String> roleNames = new ArrayList<String>();
//
//        if (appUserForm.getEmail() != null) {
//            if (adminEmails.contains(appUserForm.getEmail().toLowerCase())) {
//                roleNames.add(AppRole.ROLE_ADMIN);
//            }
//        }
//
//        // By default every user has this role
//        roleNames.add(AppRole.ROLE_USER);
//
//        AppUser registered = null;
//
//        try {
//            registered = appUserDAO.registerNewUserAccount(appUserForm, roleNames);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        if (appUserForm.getSignInProvider() != null) {
//            // (Spring Social API): If user login by social networking.
//            // This method saves social networking information to the UserConnection table.
//            providerSignInUtils.doPostSignUp(registered.getUserName(), request);
//        }
//
//        // After registration is complete, automatic login.
//        logInUser(registered, roleNames);
//
//        try {
//            response.sendRedirect("/userInfo");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return appUserForm;
//    }
//
//    private void logInUser(AppUser user, List<String> roleNames) {
//
//        SocialUserDetails userDetails = new SocialUserDetailsImpl(user, roleNames);
//
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                userDetails,
//                null,
//                userDetails.getAuthorities());
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//    }

}