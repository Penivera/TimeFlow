package org.timeflow.entity;

// UserRole.java

public enum UserRole {
    ADMIN("Admin"),
    EXAMS_OFFICER("Exams Officer"),
    LECTURER("Lecturer"),
    STUDENT("Student");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName; // what JComboBox will display
    }
}
