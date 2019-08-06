package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends PagingAndSortingRepository<Tag, Long> {

    Tag findByName(String name);

    Page<Tag> findAll(Pageable pageable);

    Page<Tag> findAllByOrderByViewCountDesc(Pageable pageable);

    Page<Tag> findAllByOrderByReputationDesc(Pageable pageable);

    List<Tag> findTop5ByOrderByViewCountDesc();

    List<Tag> findTop10ByOrderByViewCountDesc();

    List<Tag> findTop10ByOrderByReputationDesc();
}
