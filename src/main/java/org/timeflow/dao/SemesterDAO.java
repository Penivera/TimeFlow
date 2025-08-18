package org.timeflow.dao;

import org.timeflow.entity.Semester;
import org.hibernate.Session;
import org.hibernate.Transaction;
import jakarta.persistence.criteria.*;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class SemesterDAO extends BaseDAO<Semester, Long> {

    public SemesterDAO() {
        super(Semester.class);
    }

    // Get current semester using Criteria API
    public Semester getCurrentSemester() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Semester> query = cb.createQuery(Semester.class);
            Root<Semester> root = query.from(Semester.class);

            query.select(root).where(cb.isTrue(root.get("isCurrent")));

            TypedQuery<Semester> typedQuery = (TypedQuery<Semester>) session.createQuery(String.valueOf(query));
            List<Semester> results = typedQuery.getResultList();

            return results.isEmpty() ? null : results.get(0);
        }
    }

    // Find semester by name and academic year using Criteria API
    public Semester findByNameAndYear(String name, String academicYear) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Semester> query = cb.createQuery(Semester.class);
            Root<Semester> root = query.from(Semester.class);

            Predicate nameMatch = cb.equal(root.get("name"), name);
            Predicate yearMatch = cb.equal(root.get("academicYear"), academicYear);

            query.select(root).where(cb.and(nameMatch, yearMatch));

            TypedQuery<Semester> typedQuery = (TypedQuery<Semester>) session.createQuery(String.valueOf(query));
            List<Semester> results = typedQuery.getResultList();

            return results.isEmpty() ? null : results.get(0);
        }
    }

    // Set current semester using Criteria API
    public void setCurrentSemester(Long semesterId) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            // First, set all semesters to not current
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaUpdate<Semester> updateAll = cb.createCriteriaUpdate(Semester.class);
            Root<Semester> rootAll = updateAll.from(Semester.class);
            updateAll.set(rootAll.get("isCurrent"), false);
            session.createQuery((jakarta.persistence.criteria.CriteriaUpdate) updateAll).executeUpdate();

            // Then set the specified semester as current
            CriteriaUpdate<Semester> updateCurrent = cb.createCriteriaUpdate(Semester.class);
            Root<Semester> rootCurrent = updateCurrent.from(Semester.class);
            updateCurrent.set(rootCurrent.get("isCurrent"), true)
                    .where(cb.equal(rootCurrent.get("id"), semesterId));
            session.createQuery((jakarta.persistence.criteria.CriteriaUpdate) updateCurrent).executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to set current semester", e);
        }
    }

    // Find semesters by academic year using Criteria API
    public List<Semester> findByAcademicYear(String academicYear) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Semester> query = cb.createQuery(Semester.class);
            Root<Semester> root = query.from(Semester.class);

            query.select(root)
                    .where(cb.equal(root.get("academicYear"), academicYear))
                    .orderBy(cb.asc(root.get("startDate")));

            return session.createQuery((jakarta.persistence.criteria.CriteriaUpdate) query).getResultList();
        }
    }
}
