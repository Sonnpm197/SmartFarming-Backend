package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.pagination.ReportPagination;
import com.son.CapstoneProject.entity.pagination.UserAndReportTimePagination;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.CommentRepository;
import com.son.CapstoneProject.repository.NotificationRepository;
import com.son.CapstoneProject.repository.ReportRepository;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.son.CapstoneProject.common.ConstantValue.QUESTIONS_PER_PAGE;
import static com.son.CapstoneProject.common.ConstantValue.REPORTS_PER_PAGE;

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

    @GetMapping("/findListUsersAndReportTime/{pageNumber}")
    @Transactional
    public UserAndReportTimePagination findListUsersAndReportTime(@PathVariable int pageNumber) {
        try {
            String methodName = "ReportController.findListUsersAndReportTime";

            int startRow = pageNumber * REPORTS_PER_PAGE + 1; // from >= 1
            int endRow = startRow + REPORTS_PER_PAGE - 1; // to <= 10

            List<Object[]> results = reportRepository.findListUsersAndReportTime(startRow, endRow);

            List<UserAndReportTime> userAndReportTimes = new ArrayList<>();
            for (Object[] row : results) {
                UserAndReportTime userAndReportTime = new UserAndReportTime();
                userAndReportTime.setRowIndex(row[0] == null ? null : row[0].toString());
                userAndReportTime.setUserId(row[1] == null ? null : row[1].toString());
                userAndReportTime.setRole(row[2] == null ? null : row[2].toString());
                userAndReportTime.setFullName(row[3] == null ? null : row[3].toString());
                userAndReportTime.setNumberOfReports(row[4] == null ? null : row[4].toString());
                userAndReportTimes.add(userAndReportTime);
            }

            UserAndReportTimePagination userAndReportTimePagination = new UserAndReportTimePagination();
            userAndReportTimePagination.setUserAndReportTimes(userAndReportTimes);

            int numberOfReportedUsers = reportRepository.findTotalReportedUsers();
            if (numberOfReportedUsers % REPORTS_PER_PAGE == 0) {
                userAndReportTimePagination.setNumberOfPages(numberOfReportedUsers / REPORTS_PER_PAGE);
            } else {
                userAndReportTimePagination.setNumberOfPages(numberOfReportedUsers / REPORTS_PER_PAGE + 1);
            }

            userAndReportTimePagination.setNumberOfContents(numberOfReportedUsers);
            return userAndReportTimePagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/findListReportsByUser/{userId}/{pageNumber}")
    @Transactional
    public ReportPagination findListReportsByUser(@PathVariable Long userId, @PathVariable int pageNumber) {
        try {
            String methodName = "ReportController.findListReportsByUser";

            AppUser appUser = appUserRepository.findById(userId)
                    .orElseThrow(() -> new Exception(methodName + ": cannot find user with id: " + userId));

            PageRequest pageNumWithElements = PageRequest.of(pageNumber, REPORTS_PER_PAGE, Sort.by("utilTimestamp").descending());

            ReportPagination reportPagination = new ReportPagination();
            reportPagination.setReportsByPageIndex(reportRepository.findByAppUser_UserId(appUser.getUserId(), pageNumWithElements).getContent());

            Integer totalReportsByUsers = reportRepository.findTotalReportsByUser(appUser.getUserId());

            if (totalReportsByUsers == null) {
                reportPagination.setNumberOfPages(0);
                reportPagination.setNumberOfContents(0);
            } else {
                if (totalReportsByUsers % REPORTS_PER_PAGE == 0) {
                    reportPagination.setNumberOfPages(totalReportsByUsers / REPORTS_PER_PAGE);
                } else {
                    reportPagination.setNumberOfPages(totalReportsByUsers / REPORTS_PER_PAGE + 1);
                }

                reportPagination.setNumberOfContents(totalReportsByUsers);
            }

            return reportPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

}
