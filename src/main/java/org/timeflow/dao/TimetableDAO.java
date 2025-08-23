package org.timeflow.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.timeflow.entity.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.function.Consumer;

public class TimetableDAO extends BaseDAO<Timetable, Long> {

    public TimetableDAO() {
        super(Timetable.class);
    }

    private <T> List<T> executeQuery(String hql, Class<T> resultClass, Consumer<Query<T>> paramSetter) {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery(hql, resultClass);
            paramSetter.accept(query);
            return query.list();
        }
    }

    public List<Timetable> findByCourse(Course course) {
        return executeQuery(
                "FROM Timetable t WHERE t.course = :course ORDER BY t.dayOfWeek, t.startTime",
                Timetable.class,
                query -> query.setParameter("course", course)
        );
    }

    // MODIFIED: Parameter changed to SemesterType
    public List<Timetable> findBySemester(SemesterType semester) {
        return executeQuery(
                "FROM Timetable t WHERE t.semester = :semester ORDER BY t.dayOfWeek, t.startTime",
                Timetable.class,
                query -> query.setParameter("semester", semester)
        );
    }

    // MODIFIED: Parameter changed to SemesterType
    public List<Timetable> findByDepartmentAndSemester(Department department, SemesterType semester) {
        return executeQuery(
                "FROM Timetable t " +
                        "WHERE t.course.department = :department " +
                        "AND t.semester = :semester " +
                        "ORDER BY t.dayOfWeek, t.startTime",
                Timetable.class,
                query -> {
                    query.setParameter("department", department);
                    query.setParameter("semester", semester);
                }
        );
    }

    public List<Timetable> findApprovedTimetables(Department department, int level, SemesterType semester) {
        return executeQuery(
                "FROM Timetable t " +
                        "WHERE t.course.department = :department " +
                        "AND t.course.level = :level " +
                        "AND t.semester = :semester " +
                        "AND t.status = :status " +
                        "ORDER BY t.dayOfWeek, t.startTime",
                Timetable.class,
                query -> {
                    query.setParameter("department", department);
                    query.setParameter("level", level);
                    query.setParameter("semester", semester);
                    query.setParameter("status", TimetableStatus.APPROVED);
                }
        );
    }

    public List<Timetable> findByStatus(TimetableStatus status) {
        return executeQuery(
                "FROM Timetable t WHERE t.status = :status ORDER BY t.createdAt DESC",
                Timetable.class,
                query -> query.setParameter("status", status)
        );
    }

    public List<Timetable> findByLecturer(User lecturer, SemesterType semester) {
        return executeQuery(
                "FROM Timetable t " +
                        "WHERE t.course.lecturer = :lecturer " +
                        "AND t.semester = :semester " +
                        "ORDER BY t.dayOfWeek, t.startTime",
                Timetable.class,
                query -> {
                    query.setParameter("lecturer", lecturer);
                    query.setParameter("semester", semester);
                }
        );
    }

    // MODIFIED: Parameter changed to SemesterType
    public List<Timetable> findConflictingTimetables(DayOfWeek dayOfWeek, LocalTime startTime,
                                                     LocalTime endTime, String room,
                                                     SemesterType semester, Long excludeId) {
        String hql =
                "FROM Timetable t " +
                        "WHERE t.semester = :semester " +
                        "AND t.dayOfWeek = :dayOfWeek " +
                        "AND t.startTime < :endTime AND t.endTime > :startTime " +
                        "AND t.room = :room " + // This was an OR, should be AND for true conflict
                        "AND t.status != :rejectedStatus" +
                        (excludeId != null ? " AND t.id != :excludeId" : "");

        return executeQuery(
                hql,
                Timetable.class,
                query -> {
                    query.setParameter("semester", semester);
                    query.setParameter("dayOfWeek", dayOfWeek);
                    query.setParameter("startTime", startTime);
                    query.setParameter("endTime", endTime);
                    query.setParameter("room", room);
                    query.setParameter("rejectedStatus", TimetableStatus.REJECTED);
                    if (excludeId != null) {
                        query.setParameter("excludeId", excludeId);
                    }
                }
        );
    }

    // MODIFIED: Parameter changed to SemesterType
    public List<Timetable> findLecturerConflicts(User lecturer, DayOfWeek dayOfWeek,
                                                 LocalTime startTime, LocalTime endTime,
                                                 SemesterType semester, Long excludeId) {
        String hql =
                "FROM Timetable t " +
                        "WHERE t.course.lecturer = :lecturer " +
                        "AND t.semester = :semester " +
                        "AND t.dayOfWeek = :dayOfWeek " +
                        "AND t.startTime < :endTime AND t.endTime > :startTime " +
                        "AND t.status != :rejectedStatus" +
                        (excludeId != null ? " AND t.id != :excludeId" : "");

        return executeQuery(
                hql,
                Timetable.class,
                query -> {
                    query.setParameter("lecturer", lecturer);
                    query.setParameter("semester", semester);
                    query.setParameter("dayOfWeek", dayOfWeek);
                    query.setParameter("startTime", startTime);
                    query.setParameter("endTime", endTime);
                    query.setParameter("rejectedStatus", TimetableStatus.REJECTED);
                    if (excludeId != null) {
                        query.setParameter("excludeId", excludeId);
                    }
                }
        );
    }

    public List<Timetable> findExamsByDateRange(LocalDate startDate, LocalDate endDate, Department department) {
        return executeQuery(
                "FROM Timetable t " +
                        "WHERE t.specificDate BETWEEN :startDate AND :endDate " +
                        "AND t.type IN (:examTypes) " +
                        "AND t.course.department = :department " +
                        "AND t.status = :approvedStatus " +
                        "ORDER BY t.specificDate, t.startTime",
                Timetable.class,
                query -> {
                    query.setParameter("startDate", startDate);
                    query.setParameter("endDate", endDate);
                    query.setParameter("examTypes", List.of(ActivityType.EXAM, ActivityType.TEST));
                    query.setParameter("department", department);
                    query.setParameter("approvedStatus", TimetableStatus.APPROVED);
                }
        );
    }

    // MODIFIED: Parameter changed to SemesterType
    public List<Object[]> getTimetableStats(SemesterType semester) {
        return executeQuery(
                "SELECT d.name, t.status, COUNT(t.id) " +
                        "FROM Timetable t " +
                        "JOIN t.course c " +
                        "JOIN c.department d " +
                        "WHERE t.semester = :semester " +
                        "GROUP BY d.name, t.status " +
                        "ORDER BY d.name",
                Object[].class,
                query -> query.setParameter("semester", semester)
        );
    }
}