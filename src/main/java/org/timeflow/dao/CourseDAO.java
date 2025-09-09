package org.timeflow.dao;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.entity.Course;
import org.timeflow.entity.Department;
import org.timeflow.entity.User;
import jakarta.persistence.criteria.*;
import jakarta.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;

public class CourseDAO extends BaseDAO<Course, Long> {
    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);

    public CourseDAO() {
        super(Course.class);
    }

    // Find course by code using Criteria API
    public Course findByCode(String code) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Course> query = cb.createQuery(Course.class);
            Root<Course> root = query.from(Course.class);

            query.select(root)
                    .where(cb.equal(root.get("code"), code));

            TypedQuery<Course> typedQuery = session.createQuery(query);
            List<Course> results = typedQuery.getResultList();

            return results.isEmpty() ? null : results.get(0);
        }
    }

    // Find courses by department using Criteria API
    public List<Course> findByDepartment(Department department) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Course> query = cb.createQuery(Course.class);
            Root<Course> root = query.from(Course.class);

            query.select(root)
                    .where(cb.equal(root.get("department"), department))
                    .orderBy(cb.asc(root.get("level")), cb.asc(root.get("name")));

            return session.createQuery(query).getResultList();
        }
    }

    // Find courses by lecturer using Criteria API
    public List<Course> findByLecturer(User lecturer) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Course> query = cb.createQuery(Course.class);
            Root<Course> root = query.from(Course.class);

            query.select(root)
                    .where(cb.equal(root.get("lecturer"), lecturer))
                    .orderBy(cb.asc(root.get("name")));

            return session.createQuery(query).getResultList();
        }
    }

    // Find courses by department and level using Criteria API
    public List<Course> findByDepartmentAndLevel(Department department, int level) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Course> query = cb.createQuery(Course.class);
            Root<Course> root = query.from(Course.class);

            Predicate departmentMatch = cb.equal(root.get("department"), department);
            Predicate levelMatch = cb.equal(root.get("level"), level);

            query.select(root)
                    .where(cb.and(departmentMatch, levelMatch))
                    .orderBy(cb.asc(root.get("name")));

            return session.createQuery(query).getResultList();
        }
    }

    // Find borrowed courses using Criteria API with joins
    public List<Course> findBorrowedCourses(Department department) {
        // The logic for borrowed courses was dependent on a "current" semester with start/end dates.
        // Since we simplified this to an enum, this feature is more complex to implement.
        // For now, we return an empty list to prevent crashes and keep the app simple.
        // This can be revisited in a future version.
        logger.warn("findBorrowedCourses is not fully implemented for SemesterType enum and will return an empty list.");
        return Collections.emptyList();
    }

    // Get course statistics using Criteria API with multiselect and joins
    public List<CourseStatistic> getCourseStats() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<CourseStatistic> query = cb.createQuery(CourseStatistic.class);
            Root<Course> courseRoot = query.from(Course.class);

            Join<Course, Department> departmentJoin = courseRoot.join("department");
            Join<Course, User> lecturerJoin = courseRoot.join("lecturer", JoinType.LEFT);
            Join<Course, Object> timetableJoin = courseRoot.join("timetables", JoinType.LEFT);

            query.multiselect(
                            courseRoot.get("name"),
                            courseRoot.get("code"),
                            courseRoot.get("credits"),
                            departmentJoin.get("name"),
                            lecturerJoin.get("username"),
                            cb.count(timetableJoin.get("id"))
                    )
                    .groupBy(
                            courseRoot.get("id"),
                            courseRoot.get("name"),
                            courseRoot.get("code"),
                            courseRoot.get("credits"),
                            departmentJoin.get("name"),
                            lecturerJoin.get("username")
                    )
                    .orderBy(
                            cb.asc(departmentJoin.get("name")),
                            cb.asc(courseRoot.get("level")),
                            cb.asc(courseRoot.get("name"))
                    );

            return session.createQuery(query).getResultList();
        }
    }

    // Helper class for course statistics result
    public static class CourseStatistic {
        private String courseName;
        private String courseCode;
        private Integer credits;
        private String departmentName;
        private String lecturerUsername;
        private Long timetableCount;

        public CourseStatistic(String courseName, String courseCode, Integer credits,
                               String departmentName, String lecturerUsername, Long timetableCount) {
            this.courseName = courseName;
            this.courseCode = courseCode;
            this.credits = credits;
            this.departmentName = departmentName;
            this.lecturerUsername = lecturerUsername;
            this.timetableCount = timetableCount;
        }

        // Getters and Setters
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
        public Integer getCredits() { return credits; }
        public void setCredits(Integer credits) { this.credits = credits; }
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
        public String getLecturerUsername() { return lecturerUsername; }
        public void setLecturerUsername(String lecturerUsername) { this.lecturerUsername = lecturerUsername; }
        public Long getTimetableCount() { return timetableCount; }
        public void setTimetableCount(Long timetableCount) { this.timetableCount = timetableCount; }
    }
}