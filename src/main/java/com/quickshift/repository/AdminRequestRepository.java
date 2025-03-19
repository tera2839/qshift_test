package com.quickshift.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quickshift.entity.AdminRequest;
import com.quickshift.entity.Store;

@Repository
public interface AdminRequestRepository extends JpaRepository<AdminRequest, Long>{

	public List<AdminRequest> findByStore(Store store);
}