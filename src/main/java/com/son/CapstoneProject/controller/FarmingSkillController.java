package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.domain.FarmingSkill;
import com.son.CapstoneProject.repository.FarmingSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/farmingSkills")
public class FarmingSkillController {

    @Autowired
    private FarmingSkillRepository farmingSkillRepository;

    @GetMapping("/viewAll")
    public List<FarmingSkill> viewAllFarmingSkills() {
        return farmingSkillRepository.findAll();
    }
}
