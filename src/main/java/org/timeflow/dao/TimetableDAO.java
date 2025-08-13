package org.timeflow.dao;

import org.timeflow.entity.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TimetableDAO extends BaseDAO<Timetable, Long> {

    public TimetableDAO() {
        super(Timetable.class);
    }

    // Find timetables by course
    public List<Timetable> findByCourse(Course course) {
        String hql = "FROM Timetable t WHERE t.course = ?0 ORDER BY t.dayOfWeek, t.startTime";
        return executeQuery(hql, course);
    }

    // Find timetables by semester
    public List<Timetable> findBySemester(Semester semester) {
        String hql = "FROM Timetable t WHERE t.semester = ?0 ORDER BY t.dayOfWeek, t.startTime";
        return executeQuery(hql, semester);
    }

    // Find timetables by department and semester
    public List<Timetable> findByDepartmentAndSemester(Department department, Semester semester) {
        try (Session session = sessionFactory.openSession()) {
            Query<Timetable> query = session.createQuery(
                    "FROM Timetable t " +
                            "WHERE t.course.department = :department " +
                            "AND t.semester = :semester " +
                            "ORDER BY t.dayOfWeek, t.startTime",
                    Timetable.class);

            query.setParameter("department", department);
            query.setParameter("semester", semester);
            return query.list();
        }
    }

    // Find approved timetables for students
    public List<Timetable> findApprovedTimetables(Department department, int level, Semester semester) {
        try (Session session = sessionFactory.openSession()) {
            Query<Timetable> query = session.createQuery(
                    "FROM Timetable t " +
                            "WHERE t.course.department = :department " +
                            "AND t.course.level = :level " +
                            "AND t.semester = :semester " +
                            "AND t.status = :status " +
                            "ORDER BY t.dayOfWeek, t.startTime",
                    Timetable.class);

            query.setParameter("department", department);
            query.setParameter("level", level);
            query.setParameter("semester", semester);
            query.setParameter("status", TimetableStatus.APPROVED);
            return query.list();
        }
    }

    // Find timetables by status
    public List<Timetable> findByStatus(TimetableStatus status) {
        String hql = "FROM Timetable t WHERE t.status = ?0 ORDER BY t.createdAt DESC";
        return executeQuery(hql, status);
    }

    // Find timetables by lecturer
    public List<Timetable> findByLecturer(User lecturer, Semester semester) {
        try (Session session = sessionFactory.openSession()) {
            Query<Timetable> query = session.createQuery(
                    "FROM Timetable t " +
                            "WHERE t.course.lecturer = :lecturer " +
                            "AND t.semester = :semester " +
                            "ORDER BY t.dayOfWeek, t.startTime",
                    Timetable.class);

            query.setParameter("lecturer", lecturer);
            query.setParameter("semester", semester);
            return query.list();
        }
    }

    // Check for time conflicts
    public List<Timetable> findConflictingTimetables(DayOfWeek dayOfWeek, LocalTime startTime,
            LocalTime endTime, String room, Semester semester, Long excludeId) {
        try (Session session = sessionFactory.openSession()) {
            StringBuilder hql = new StringBuilder(
                    "FROM Timetable t WHERE t.semester = :semester " +
                            "AND t.dayOfWeek = :dayOfWeek " +
                            "AND (" +
                            "(t.startTime < :endTime AND t.endTime > :startTime) " + // Time overlap
                            "OR (t.room = :room)" + // Room conflict
                            ") " +
                            "AND t.status != :rejectedStatus");

            if (excludeId != null) {
                hql.append(" AND t.id != :excludeId");
            }

            Query<Timetable> query = session.createQuery(hql.toString(), Timetable.class);
            query.setParameter("semester", semester);
            query.setParameter("dayOfWeek", dayOfWeek);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            query.setParameter("room", room);
            query.setParameter("rejectedStatus", TimetableStatus.REJECTED);

            if (excludeId != null) {
                query.setParameter("excludeId", excludeId);
            }

            return query.list();
        }
    }

    // Find lecturer conflicts
    public List<Timetable> findLecturerConflicts(User lecturer, DayOfWeek dayOfWeek,
            LocalTime startTime, LocalTime endTime,
            Semester semester, Long excludeId) {
        try (Session session = sessionFactory.openSession()) {
            StringBuilder hql = new StringBuilder(
                    "FROM Timetable t WHERE t.course.lecturer = :lecturer " +
                            "AND t.semester = :semester " +
                            "AND t.dayOfWeek = :dayOfWeek " +
                            "AND t.startTime < :endTime AND t.endTime > :startTime " +
                            "AND t.status != :rejectedStatus");

            if (excludeId != null) {
                hql.append(" AND t.id != :excludeId");
            }

            Query<Timetable> query = session.createQuery(hql.toString(), Timetable.class);
            query.setParameter("lecturer", lecturer);
            query.setParameter("semester", semester);
            query.setParameter("dayOfWeek", dayOfWeek);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            query.setParameter("rejectedStatus", TimetableStatus.REJECTED);

            if (excludeId != null) {
                query.setParameter("excludeId", excludeId);
            }

            return query.list();
        }
    }

    // Find exams and tests by date range
    public List<Timetable> findExamsByDateRange(LocalDate startDate, LocalDate endDate, Department department) {
        try (Session session = sessionFactory.openSession()) {
            Query<Timetable> query = session.createQuery(
                    "FROM Timetable t " +
                            "WHERE t.specificDate BETWEEN :startDate AND :endDate " +
                            "AND t.type IN (:examTypes) " +
                            "AND t.course.department = :department " +
                            "AND t.status = :approvedStatus " +
                            "ORDER BY t.specificDate, t.startTime",
                    Timetable.class);

            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            query.setParameterList("examTypes", List.of(ActivityType.EXAM, ActivityType.TEST));
            query.setParameter("department", department);
            query.setParameter("approvedStatus", TimetableStatus.APPROVED);
            return query.list();
        }
    }

    // Get timetable statistics
    public List<Object[]> getTimetableStats(Semester semester) {
        try (Session session = sessionFactory.openSession()) {
            Query<Object[]> query = session.createQuery(
                    "SELECT d.name, t.status, COUNT(t.id) " +
                            "FROM Timetable t " +
                            "JOIN t.course c " +
                            "JOIN c.department d " +
                            "WHERE t.semester = :semester " +
                            "GROUP BY d.name, t.status " +
                            "ORDER BY d.name",
                    Object[].class);

            query.setParameter("semester", semester);
            return query.list();
        }
    }
}