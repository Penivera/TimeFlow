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
import org.timeflow.entity.Room;
import org.timeflow.dao.RoomDAO;

public class DataSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private final UserDAO userDAO = new UserDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    public void seedInitialData() {
        if (departmentDAO.findAll().isEmpty()) {
            logger.info("Database is empty. Seeding initial data...");
            try {
                logger.info("Creating initial rooms...");
                roomDAO.save(new Room("1K Capacity", 1000, "CSC LT"));
                roomDAO.save(new Room("600 Capacity", 150, "ICT I"));
                roomDAO.save(new Room("Lab 1", 30, "CSC Lab 1"));
                roomDAO.save(new Room("Lab 2", 30, "Defense Lab 2"));
                // Step 1: Create Departments
                Department csDept = new Department("Computer Science", "CSC", "Dr. Ada Lovelace");
                Department eeDept = new Department("Electrical Engineering", "EEE", "Dr. Nikola Tesla");
                departmentDAO.save(csDept);
                departmentDAO.save(eeDept);
                logger.info("Created initial departments.");

                // Step 2: Create Users
                User admin = new User("admin", "admin@timeflow.com", "admin123", UserRole.ADMIN, null);
                userDAO.save(admin);

                User examsOfficer = new User("eo_csc", "eo.csc@timeflow.com", "officer123", UserRole.EXAMS_OFFICER, csDept);
                userDAO.save(examsOfficer);

                User lecturer = new User("jdoe", "j.doe@timeflow.com", "lecturer123", UserRole.LECTURER, csDept);
                userDAO.save(lecturer);

                // --- MODIFIED: Set the level for the student user ---
                User student = new User("asmith", "a.smith@timeflow.com", "student123", UserRole.STUDENT, csDept);
                student.setLevel(100); // Set student's level to match CSC101
                userDAO.save(student);
                logger.info("Created initial users.");

                // Step 3: Create Courses
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