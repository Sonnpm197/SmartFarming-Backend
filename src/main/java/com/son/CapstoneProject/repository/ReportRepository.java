package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends PagingAndSortingRepository<Report, Long> {

    Page<Report> findAll(Pageable pageable);

}
