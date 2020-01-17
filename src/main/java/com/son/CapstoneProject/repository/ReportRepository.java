package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.common.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends PagingAndSortingRepository<Report, Long> {

    Page<Report> findAll(Pageable pageable);

    @Query(
            value = "select *\n" +
                    "from (select row_number() over(order by au.user_id) as rowIndex,\n" +
                    "             au.user_id,\n" +
                    "             au.role,\n" +
                    "             su.name,\n" +
                    "             count(r.report_id) as 'number_of_reports'\n" +
                    "      from app_user au\n" +
                    "             left join social_user su on au.social_id = su.social_user_id\n" +
                    "             join report r on au.user_id = r.user_id\n" +
                    "      group by au.user_id, au.role, su.name) as sub\n"
                    /*+ "where sub.rowIndex >= :startRow and sub.rowIndex <= :endRow"*/,
            nativeQuery = true
    )
    List<Object[]> findListUsersAndReportTime(/*@Param("startRow") int startRow, @Param("endRow") int endRow*/);

    @Query(
            value = "select count(distinct r.user_id) from report r;",
            nativeQuery = true
    )
    Integer findTotalReportedUsers();

    Page<Report> findByAppUser_UserId(Long userId, Pageable pageable);

    @Query(
            value = "select count(r.report_id) from report r where r.user_id = :userId",
            nativeQuery = true
    )
    Integer findTotalReportsByUser(@Param("userId") Long userId);

    List<Report> findByQuestion_QuestionIdAndMessageAndAppUser_UserId(Long questionId, String message, Long userId);

}
