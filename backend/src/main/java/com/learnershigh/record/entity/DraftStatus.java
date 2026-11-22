package com.learnershigh.record.entity;

// 세특 초안 상태. 학생은 DRAFT, REVISION_REQUESTED일 때만 고칠 수 있다
public enum DraftStatus {
	DRAFT,
	SUBMITTED,
	REVISION_REQUESTED,
	CONFIRMED;

	public boolean isEditableByStudent() {
		return this == DRAFT || this == REVISION_REQUESTED;
	}
}
