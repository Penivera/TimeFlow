package org.timeflow.dao;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.timeflow.entity.User;
import org.timeflow.entity.UserRole;
import org.timeflow.entity.Department;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.function.Consumer;

public class UserDAO extends BaseDAO<User, Long> {

    public UserDAO() {
        super(User.class);
    }

    // Find user by username
    public User findByUsername(String username) {
        return executeUniqueQuery(
                "FROM User u WHERE u.username = :username",
                User.class,
                query -> query.setParameter("username", username)
        );
    }

    // Find user by email
    public User findByEmail(String email) {
        return executeUniqueQuery(
                "FROM User u WHERE u.email = :email",
                User.class,
                query -> query.setParameter("email", email)
        );
    }

    // Authenticate user
    public User authenticate(String username, String password) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.bySimpleNaturalId(User.class).load(username);
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            // Note: In production, use proper password hashing verification
            if (user != null && encoder.matches(password,user.getPassword())) {
                logger.info("{} authenticated",user.getUsername());
                return user;
            }
            logger.info("Incorrect username or password");
            return null;
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", username, e);
            return null;
        }
    }

    // Find users by role
    public List<User> findByRole(UserRole role) {
        return executeQuery(
                "FROM User u WHERE u.role = :role AND u.isActive = true",
                User.class,
                query -> query.setParameter("role", role)
        );
    }

    // Find users by department
    public List<User> findByDepartment(Department department) {
        return executeQuery(
                "FROM User u WHERE u.department = :department AND u.isActive = true",
                User.class,
                query -> query.setParameter("department", department)
        );
    }

    // Find students by department and level
    public List<User> findStudentsByDepartmentAndLevel(Department department, int level) {
        return executeQuery(
                "SELECT DISTINCT u FROM User u " +
                        "JOIN Course c ON c.department = u.department " +
                        "WHERE u.role = :role AND u.department = :department " +
                        "AND c.level = :level AND u.isActive = true",
                User.class,
                query -> {
                    query.setParameter("role", UserRole.STUDENT);
                    query.setParameter("department", department);
                    query.setParameter("level", level);
                }
        );
    }

    // Find lecturers by department
    public List<User> findLecturersByDepartment(Department department) {
        return executeQuery(
                "FROM User u WHERE u.role = :role AND u.department = :department AND u.isActive = true",
                User.class,
                query -> {
                    query.setParameter("role", UserRole.LECTURER);
                    query.setParameter("department", department);
                }
        );
    }

    // Deactivate user (soft delete)
    public void deactivateUser(Long userId) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            User user = session.find(User.class, userId);
            if (user != null) {
                user.setActive(false);
                session.merge(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to deactivate user", e);
        }
    }

    private <T> List<T> executeQuery(String hql, Class<T> resultClass, Consumer<Query<T>> paramSetter) {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery(hql, resultClass);
            paramSetter.accept(query);
            return query.list();
        }
    }

    private <T> T executeUniqueQuery(String hql, Class<T> resultClass, Consumer<Query<T>> paramSetter) {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery(hql, resultClass);
            paramSetter.accept(query);
            return query.uniqueResult();
        }
    }
}