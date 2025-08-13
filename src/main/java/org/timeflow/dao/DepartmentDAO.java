package org.timeflow.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.timeflow.entity.Department;
import java.util.List;

public class DepartmentDAO extends BaseDAO<Department, Long> {

    public DepartmentDAO() {
        super(Department.class);
    }

    // Find department by code
    public Department findByCode(String code) {
        String hql = "FROM Department d WHERE d.code = ?0";
        return executeUniqueQuery(hql, code);
    }

    // Find department by name
    public Department findByName(String name) {
        String hql = "FROM Department d WHERE d.name = ?0";
        return executeUniqueQuery(hql, name);
    }

    // Get all departments with their course count
    public List<Object[]> getDepartmentStats() {
        try (Session session = sessionFactory.openSession()) {
            Query<Object[]> query = session.createQuery(
                    "SELECT d.name, d.code, COUNT(c.id) as courseCount " +
                            "FROM Department d LEFT JOIN d.courses c " +
                            "GROUP BY d.id, d.name, d.code " +
                            "ORDER BY d.name",
                    Object[].class);
            return query.list();
        }
    }
}
