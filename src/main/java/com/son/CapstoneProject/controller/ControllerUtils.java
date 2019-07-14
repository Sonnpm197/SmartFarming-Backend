package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.login.AppRole;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.form.AppUserForm;
import com.son.CapstoneProject.repository.TagRepository;
import com.son.CapstoneProject.repository.loginRepository.AppUserDAO;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ControllerUtils {

    private Logger logger = Logger.getLogger(ControllerUtils.class.getSimpleName());

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AppUserRepository appUserRepository;

//    @Autowired
//    private AppUserDAO appUserDAO;

    /**
     * Only save tags which don't exist but still return all tags of the questions
     * @param tags
     * @return
     */
    public List<Tag> saveDistinctiveTags(List<Tag> tags) {
        List<Tag> processedList = new ArrayList<>();
        if (tags != null) {
            for (Tag tag : tags) {
                if (tag.getName() != null) {
                    tag.setName(tag.getName().toLowerCase().trim());
                }
                if (tag.getDescription() != null) {
                    tag.setDescription(tag.getDescription().toLowerCase().trim());
                }

                // Do not save if that tag existed
                Tag tagByName = tagRepository.findByName(tag.getName());
                if (tagByName != null) {
                    processedList.add(tagByName);
                    continue;
                }

                tag = tagRepository.save(tag);
                processedList.add(tag);
            }
        }

        return processedList;
    }

    /**
     * Save new anonymous user based on his ip address or retrieve existed one
     *
     * @param ipAddress
     * @return
     */
    public AppUser saveOrReturnAnonymousUser(String ipAddress) {

        if (ipAddress == null) {
            // TODO: logger
            return null;
        }

        // === Since we dont use Social Login by Spring, comment this === //

//        AppUserForm myForm = new AppUserForm();
//        myForm.setPassword("defaultPassword");
//        myForm.setAnonymous(true);
//        myForm.setIpAddress(ipAddress);
//
//        List<String> roleNames = new ArrayList<>();
//        // By default every user has this role
//        roleNames.add(AppRole.ROLE_USER);

        // ==============================================================//

        // Check if this anonymous user existed
        AppUser appUserByIpAddress = appUserRepository.findByIpAddress(ipAddress);
        if (appUserByIpAddress != null) {
            return appUserByIpAddress;
        }

        // Else if we cannot find user by ip address => create new one
        // ** Anonymous user is automatically a normal user
        try {
//            return appUserDAO.registerNewUserAccount(myForm, roleNames);

            AppUser newAppUser = new AppUser();
            newAppUser.setAnonymous(true);
            newAppUser.setIpAddress(ipAddress);
            newAppUser.setRole(ConstantValue.Role.ANONYMOUS.getValue());
            return appUserRepository.save(newAppUser);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * checkUserId = true => check if appUser inside request has userId or not
     * @param appUser
     * @param methodName
     * @param checkUserId
     * @throws Exception
     */
    public void validateAppUser(AppUser appUser, String methodName, boolean checkUserId) throws Exception {
        if (appUser == null) {
            String message = methodName + ": Request body has no appUser";
            logger.info(message);
            throw new Exception(message);
        }

        if (checkUserId) {
            if (appUser.getUserId() == null) {
                String message = methodName + ": AppUser from request body has no user id";
                logger.info(message);
                throw new Exception(message);
            }
        }
    }

}
