package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.domain.FarmingSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FarmingSkillRepository extends JpaRepository<FarmingSkill, Long> {
}
