package org.timeflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.dao.CourseDAO;
import org.timeflow.dao.DepartmentDAO;
import org.timeflow.dao.UserDAO;
import org.timeflow.entity.Course;
import org.timeflow.entity.Department;
import org.timeflow.entity.User;
import org.timeflow.entity.UserRole;

public class DataSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private final UserDAO userDAO = new UserDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    public void seedInitialData() {
        // Check if data already exists to prevent running this more than once.
        if (departmentDAO.findAll().isEmpty()) {
            logger.info("Database is empty. Seeding initial data...");
            try {
                // Step 1: Create Departments
                Department csDept = new Department("Computer Science", "CSC", "Dr. Ada Lovelace");
                Department eeDept = new Department("Electrical Engineering", "EEE", "Dr. Nikola Tesla");
                departmentDAO.save(csDept);
                departmentDAO.save(eeDept);
                logger.info("Created initial departments.");

                // Step 2: Create Users
                // Admin (does not need a department)
                User admin = new User("admin", "admin@timeflow.com", "admin123", UserRole.ADMIN, null);
                userDAO.save(admin);

                // Exams Officer for Computer Science
                User examsOfficer = new User("eo_csc", "eo.csc@timeflow.com", "officer123", UserRole.EXAMS_OFFICER, csDept);
                userDAO.save(examsOfficer);

                // Lecturer for Computer Science
                User lecturer = new User("jdoe", "j.doe@timeflow.com", "lecturer123", UserRole.LECTURER, csDept);
                userDAO.save(lecturer);

                // Student in Computer Science
                User student = new User("asmith", "a.smith@timeflow.com", "student123", UserRole.STUDENT, csDept);
                userDAO.save(student);
                logger.info("Created initial users.");

                // Step 3: Create Courses for Computer Science, assigned to the lecturer
                Course csc101 = new Course("Introduction to Programming", "CSC101", 3, csDept, lecturer, 100);
                Course csc201 = new Course("Data Structures", "CSC201", 4, csDept, lecturer, 200);
                courseDAO.save(csc101);
                courseDAO.save(csc201);
                logger.info("Created initial courses.");

                logger.info("Initial data seeding completed successfully.");

            } catch (Exception e) {
                logger.error("Failed to seed initial data", e);
            }
        } else {
            logger.info("Database already contains data. Skipping seeding.");
        }
    }
}