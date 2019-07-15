package com.son.CapstoneProject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.son.CapstoneProject.entity.login.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
/**
 * This class is used when users want to report inappropriate questions
 */
public class Report {

    @Id
    @GeneratedValue
    private Long reportId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "FK_REPORT_APPUSER"))
    private AppUser appUser;

    @Column(columnDefinition = "ntext")
    private String message;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionId", foreignKey = @ForeignKey(name = "FK_REPORT_QUESTION"))
    private Question question;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date utilTimestamp;
}
