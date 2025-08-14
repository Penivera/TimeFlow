package org.timeflow.dao;

import org.hibernate.Session;
import org.timeflow.entity.Department;
import jakarta.persistence.criteria.*;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class DepartmentDAO extends BaseDAO<Department, Long> {

    public DepartmentDAO() {
        super(Department.class);
    }

    // Find department by code using Criteria API
    public Department findByCode(String code) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Department> query = cb.createQuery(Department.class);
            Root<Department> root = query.from(Department.class);

            query.select(root).where(cb.equal(root.get("code"), code));

            TypedQuery<Department> typedQuery = (TypedQuery<Department>) session.createQuery(String.valueOf(query));
            List<Department> results = typedQuery.getResultList();

            return results.isEmpty() ? null : results.get(0);
        }
    }

    // Find department by name using Criteria API
    public Department findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<Department> query = cb.createQuery(Department.class);
            Root<Department> root = query.from(Department.class);

            query.select(root).where(cb.equal(root.get("name"), name));

            TypedQuery<Department> typedQuery = (TypedQuery<Department>) session.createQuery(String.valueOf(query));
            List<Department> results = typedQuery.getResultList();

            return results.isEmpty() ? null : results.get(0);
        }
    }

    // Get department statistics using Criteria API
    public List<DepartmentStatistic> getDepartmentStats() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = (CriteriaBuilder) session.getCriteriaBuilder();
            CriteriaQuery<DepartmentStatistic> query = cb.createQuery(DepartmentStatistic.class);
            Root<Department> root = query.from(Department.class);

            // Left join with courses
            Join<Department, Object> coursesJoin = root.join("courses", JoinType.LEFT);

            query.multiselect(
                            root.get("name"),
                            root.get("code"),
                            cb.count(coursesJoin.get("id"))
                    )
                    .groupBy(root.get("id"), root.get("name"), root.get("code"))
                    .orderBy(cb.asc(root.get("name")));

            return session.createQuery((javax.persistence.criteria.CriteriaUpdate) query).getResultList();
        }
    }

    public static class DepartmentStatistic {
        private String name;
        private String code;
        private Long courseCount;

        public DepartmentStatistic(String name, String code, Long courseCount) {
            this.name = name;
            this.code = code;
            this.courseCount = courseCount;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public Long getCourseCount() { return courseCount; }
        public void setCourseCount(Long courseCount) { this.courseCount = courseCount; }
    }
}