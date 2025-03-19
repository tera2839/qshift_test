package com.quickshift.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quickshift.entity.Calendar;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long>{

}
