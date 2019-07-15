package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.common.StringUtils;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.login.SocialUserInformation;
import com.son.CapstoneProject.entity.pagination.AppUserPagination;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import static com.son.CapstoneProject.common.ConstantValue.USERS_PER_PAGE;

@RestController
@RequestMapping("/userDetail")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class AppUserController {

    @Autowired
    private AppUserRepository appUserRepository;

    @GetMapping("/viewNumberOfPages")
    public long viewNumberOfPages() {
        long numberOfUsers = appUserRepository.count();
        if (numberOfUsers % USERS_PER_PAGE == 0) {
            return numberOfUsers / USERS_PER_PAGE;
        } else {
            return (numberOfUsers / USERS_PER_PAGE) + 1;
        }
    }

    @GetMapping("/viewNumberOfUsers")
    public long viewNumberOfUsers() {
        return appUserRepository.count();
    }

    @GetMapping("/viewUser/{userId}")
    public AppUser viewUser(@PathVariable Long userId) {
        return appUserRepository.findById(userId).get();
    }

    @GetMapping("/viewUsers/{pageNumber}")
    public AppUserPagination viewUsers(@PathVariable int pageNumber) {
        PageRequest pageNumWithElements = PageRequest.of(
                pageNumber,
                ConstantValue.USERS_PER_PAGE,
                Sort.by("reputation").descending());

        AppUserPagination userPagination = new AppUserPagination();
        userPagination.setAppUsersByPageIndex(appUserRepository.findAll(pageNumWithElements).getContent());
        userPagination.setNumberOfPages(Integer.parseInt("" + viewNumberOfPages()));
        return userPagination;
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
        AppUser appUser = appUserRepository.findById(userId).get();
        SocialUserInformation oldSocialUserInformation = appUser.getSocialUserInformation();
        SocialUserInformation newSocialUserInformation = updatedAppUser.getSocialUserInformation();

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

        appUser.setSocialUserInformation(oldSocialUserInformation);
        appUser.setCvUrl(updatedAppUser.getCvUrl());
        return appUserRepository.save(appUser);
    }
}
