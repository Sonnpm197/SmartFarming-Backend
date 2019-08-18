package com.son.CapstoneProject.entity;

import com.son.CapstoneProject.entity.login.AppUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RelatedAppUserWithDetails {

    private AppUser appUser;
    private List<AppUserTag> appUserTags;

}
