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

    // Find conflicts by status using Criteria API
    public List findByStatus(ConflictStatus status) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            query.select(root)
                    .where(cb.equal(root.get("status"), status))
                    .orderBy(cb.desc(root.get("detectedAt")));

            return session.createQuery((jakarta.persistence.criteria.CriteriaUpdate) query).getResultList();
        }
    }

    // Find conflicts by timetable using Criteria API
    public List<Conflict> findByTimetable(Timetable timetable) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            Predicate timetable1Match = cb.equal(root.get("timetable1"), timetable);
            Predicate timetable2Match = cb.equal(root.get("timetable2"), timetable);

            query.select(root)
                    .where(cb.or(timetable1Match, timetable2Match));

            return session.createQuery((jakarta.persistence.criteria.CriteriaUpdate) query).getResultList();
        }
    }

    // Find conflicts by type using Criteria API
    public List<Conflict> findByType(ConflictType type) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            query.select(root)
                    .where(cb.equal(root.get("type"), type))
                    .orderBy(cb.desc(root.get("detectedAt")));

            return session.createQuery((jakarta.persistence.criteria.CriteriaUpdate) query).getResultList();
        }
    }

    // Find unresolved conflicts using Criteria API
    public List<Conflict> findUnresolvedConflicts() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            List<ConflictStatus> unresolvedStatuses = List.of(ConflictStatus.DETECTED, ConflictStatus.APPEALED);

            query.select(root)
                    .where(root.get("status").in(unresolvedStatuses))
                    .orderBy(cb.desc(root.get("detectedAt")));

            return session.createQuery((jakarta.persistence.criteria.CriteriaUpdate) query).getResultList();
        }
    }

    // Find conflicts for a specific department using Criteria API with joins
    public List<Conflict> findByDepartment(Department department) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            // Join paths for timetable1 -> course -> department
            Join<Conflict, Timetable> timetable1Join = root.join("timetable1");
            Join<Timetable, Course> course1Join = timetable1Join.join("course");

            // Join paths for timetable2 -> course -> department
            Join<Conflict, Timetable> timetable2Join = root.join("timetable2");
            Join<Timetable, Course> course2Join = timetable2Join.join("course");

            Predicate dept1Match = cb.equal(course1Join.get("department"), department);
            Predicate dept2Match = cb.equal(course2Join.get("department"), department);

            query.select(root)
                    .where(cb.or(dept1Match, dept2Match))
                    .orderBy(cb.desc(root.get("detectedAt")));

            return session.createQuery((jakarta.persistence.criteria.CriteriaUpdate) query).getResultList();
        }
    }

    // Find conflicts involving a specific lecturer using Criteria API with joins
    public List<Conflict> findByLecturer(User lecturer) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            // Join paths for timetable1 -> course -> lecturer
            Join<Conflict, Timetable> timetable1Join = root.join("timetable1");
            Join<Timetable, Course> course1Join = timetable1Join.join("course");

            // Join paths for timetable2 -> course -> lecturer
            Join<Conflict, Timetable> timetable2Join = root.join("timetable2");
            Join<Timetable, Course> course2Join = timetable2Join.join("course");

            Predicate lecturer1Match = cb.equal(course1Join.get("lecturer"), lecturer);
            Predicate lecturer2Match = cb.equal(course2Join.get("lecturer"), lecturer);

            query.select(root)
                    .where(cb.or(lecturer1Match, lecturer2Match))
                    .orderBy(cb.desc(root.get("detectedAt")));

            return session.createQuery((jakarta.persistence.criteria.CriteriaUpdate) query).getResultList();
        }
    }

    // Check if conflict already exists using Criteria API
    public Conflict findExistingConflict(Timetable timetable1, Timetable timetable2) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Conflict> query = cb.createQuery(Conflict.class);
            Root<Conflict> root = query.from(Conflict.class);

            // Check both combinations: (t1, t2) and (t2, t1)
            Predicate combo1 = cb.and(
                    cb.equal(root.get("timetable1"), timetable1),
                    cb.equal(root.get("timetable2"), timetable2)
            );

            Predicate combo2 = cb.and(
                    cb.equal(root.get("timetable1"), timetable2),
                    cb.equal(root.get("timetable2"), timetable1)
            );

            query.select(root).where(cb.or(combo1, combo2));

            TypedQuery<Conflict> typedQuery = (TypedQuery<Conflict>) session.createQuery(String.valueOf(query));
            List<Conflict> results = typedQuery.getResultList();

            return results.isEmpty() ? null : results.get(0);
        }
    }

    // Get conflict statistics using Criteria API with groupBy and multiselect
    public List<Object> getConflictStats() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
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

            return session.createQuery((jakarta.persistence.criteria.CriteriaUpdate) query).getResultList();
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

        // Getters
        public ConflictType getType() { return type; }
        public ConflictStatus getStatus() { return status; }
        public Long getCount() { return count; }

        // Setters
        public void setType(ConflictType type) { this.type = type; }
        public void setStatus(ConflictStatus status) { this.status = status; }
        public void setCount(Long count) { this.count = count; }
    }
}