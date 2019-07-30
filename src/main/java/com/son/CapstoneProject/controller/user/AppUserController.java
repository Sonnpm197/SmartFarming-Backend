package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.common.StringUtils;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.login.SocialUser;
import com.son.CapstoneProject.entity.pagination.AppUserPagination;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.son.CapstoneProject.common.ConstantValue.USERS_PER_PAGE;

@RestController
@RequestMapping("/userDetail")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class AppUserController {

    private static final Logger logger = LoggerFactory.getLogger(AppUserController.class);

    @Autowired
    private AppUserRepository appUserRepository;

    @GetMapping("/viewNumberOfPages")
    public long viewNumberOfPages() {
        try {
            long numberOfUsers = appUserRepository.count();
            if (numberOfUsers % USERS_PER_PAGE == 0) {
                return numberOfUsers / USERS_PER_PAGE;
            } else {
                return (numberOfUsers / USERS_PER_PAGE) + 1;
            }
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewNumberOfUsers")
    public long viewNumberOfUsers() {
        try {
            return appUserRepository.count();
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewTop3UsersByReputation")
    public AppUserPagination viewTop3UsersByReputation() {
        try {
            List<AppUser> appUsers = appUserRepository.findTop3ByRoleOrderByReputationDesc(ConstantValue.Role.USER.getValue());

            AppUserPagination appUserPagination = new AppUserPagination();
            appUserPagination.setAppUsersByPageIndex(appUsers);
            appUserPagination.setNumberOfPages(1);
            return appUserPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewUser/{userId}")
    public AppUser viewUser(@PathVariable Long userId) {
        try {
            return appUserRepository.findById(userId).get();
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewUsers/{pageNumber}")
    public AppUserPagination viewUsers(@PathVariable int pageNumber) {
        try {
            PageRequest pageNumWithElements = PageRequest.of(
                    pageNumber,
                    ConstantValue.USERS_PER_PAGE,
                    Sort.by("reputation").descending());

            AppUserPagination userPagination = new AppUserPagination();
            userPagination.setAppUsersByPageIndex(appUserRepository.findAll(pageNumWithElements).getContent());
            userPagination.setNumberOfPages(Integer.parseInt("" + viewNumberOfPages()));
            return userPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * On UI there are many fields are hidden, so we must include the userId in path
     * to find the full data
     *
     * @param updatedAppUser
     * @param userId
     * @return
     */
    @PostMapping("/editProfile/{userId}")
    @Transactional
    public AppUser editProfile(@RequestBody AppUser updatedAppUser, @PathVariable Long userId) {
        try {
            AppUser appUser = appUserRepository.findById(userId).get();
            SocialUser oldSocialUserInformation = appUser.getSocialUser();
            SocialUser newSocialUserInformation = updatedAppUser.getSocialUser();

            if (!StringUtils.isNullOrEmpty(newSocialUserInformation.getEmail())) {
                oldSocialUserInformation.setEmail(newSocialUserInformation.getEmail());
            }

            if (!StringUtils.isNullOrEmpty(newSocialUserInformation.getFirstName())) {
                oldSocialUserInformation.setEmail(newSocialUserInformation.getFirstName());
            }

            if (!StringUtils.isNullOrEmpty(newSocialUserInformation.getLastName())) {
                oldSocialUserInformation.setEmail(newSocialUserInformation.getLastName());
            }

            if (!StringUtils.isNullOrEmpty(newSocialUserInformation.getPhotoUrl())) {
                oldSocialUserInformation.setEmail(newSocialUserInformation.getPhotoUrl());
            }

            appUser.setSocialUser(oldSocialUserInformation);
            appUser.setCvUrl(updatedAppUser.getCvUrl());
            return appUserRepository.save(appUser);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
