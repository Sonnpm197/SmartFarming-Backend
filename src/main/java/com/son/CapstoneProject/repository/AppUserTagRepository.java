package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.AppUserTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppUserTagRepository extends PagingAndSortingRepository<AppUserTag, Long> {

    AppUserTag findAppUserTagByAppUser_UserIdAndTag_TagId(Long appUserId, Long tagId);

    List<AppUserTag> findByTag_TagId(Long tagId);

    List<AppUserTag> findTop5ByAppUser_UserIdOrderByViewCountDesc(Long userId);

    Page<AppUserTag> findByAppUser_UserId(Long userId, Pageable pageable);
}
