package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Notification;
import com.son.CapstoneProject.entity.login.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends PagingAndSortingRepository<Notification, Long> {

    @Query(
            value = "select count(distinct n.notification_id) from notification n\n" +
                    "where n.user_id = :userId",
            nativeQuery = true
    )
    Integer findTotalNumberByAppUserReceiver_UserId(@Param("userId") Long userId);

    Page<Notification> findByAppUserReceiver_UserId(Long userId, Pageable pageable);

    @Query("select count(n.notificationId) from Notification n where n.seen = :seen and n.appUserReceiver = :appUser")
    Integer getTotalUnseenNotificationByUser(@Param("seen") boolean seen, @Param("appUser") AppUser appUser);
}
