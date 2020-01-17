package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.common.StringUtils;
import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.common.entity.AppUserTag;
import com.son.CapstoneProject.common.entity.Question;
import com.son.CapstoneProject.common.entity.Tag;
import com.son.CapstoneProject.common.entity.login.AppUser;
import com.son.CapstoneProject.common.entity.login.SocialUser;
import com.son.CapstoneProject.common.entity.pagination.AppUserPagination;
import com.son.CapstoneProject.common.entity.pagination.QuestionPagination;
import com.son.CapstoneProject.common.entity.pagination.TagPagination;
import com.son.CapstoneProject.repository.AppUserTagRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static com.son.CapstoneProject.common.ConstantValue.*;

@RestController
@RequestMapping("/userDetail")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class AppUserController {

    private static final Logger logger = LoggerFactory.getLogger(AppUserController.class);

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    @GetMapping("/viewNumberOfPages")
    @Transactional
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
    @Transactional
    public long viewNumberOfUsers() {
        try {
            return appUserRepository.count();
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewTop3UsersByReputation")
    @Transactional
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
    @Transactional
    public AppUser viewUser(@PathVariable Long userId) {
        try {
            return appUserRepository.findById(userId).get();
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewUsers/{pageNumber}")
    @Transactional
    public AppUserPagination viewUsers(@PathVariable int pageNumber) {
        try {
            PageRequest pageNumWithElements = PageRequest.of(
                    pageNumber,
                    ConstantValue.USERS_PER_PAGE,
                    Sort.by("reputation").descending());

            AppUserPagination userPagination = new AppUserPagination();
            userPagination.setAppUsersByPageIndex(appUserRepository.findAll(pageNumWithElements).getContent());
            userPagination.setNumberOfPages(Integer.parseInt("" + viewNumberOfPages()));
            userPagination.setNumberOfContents(Integer.parseInt("" + appUserRepository.count()));
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
                oldSocialUserInformation.setFirstName(newSocialUserInformation.getFirstName());
            }

            if (!StringUtils.isNullOrEmpty(newSocialUserInformation.getLastName())) {
                oldSocialUserInformation.setLastName(newSocialUserInformation.getLastName());
            }

            if (!StringUtils.isNullOrEmpty(newSocialUserInformation.getPhotoUrl())) {
                oldSocialUserInformation.setPhotoUrl(newSocialUserInformation.getPhotoUrl());
            }

            if (!StringUtils.isNullOrEmpty(newSocialUserInformation.getName())) {
                oldSocialUserInformation.setName(newSocialUserInformation.getName());
            }

            appUser.setSocialUser(oldSocialUserInformation);
            appUser.setCvUrl(updatedAppUser.getCvUrl());
            return appUserRepository.save(appUser);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/getTop5TagsOfUser/{type}/{userId}")
    @Transactional
    public TagPagination getTop5TagsOfUser(@PathVariable String type, @PathVariable Long userId) {
        try {
            List<AppUserTag> appUserTags;
            if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
                appUserTags = appUserTagRepository.findTop5ByAppUser_UserIdOrderByViewCountDesc(userId);
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
                appUserTags = appUserTagRepository.findTop5ByAppUser_UserIdOrderByReputationDesc(userId);
            } else {
                throw new Exception("Unknown type to getTop5TagsOfUser: " + type);
            }

            List<Tag> tags = new ArrayList<>();
            for (AppUserTag appUserTag : appUserTags) {
                tags.add(appUserTag.getTag());
            }

            TagPagination tagPagination = new TagPagination();
            tagPagination.setTagsByPageIndex(tags);
            tagPagination.setNumberOfPages(1);
            return tagPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/getAllTagsOfUser/{userId}/{pageNumber}")
    @Transactional
    public TagPagination getAllTagOfUser(@PathVariable Long userId, @PathVariable int pageNumber) {
        try {
            PageRequest pageNumWithElements = PageRequest.of(pageNumber, TAGS_PER_PAGE, Sort.by("viewCount").descending());
            Page<AppUserTag> appUserTags = appUserTagRepository.findByAppUser_UserId(userId, pageNumWithElements);

            List<Tag> tags = new ArrayList<>();
            for (AppUserTag appUserTag : appUserTags.getContent()) {
                tags.add(appUserTag.getTag());
            }

            int numberOfPages = 0;
            int resultTagsSize = getTotalTagsOfUser(userId);

            if (resultTagsSize % TAGS_PER_PAGE == 0) {
                numberOfPages = resultTagsSize / TAGS_PER_PAGE;
            } else {
                numberOfPages = resultTagsSize / TAGS_PER_PAGE + 1;
            }

            TagPagination tagPagination = new TagPagination();
            tagPagination.setTagsByPageIndex(tags);
            tagPagination.setNumberOfPages(numberOfPages);
            tagPagination.setNumberOfContents(resultTagsSize);
            return tagPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/getTop5QuestionsOfUser/{type}/{userId}")
    @Transactional
    public QuestionPagination getTop5QuestionsOfUser(@PathVariable String type, @PathVariable Long userId) {
        try {
            List<Question> questions;
            if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
                questions = questionRepository.findTop5ByAppUser_UserIdOrderByViewCountDesc(userId);
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
                questions = questionRepository.findTop5ByAppUser_UserIdOrderByUpvoteCountDesc(userId);
            } else if (SORT_DATE.equalsIgnoreCase(type)) {
                questions = questionRepository.findTop5ByAppUser_UserIdOrderByUtilTimestampDesc(userId);
            } else {
                throw new Exception("Unknown type to find top 5 questions: " + type);
            }
            QuestionPagination questionPagination = new QuestionPagination();
            questionPagination.setQa(questions);
            questionPagination.setNumberOfPages(1);
            return questionPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/getAllQuestionsOfUser/{type}/{userId}/{pageNumber}")
    @Transactional
    public QuestionPagination getAllQuestionsOfUser(@PathVariable String type, @PathVariable Long userId, @PathVariable int pageNumber) {
        try {
            PageRequest pageNumWithElements;
            if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, QUESTIONS_PER_PAGE, Sort.by("viewCount").descending());
            } else if (SORT_DATE.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, QUESTIONS_PER_PAGE, Sort.by("utilTimestamp").descending());
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, QUESTIONS_PER_PAGE, Sort.by("upvoteCount").descending());
            } else {
                throw new Exception("Unknown type to sort questions: " + type);
            }
            Page<Question> questionPage = questionRepository.findByAppUser_UserId(userId, pageNumWithElements);

            int numberOfPages = 0;
            Integer numberOfQuestionsByUserId = questionRepository.countNumberOfQuestionsByUserId(userId);

            if (numberOfQuestionsByUserId == null) {
                numberOfQuestionsByUserId = 0;
            }

            if (numberOfQuestionsByUserId % QUESTIONS_PER_PAGE == 0) {
                numberOfPages = numberOfQuestionsByUserId / QUESTIONS_PER_PAGE;
            } else {
                numberOfPages = numberOfQuestionsByUserId / QUESTIONS_PER_PAGE + 1;
            }

            QuestionPagination questionPagination = new QuestionPagination();
            questionPagination.setQa(questionPage.getContent());
            questionPagination.setNumberOfPages(numberOfPages);
            questionPagination.setNumberOfContents(numberOfQuestionsByUserId);
            return questionPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/getTotalTagsOfUser/{userId}")
    @Transactional
    public int getTotalTagsOfUser(@PathVariable Long userId) {
        try {
            AppUser appUser = appUserRepository.findById(userId)
                    .orElseThrow(() -> new Exception("Cannot find any users with id: " + userId));
            return appUserTagRepository.getTotalTagCount(appUser);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/getUserByIpAddress")
    @Transactional
    public AppUser getUserByIpAddress(HttpServletRequest request) {
        try {
            return controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/linkingAppUserToTags")
    @Transactional
    public String linkingAppUserToTags() {
        try {

            // First select all appUsers
            List<AppUser> appUsers = appUserRepository.findAll();

            for (AppUser appUser : appUsers) {

                // Select all list questions of that users
                List<Question> questionsByUserId = questionRepository.findByAppUser_UserId(appUser.getUserId());

                List<Tag> listDistinctTags = new ArrayList<>();
                for (Question question : questionsByUserId) {
                    List<Tag> tags = question.getTags();

                    for (Tag tag : tags) {
                        if (!listDistinctTags.contains(tag)) {
                            listDistinctTags.add(tag);
                        }
                    }
                }

                // Now merge it into AppUserTag
                for (Tag tag : listDistinctTags) {
                    List<AppUserTag> appUserTags = appUserTagRepository.findAllAppUserTagByAppUser_UserIdAndTag_TagId(appUser.getUserId(), tag.getTagId());
                    if (appUserTags == null) {
                        AppUserTag appUserTag = new AppUserTag();
                        appUserTag.setAppUser(appUser);
                        appUserTag.setTag(tag);
                        appUserTagRepository.save(appUserTag);
                    } else {
                        // remove to duplicate value
                        if (appUserTags.size() > 1) {
                            for (int i = 1; i < appUserTags.size(); i++) {
                                appUserTagRepository.delete(appUserTags.get(i));
                            }
                        }
                    }
                }

            }

            return "Please wait about 10 seconds for this method. Don't rush";
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
