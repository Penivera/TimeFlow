package org.timeflow.entity;

public enum SemesterType {
    FIRST_SEMESTER("First Semester"),
    SECOND_SEMESTER("Second Semester");

    private final String displayName;

    SemesterType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() { return displayName; }
}