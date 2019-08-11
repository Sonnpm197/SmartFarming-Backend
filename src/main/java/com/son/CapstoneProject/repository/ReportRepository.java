package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Report;
import com.son.CapstoneProject.entity.UserAndReportTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportRepository extends PagingAndSortingRepository<Report, Long> {

    Page<Report> findAll(Pageable pageable);

    @Query(
            value = "select au.user_id as userId, au.role as role, su.name as fullName, count(r.report_id) as 'numberOfReports'\n" +
                    "from app_user au\n" +
                    "       left join social_user su on au.social_id = su.social_user_id\n" +
                    "       join report r on au.user_id = r.user_id\n" +
                    "group by au.user_id, au.role, su.name;",
            nativeQuery=true
    )
    List<Object[]> findListUsersAndReportTime();

    List<Report> findByAppUser_UserId(Long userId);

}
