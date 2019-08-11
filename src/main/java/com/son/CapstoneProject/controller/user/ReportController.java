package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.CommentRepository;
import com.son.CapstoneProject.repository.NotificationRepository;
import com.son.CapstoneProject.repository.ReportRepository;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/report")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
    }

    @GetMapping("/findListUsersAndReportTime")
    public List<UserAndReportTime> findListUsersAndReportTime() {
        try {
            String methodName = "ReportController.findListUsersAndReportTime";
            List<Object[]> results =  reportRepository.findListUsersAndReportTime();

            List<UserAndReportTime> userAndReportTimes = new ArrayList<>();
            for (Object[] row: results) {
                UserAndReportTime userAndReportTime = new UserAndReportTime();
                userAndReportTime.setUserId(row[0] == null ? null : row[0].toString());
                userAndReportTime.setRole(row[1] == null ? null : row[1].toString());
                userAndReportTime.setFullName(row[2] == null ? null : row[2].toString());
                userAndReportTime.setNumberOfReports(row[3] == null ? null : row[3].toString());
                userAndReportTimes.add(userAndReportTime);
            }

            return userAndReportTimes;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/findListReportsByUser/{userId}")
    public List<Report> findListReportsByUser(@PathVariable Long userId) {
        try {
            String methodName = "ReportController.findListReportsByUser";

            AppUser appUser = appUserRepository.findById(userId)
                    .orElseThrow(() -> new Exception(methodName + ": cannot find user with id: " + userId));

            return reportRepository.findByAppUser_UserId(appUser.getUserId());
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

}
