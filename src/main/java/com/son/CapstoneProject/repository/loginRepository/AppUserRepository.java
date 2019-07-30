package com.son.CapstoneProject.repository.loginRepository;

import com.son.CapstoneProject.entity.login.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppUserRepository extends PagingAndSortingRepository<AppUser, Long> {

    AppUser findBySocialUser_SocialUserId(Long id);

    Page<AppUser> findAll(Pageable pageable);

    AppUser findByIpAddress(String ipAddress);

    List<AppUser> findTop3ByRoleOrderByReputationDesc(String role);

//    AppUser findByUserName(String userName);

}
