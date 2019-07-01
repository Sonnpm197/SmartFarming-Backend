package com.son.CapstoneProject.repository.loginRepository;

import com.son.CapstoneProject.entity.login.UserRole;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends PagingAndSortingRepository<UserRole, Long> {

    UserRole findByAppUser_UserId(Long userId);

}
