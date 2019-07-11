package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/userDetail")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class AppUserController {

    @Autowired
    private AppUserRepository appUserRepository;

    @GetMapping("/viewNumberOfUsers")
    public long viewNumberOfUsers() {
        return appUserRepository.count();
    }

    @GetMapping("/viewUser/{userId}")
    public AppUser viewUser(@PathVariable Long userId) {
        return appUserRepository.findById(userId).get();
    }

    @GetMapping("/viewUsers/{pageNumber}")
    public Page<AppUser> viewUsers(@PathVariable int pageNumber) {
        PageRequest pageNumWithElements = PageRequest.of(
                pageNumber,
                ConstantValue.USERS_PER_PAGE,
                Sort.by("userId"));
        return appUserRepository.findAll(pageNumWithElements);
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
    public AppUser editProfile(@PathVariable AppUser updatedAppUser, @PathVariable Long userId) {
        AppUser appUser = appUserRepository.findById(userId).get();
        // TODO: finish this
//        appUser.setFirstName(updatedAppUser.getFirstName());
//        appUser.setLastName(updatedAppUser.getLastName());
//        appUser.setEmail(updatedAppUser.getEmail());
//        appUser.setCvUrl(updatedAppUser.getCvUrl());
//        appUser.setProfileImageUrl(updatedAppUser.getProfileImageUrl());

        return appUserRepository.save(appUser);
    }
}
