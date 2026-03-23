package com.streaming.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.streaming.demo.entity.UploadJob;

@Repository
public interface UploadJobRepository extends JpaRepository<UploadJob, Long> {

	List<UploadJob> findByStatusIn(List<String> statuses);

}