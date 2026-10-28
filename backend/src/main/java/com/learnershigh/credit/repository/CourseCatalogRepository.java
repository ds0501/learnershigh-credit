package com.learnershigh.credit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.credit.entity.CourseCatalog;

public interface CourseCatalogRepository extends JpaRepository<CourseCatalog, Long> {

	List<CourseCatalog> findAllByOrderByIdAsc();

}
