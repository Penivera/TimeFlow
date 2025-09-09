package org.timeflow.dao;

import org.timeflow.entity.*;
import org.hibernate.Session;
import jakarta.persistence.criteria.*;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ConflictDAO extends BaseDAO<Conflict, Long> {

    public ConflictDAO() {
        super(Conflict.class);
    }

    @Override
    public List<Conflict> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Conflict d ORDER BY d.name", Conflict.class)
                    .getResultList();
        }
    }

    public List<Conflict> findByStatus(ConflictStatus status) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            query.select(root)
                    .where(cb.equal(root.get("status"), status))
                    .orderBy(cb.desc(root.get("detectedAt")));

            // --- FIX: Removed incorrect cast ---
            return session.createQuery(query).getResultList();
        }
    }

    // Find conflicts by timetable using Criteria API
    public List<Conflict> findByTimetable(Timetable timetable) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            Predicate timetable1Match = cb.equal(root.get("timetable1"), timetable);
            Predicate timetable2Match = cb.equal(root.get("timetable2"), timetable);

            query.select(root)
                    .where(cb.or(timetable1Match, timetable2Match));

            // --- FIX: Removed incorrect cast ---
            return session.createQuery(query).getResultList();
        }
    }

    // Find conflicts by type using Criteria API
    public List<Conflict> findByType(ConflictType type) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            query.select(root)
                    .where(cb.equal(root.get("type"), type))
                    .orderBy(cb.desc(root.get("detectedAt")));

            // --- FIX: Removed incorrect cast ---
            return session.createQuery(query).getResultList();
        }
    }

    // Find unresolved conflicts using Criteria API
    public List<Conflict> findUnresolvedConflicts() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            List<ConflictStatus> unresolvedStatuses = List.of(ConflictStatus.DETECTED, ConflictStatus.APPEALED);

            query.select(root)
                    .where(root.get("status").in(unresolvedStatuses))
                    .orderBy(cb.desc(root.get("detectedAt")));

            // --- FIX: Removed incorrect cast ---
            return session.createQuery(query).getResultList();
        }
    }

    // Find conflicts for a specific department using Criteria API with joins
    public List<Conflict> findByDepartment(Department department) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            Join<Conflict, Timetable> timetable1Join = root.join("timetable1");
            Join<Timetable, Course> course1Join = timetable1Join.join("course");
            Join<Conflict, Timetable> timetable2Join = root.join("timetable2");
            Join<Timetable, Course> course2Join = timetable2Join.join("course");

            Predicate dept1Match = cb.equal(course1Join.get("department"), department);
            Predicate dept2Match = cb.equal(course2Join.get("department"), department);

            query.select(root)
                    .where(cb.or(dept1Match, dept2Match))
                    .orderBy(cb.desc(root.get("detectedAt")));

            // --- FIX: Removed incorrect cast ---
            return session.createQuery(query).getResultList();
        }
    }

    // Find conflicts involving a specific lecturer using Criteria API with joins
    public List<Conflict> findByLecturer(User lecturer) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            Join<Conflict, Timetable> timetable1Join = root.join("timetable1");
            Join<Timetable, Course> course1Join = timetable1Join.join("course");
            Join<Conflict, Timetable> timetable2Join = root.join("timetable2");
            Join<Timetable, Course> course2Join = timetable2Join.join("course");

            Predicate lecturer1Match = cb.equal(course1Join.get("lecturer"), lecturer);
            Predicate lecturer2Match = cb.equal(course2Join.get("lecturer"), lecturer);

            query.select(root)
                    .where(cb.or(lecturer1Match, lecturer2Match))
                    .orderBy(cb.desc(root.get("detectedAt")));

            // --- FIX: Removed incorrect cast ---
            return session.createQuery(query).getResultList();
        }
    }

    // Check if conflict already exists using Criteria API
    public Conflict findExistingConflict(Timetable timetable1, Timetable timetable2) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            Predicate combo1 = cb.and(
                    cb.equal(root.get("timetable1"), timetable1),
                    cb.equal(root.get("timetable2"), timetable2)
            );

            Predicate combo2 = cb.and(
                    cb.equal(root.get("timetable1"), timetable2),
                    cb.equal(root.get("timetable2"), timetable1)
            );

            query.select(root).where(cb.or(combo1, combo2));

            // --- FIX: Removed incorrect cast ---
            TypedQuery<Conflict> typedQuery = session.createQuery(query);
            List<Conflict> results = typedQuery.getResultList();

            return results.isEmpty() ? null : results.get(0);
        }
    }

    // Get conflict statistics using Criteria API with groupBy and multiselect
    public List<ConflictStatistic> getConflictStats() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ConflictStatistic> query = cb.createQuery(ConflictStatistic.class);
            Root<Conflict> root = query.from(Conflict.class);

            query.multiselect(
                            root.get("type"),
                            root.get("status"),
                            cb.count(root.get("id"))
                    )
                    .groupBy(root.get("type"), root.get("status"))
                    .orderBy(
                            cb.asc(root.get("type")),
                            cb.asc(root.get("status"))
                    );

            // --- FIX: Changed the return type to match the method signature ---
            return session.createQuery(query).getResultList();
        }
    }

    // Helper class for statistics result
    public static class ConflictStatistic {
        private ConflictType type;
        private ConflictStatus status;
        private Long count;

        public ConflictStatistic(ConflictType type, ConflictStatus status, Long count) {
            this.type = type;
            this.status = status;
            this.count = count;
        }

        // Getters and Setters
        public ConflictType getType() { return type; }
        public void setType(ConflictType type) { this.type = type; }
        public ConflictStatus getStatus() { return status; }
        public void setStatus(ConflictStatus status) { this.status = status; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}