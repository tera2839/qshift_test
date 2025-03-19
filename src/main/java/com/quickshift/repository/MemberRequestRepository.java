package com.quickshift.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quickshift.entity.MemberRequest;

@Repository
public interface MemberRequestRepository extends JpaRepository<MemberRequest, Long>{
	
}