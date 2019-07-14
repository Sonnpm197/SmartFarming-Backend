package com.son.CapstoneProject.repository.loginRepository;

import com.son.CapstoneProject.entity.login.SocialUserInformation;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialUserInformationRepository extends PagingAndSortingRepository<SocialUserInformation, Long> {

    // Id is social Id
    SocialUserInformation findById(String id);

}
