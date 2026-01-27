package com.hiccup;

public class AccessibilityIssue {
    public String wcagCriterion;
    public String level;
    public String description;
    public String status;

    public AccessibilityIssue(String wcagCriterion, String level,
                              String description, String status) {
        this.wcagCriterion = wcagCriterion;
        this.level = level;
        this.description = description;
        this.status = status;
    }
}
