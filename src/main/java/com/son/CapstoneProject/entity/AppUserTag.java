package com.son.CapstoneProject.entity;

import com.son.CapstoneProject.entity.login.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AppUserTag implements Serializable {

    @Id
    @GeneratedValue
    private Long appUserTagId;

    @ManyToOne
    @JoinColumn
    private AppUser appUser;

    @ManyToOne
    @JoinColumn
    private Tag tag;

    private int reputation;

    private int viewCount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppUserTag that = (AppUserTag) o;
        return Objects.equals(appUserTagId, that.appUserTagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appUserTagId);
    }
}
