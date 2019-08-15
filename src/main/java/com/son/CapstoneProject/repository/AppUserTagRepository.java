package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.AppUserTag;
import com.son.CapstoneProject.entity.login.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface AppUserTagRepository extends PagingAndSortingRepository<AppUserTag, Long> {

    AppUserTag findAppUserTagByAppUser_UserIdAndTag_TagId(Long appUserId, Long tagId);

    List<AppUserTag> findAllAppUserTagByAppUser_UserIdAndTag_TagId(Long appUserId, Long tagId);

    List<AppUserTag> findByTag_TagId(Long tagId);

    List<AppUserTag> findTop5ByAppUser_UserIdOrderByViewCountDesc(Long userId);

    List<AppUserTag> findTop5ByAppUser_UserIdOrderByReputationDesc(Long userId);

    Page<AppUserTag> findByAppUser_UserId(Long userId, Pageable pageable);

    @Query("select count(a.appUserTagId) from AppUserTag a where a.appUser = :appUser")
    Integer getTotalTagCount(@Param("appUser") AppUser appUser);

    @Query(
            value = "select distinct au.user_id\n" +
                    "from app_user au join app_user_tag aut on au.user_id = aut.app_user_user_id\n" +
                    "where aut.tag_tag_id in :tagIds and au.user_id != :userId",
            nativeQuery = true
    )
    List<BigInteger> findDistinctUsersByTagIdsInAndUserIdIsNot(@Param("tagIds") List<Long> tagIds, @Param("userId") Long userId);

    @Query(
            value = "select sum(aut.view_count)\n" +
                    "from app_user_tag aut\n" +
                    "where aut.app_user_user_id = :userId and aut.tag_tag_id in :tagIds",
            nativeQuery = true
    )
    Integer findTotalViewCountOfUserIdByTagIdsIn(@Param("userId") Long userId, @Param("tagIds") List<Long> tagIds);

    List<AppUserTag> findByAppUser_UserIdAndTag_TagIdIn(Long userId, List<Long> tagIds);
}
