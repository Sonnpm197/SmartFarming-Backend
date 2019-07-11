package com.son.CapstoneProject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class UploadedFile {

    @Id
    @GeneratedValue
    private Long id;

    private String uploadedFileUrlShownOnUI;

    private String uploadedFileName;

    private String bucketName;

    @JsonBackReference(value = "uploadedFile_article")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articleId", foreignKey = @ForeignKey(name = "FK_UPLOADEDFILE_ARTICLE"))
    private Article article;

    @JsonBackReference(value = "uploadedFile_question")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionId", foreignKey = @ForeignKey(name = "FK_UPLOADEDFILE_QUESTION"))
    private Question question;
}
