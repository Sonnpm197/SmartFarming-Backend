package com.son.CapstoneProject.repository.loginRepository;

import com.son.CapstoneProject.entity.login.AppRole;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.login.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Repository
public class AppRoleDAO {

    @Autowired
    private EntityManager entityManager;

    /**
     * An user can have many roles
     * @param userId
     * @return
     */
    public List<String> getRoleNamesByUserId(Long userId) {
        String sql = "Select ur.appRole.roleName from " + UserRole.class.getName() + " ur "
                + " where ur.appUser.userId = :userId ";

        Query query = this.entityManager.createQuery(sql, String.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    /**
     * Get the roles inside the app by roleName
     * @param roleName
     * @return
     */
    public AppRole findAppRoleByName(String roleName) {
        try {
            String sql = "Select e from " + AppRole.class.getName() + " e " //
                    + " where e.roleName = :roleName ";

            Query query = this.entityManager.createQuery(sql, AppRole.class);
            query.setParameter("roleName", roleName);
            return (AppRole) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Assign more roles to an user
     * @param appUser
     * @param roleNames
     */
    public void createRoleFor(AppUser appUser, List<String> roleNames) {
        for (String roleName : roleNames) {
            AppRole role = this.findAppRoleByName(roleName);
            if (role == null) {
                role = new AppRole();
                role.setRoleName(AppRole.ROLE_USER);
                this.entityManager.persist(role);
                this.entityManager.flush();
            }
            UserRole userRole = new UserRole();
            userRole.setAppRole(role);
            userRole.setAppUser(appUser);
            this.entityManager.persist(userRole);
            this.entityManager.flush();
        }
    }

}