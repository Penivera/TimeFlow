package org.timeflow.dao;

import org.timeflow.entity.Semester;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class SemesterDAO extends BaseDAO<Semester, Long> {

    public SemesterDAO() {
        super(Semester.class);
    }

    // Get current semester
    public Semester getCurrentSemester() {
        String hql = "FROM Semester s WHERE s.isCurrent = true";
        return executeUniqueQuery(hql);
    }

    // Find semester by name and academic year
    public Semester findByNameAndYear(String name, String academicYear) {
        String hql = "FROM Semester s WHERE s.name = ?0 AND s.academicYear = ?1";
        return executeUniqueQuery(hql, name, academicYear);
    }

    // Set current semester
    public void setCurrentSemester(Long semesterId) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            // First, set all semesters to not current
            Query updateQuery = session.createQuery("UPDATE Semester SET isCurrent = false");
            updateQuery.executeUpdate();

            // Then set the specified semester as current
            Semester semester = session.get(Semester.class, semesterId);
            if (semester != null) {
                semester.setCurrent(true);
                session.update(semester);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to set current semester", e);
        }
    }

    // Find semesters by academic year
    public List<Semester> findByAcademicYear(String academicYear) {
        String hql = "FROM Semester s WHERE s.academicYear = ?0 ORDER BY s.startDate";
        return executeQuery(hql, academicYear);
    }
}