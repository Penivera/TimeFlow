package org.timeflow.dao;

public class DAOFactory {
    private static DAOFactory instance;
    private UserDAO userDAO;
    private DepartmentDAO departmentDAO;
    private CourseDAO courseDAO;
    private TimetableDAO timetableDAO;
    private ConflictDAO conflictDAO;

    private DAOFactory() {
        // Initialize all DAOs
        userDAO = new UserDAO();
        departmentDAO = new DepartmentDAO();
        courseDAO = new CourseDAO();
        timetableDAO = new TimetableDAO();
        conflictDAO = new ConflictDAO();
    }

    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    // Getters for all DAOs
    public UserDAO getUserDAO() { return userDAO; }
    public DepartmentDAO getDepartmentDAO() { return departmentDAO; }
    public CourseDAO getCourseDAO() { return courseDAO; }
    public TimetableDAO getTimetableDAO() { return timetableDAO; }
    public ConflictDAO getConflictDAO() { return conflictDAO; }
}
