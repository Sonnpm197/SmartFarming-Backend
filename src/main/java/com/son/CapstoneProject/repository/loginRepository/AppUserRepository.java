package com.son.CapstoneProject.repository.loginRepository;

import com.son.CapstoneProject.entity.login.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    AppUser findByIpAddress(String ipAddress);

    AppUser findByUserName(String userName);

}
