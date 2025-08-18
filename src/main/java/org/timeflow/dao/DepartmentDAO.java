package org.timeflow.dao;

import org.hibernate.Session;
import org.timeflow.entity.Department;

import java.util.List;

public class DepartmentDAO extends BaseDAO<Department, Long> {
    public DepartmentDAO() {
        super(Department.class);
    }

    @Override
    public List<Department> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Department d ORDER BY d.name", Department.class)
                    .getResultList();
        }
    }

    public Department findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Department d WHERE d.name = :name", Department.class)
                    .setParameter("name", name)
                    .uniqueResult();
        }
    }

    public Department findByCode(String code) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Department d WHERE d.code = :code", Department.class)
                    .setParameter("code", code)
                    .uniqueResult();
        }
    }
}