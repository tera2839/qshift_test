package com.quickshift.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "memberRequest")
public class MemberRequest {
	
	@Id
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "calendar_id", referencedColumnName = "id")
	private Calendar calendar;
	
	@ManyToOne
	@JoinColumn(name = "member_id", referencedColumnName = "id")
	private Member member;
	
	@ManyToOne
	@JoinColumn(name = "timeplan_id", referencedColumnName = "id")
	private Timeplan timeplan;
}
