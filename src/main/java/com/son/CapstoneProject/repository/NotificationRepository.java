package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends PagingAndSortingRepository<Notification, Long> {

    Page<Notification> findByAppUserReceiver_UserId(Long userId, Pageable pageable);

}
