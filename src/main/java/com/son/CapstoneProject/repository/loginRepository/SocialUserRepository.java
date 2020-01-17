package com.son.CapstoneProject.repository.loginRepository;

import com.son.CapstoneProject.common.entity.login.SocialUser;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialUserRepository extends PagingAndSortingRepository<SocialUser, Long> {

    // Id is social Id
    SocialUser findById(String id);

}
