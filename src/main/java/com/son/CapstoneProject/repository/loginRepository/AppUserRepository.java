package com.son.CapstoneProject.repository.loginRepository;

import com.son.CapstoneProject.entity.login.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends PagingAndSortingRepository<AppUser, Long> {

    Page<AppUser> findAll(Pageable pageable);

    AppUser findByIpAddress(String ipAddress);

//    AppUser findByUserName(String userName);

}
