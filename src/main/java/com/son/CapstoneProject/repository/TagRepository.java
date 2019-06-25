package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Tag;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends PagingAndSortingRepository<Tag, Long> {

    List<Tag> findByName(String name);

}
