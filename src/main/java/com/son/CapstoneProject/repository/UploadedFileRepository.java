package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findByQuestion_QuestionId(Long questionId);

    UploadedFile findByUploadedFileName(String fileName);

    UploadedFile findByBucketNameAndUploadedFileName(String bucketName, String uploadedFileName);

}
