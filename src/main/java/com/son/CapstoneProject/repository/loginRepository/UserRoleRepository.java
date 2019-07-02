package com.son.CapstoneProject.repository.loginRepository;

import com.son.CapstoneProject.entity.login.UserRole;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends PagingAndSortingRepository<UserRole, Long> {

    List<UserRole> findByAppUser_UserId(Long userId);

}
