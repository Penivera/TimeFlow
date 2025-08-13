package org.timeflow.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.timeflow.entity.Course;
import org.timeflow.entity.Department;
import org.timeflow.entity.User;
import java.util.List;

public class CourseDAO extends BaseDAO<Course, Long> {

    public CourseDAO() {
        super(Course.class);
    }

    // Find course by code
    public Course findByCode(String code) {
        String hql = "FROM Course c WHERE c.code = ?0";
        return executeUniqueQuery(hql, code);
    }

    // Find courses by department
    public List<Course> findByDepartment(Department department) {
        String hql = "FROM Course c WHERE c.department = ?0 ORDER BY c.level, c.name";
        return executeQuery(hql, department);
    }

    // Find courses by lecturer
    public List<Course> findByLecturer(User lecturer) {
        String hql = "FROM Course c WHERE c.lecturer = ?0 ORDER BY c.name";
        return executeQuery(hql, lecturer);
    }

    // Find courses by department and level
    public List<Course> findByDepartmentAndLevel(Department department, int level) {
        String hql = "FROM Course c WHERE c.department = ?0 AND c.level = ?1 ORDER BY c.name";
        return executeQuery(hql, department, level);
    }

    // Find borrowed courses (courses from other departments)
    public List<Course> findBorrowedCourses(Department department) {
        try (Session session = sessionFactory.openSession()) {
            Query<Course> query = session.createQuery(
                    "SELECT DISTINCT c FROM Course c " +
                            "JOIN c.timetables t " +
                            "JOIN t.semester s " +
                            "WHERE c.department != :department " +
                            "AND s.isCurrent = true",
                    Course.class);
            query.setParameter("department", department);
            return query.list();
        }
    }

    // Get course statistics
    public List<Object[]> getCourseStats() {
        try (Session session = sessionFactory.openSession()) {
            Query<Object[]> query = session.createQuery(
                    "SELECT c.name, c.code, c.credits, d.name, " +
                            "u.username, COUNT(t.id) as timetableCount " +
                            "FROM Course c " +
                            "JOIN c.department d " +
                            "LEFT JOIN c.lecturer u " +
                            "LEFT JOIN c.timetables t " +
                            "GROUP BY c.id, c.name, c.code, c.credits, d.name, u.username " +
                            "ORDER BY d.name, c.level, c.name",
                    Object[].class);
            return query.list();
        }
    }
}
