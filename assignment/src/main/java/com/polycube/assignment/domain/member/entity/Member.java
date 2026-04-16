package com.polycube.assignment.domain.member.entity;

import com.polycube.assignment.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access =AccessLevel.PROTECTED)
public class Member extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MemberGrade grade; // NORMAL, VIP, VVIP
}
