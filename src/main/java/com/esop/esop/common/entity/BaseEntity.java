/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.common.entity;

import java.io.Serializable;
import java.time.ZonedDateTime;

import lombok.Getter;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import com.esop.esop.common.util.DateTimeUtil;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {
	@Column(name = "created_at", nullable = false, updatable = false)
	protected ZonedDateTime createdAt;
	
	@Column(name = "updated_at", nullable = false)
	protected ZonedDateTime updatedAt;
	
	
	@PrePersist
	private void onCreate() {
		final ZonedDateTime now = DateTimeUtil.now();
		if (this.createdAt == null) {
			this.createdAt = now;
		}
		if (this.updatedAt == null) {
			this.updatedAt = now;
		}
	}
	
	@PreUpdate
	private void onUpdate() {
		this.updatedAt = DateTimeUtil.now();
	}
}
