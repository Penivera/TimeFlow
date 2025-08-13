package org.timeflow.dao;

import org.timeflow.entity.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ConflictDAO extends BaseDAO<Conflict, Long> {

    public ConflictDAO() {
        super(Conflict.class);
    }

    // Find conflicts by status
    public List<Conflict> findByStatus(ConflictStatus status) {
        String hql = "FROM Conflict c WHERE c.status = ?0 ORDER BY c.detectedAt DESC";
        return executeQuery(hql, status);
    }

    // Find conflicts by timetable
    public List<Conflict> findByTimetable(Timetable timetable) {
        String hql = "FROM Conflict c WHERE c.timetable1 = ?0 OR c.timetable2 = ?0";
        return executeQuery(hql, timetable);
    }

    // Find conflicts by type
    public List<Conflict> findByType(ConflictType type) {
        String hql = "FROM Conflict c WHERE c.type = ?0 ORDER BY c.detectedAt DESC";
        return executeQuery(hql, type);
    }

    // Find unresolved conflicts
    public List<Conflict> findUnresolvedConflicts() {
        try (Session session = sessionFactory.openSession()) {
            Query<Conflict> query = session.createQuery(
                    "FROM Conflict c WHERE c.status IN (:statuses) ORDER BY c.detectedAt DESC",
                    Conflict.class);
            query.setParameterList("statuses", List.of(ConflictStatus.DETECTED, ConflictStatus.APPEALED));
            return query.list();
        }
    }

    // Find conflicts for a specific department
    public List<Conflict> findByDepartment(Department department) {
        try (Session session = sessionFactory.openSession()) {
            Query<Conflict> query = session.createQuery(
                    "FROM Conflict c WHERE " +
                            "c.timetable1.course.department = :department " +
                            "OR c.timetable2.course.department = :department " +
                            "ORDER BY c.detectedAt DESC",
                    Conflict.class);
            query.setParameter("department", department);
            return query.list();
        }
    }

    // Find conflicts involving a specific lecturer
    public List<Conflict> findByLecturer(User lecturer) {
        try (Session session = sessionFactory.openSession()) {
            Query<Conflict> query = session.createQuery(
                    "FROM Conflict c WHERE " +
                            "c.timetable1.course.lecturer = :lecturer " +
                            "OR c.timetable2.course.lecturer = :lecturer " +
                            "ORDER BY c.detectedAt DESC",
                    Conflict.class);
            query.setParameter("lecturer", lecturer);
            return query.list();
        }
    }

    // Check if conflict already exists
    public Conflict findExistingConflict(Timetable timetable1, Timetable timetable2) {
        try (Session session = sessionFactory.openSession()) {
            Query<Conflict> query = session.createQuery(
                    "FROM Conflict c WHERE " +
                            "(c.timetable1 = :t1 AND c.timetable2 = :t2) " +
                            "OR (c.timetable1 = :t2 AND c.timetable2 = :t1)",
                    Conflict.class);
            query.setParameter("t1", timetable1);
            query.setParameter("t2", timetable2);
            return query.uniqueResult();
        }
    }

    // Get conflict statistics
    public List<Object[]> getConflictStats() {
        try (Session session = sessionFactory.openSession()) {
            Query<Object[]> query = session.createQuery(
                    "SELECT c.type, c.status, COUNT(c.id) " +
                            "FROM Conflict c " +
                            "GROUP BY c.type, c.status " +
                            "ORDER BY c.type, c.status",
                    Object[].class);
            return query.list();
        }
    }
}
