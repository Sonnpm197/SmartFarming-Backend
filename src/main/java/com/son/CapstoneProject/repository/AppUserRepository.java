package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.login.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    AppUser findByUserId(Long userId);

    AppUser findByIpAddress(String ipAddress);

    AppUser findByUserName(String userName);

}
