package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.AppUserTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppUserTagRepository extends JpaRepository<AppUserTag, Long> {

    AppUserTag findAppUserTagByAppUser_UserIdAndTag_TagId(Long appUserId, Long tagId);

    List<AppUserTag> findByTag_TagId(Long tagId);
}
