package org.timeflow.dao;

import org.timeflow.entity.User;
import org.timeflow.entity.UserRole;
import org.timeflow.entity.Department;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UserDAO extends BaseDAO<User, Long> {

    public UserDAO() {
        super(User.class);
    }

    // Find user by username
    public User findByUsername(String username) {
        String hql = "FROM User u WHERE u.username = ?0";
        return executeUniqueQuery(hql, username);
    }

    // Find user by email
    public User findByEmail(String email) {
        String hql = "FROM User u WHERE u.email = ?0";
        return executeUniqueQuery(hql, email);
    }

    // Authenticate user
    public User authenticate(String username, String password) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.username = :username AND u.isActive = true", User.class);
            query.setParameter("username", username);
            User user = query.uniqueResult();

            // Note: In production, use proper password hashing verification
            if (user != null && user.getPassword().equals(password)) {
                return user;
            }
            return null;
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", username, e);
            return null;
        }
    }

    // Find users by role
    public List<User> findByRole(UserRole role) {
        String hql = "FROM User u WHERE u.role = ?0 AND u.isActive = true";
        return executeQuery(hql, role);
    }

    // Find users by department
    public List<User> findByDepartment(Department department) {
        String hql = "FROM User u WHERE u.department = ?0 AND u.isActive = true";
        return executeQuery(hql, department);
    }

    // Find students by department and level
    public List<User> findStudentsByDepartmentAndLevel(Department department, int level) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery(
                    "SELECT DISTINCT u FROM User u " +
                            "JOIN Course c ON c.department = u.department " +
                            "WHERE u.role = :role AND u.department = :department " +
                            "AND c.level = :level AND u.isActive = true",
                    User.class);

            query.setParameter("role", UserRole.STUDENT);
            query.setParameter("department", department);
            query.setParameter("level", level);
            return query.list();
        }
    }

    // Find lecturers by department
    public List<User> findLecturersByDepartment(Department department) {
        String hql = "FROM User u WHERE u.role = ?0 AND u.department = ?1 AND u.isActive = true";
        return executeQuery(hql, UserRole.LECTURER, department);
    }

    // Deactivate user (soft delete)
    public void deactivateUser(Long userId) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user != null) {
                user.setActive(false);
                session.update(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to deactivate user", e);
        }
    }
}
