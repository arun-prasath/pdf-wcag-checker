package com.hiccup.model;

import java.util.Objects;

public class AccessibilityIssue {
    private String wcagCriterion;
    private String level;
    private String description;
    private String status;

    public AccessibilityIssue(String wcagCriterion, String level,
                              String description, String status) {
        this.wcagCriterion = wcagCriterion;
        this.level = level;
        this.description = description;
        this.status = status;
    }

	public String getWcagCriterion() {
		return wcagCriterion;
	}

	public void setWcagCriterion(String wcagCriterion) {
		this.wcagCriterion = wcagCriterion;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(wcagCriterion);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccessibilityIssue other = (AccessibilityIssue) obj;
		return Objects.equals(wcagCriterion, other.wcagCriterion);
	}

}
