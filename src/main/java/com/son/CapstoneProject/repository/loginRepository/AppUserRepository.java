package com.son.CapstoneProject.repository.loginRepository;

import com.son.CapstoneProject.entity.login.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AppUserRepository extends PagingAndSortingRepository<AppUser, Long> {

    AppUser findBySocialUser_SocialUserId(Long id);

    Page<AppUser> findAll(Pageable pageable);

    AppUser findByIpAddress(String ipAddress);

    List<AppUser> findTop3ByRoleOrderByReputationDesc(String role);

    @Query("select count(q.userId) from AppUser q where q.createdTimeByUtilTimeStamp >= :startDateTime and q.createdTimeByUtilTimeStamp <= :endDateTime")
    Integer findTotalNewAccountsByUtilTimestampBetween(@Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

    @Query("select count(q.userId) from AppUser q where q.lastActiveByUtilTimeStamp <= :endDateTime")
    Integer findTotalInactiveAccountsByUtilTimestampBefore(@Param("endDateTime") Date endDateTime);
}
