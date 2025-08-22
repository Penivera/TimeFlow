package org.timeflow.service;

import org.timeflow.entity.User;
import org.timeflow.entity.UserRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthenticationService extends BaseService {
    private static final AuthenticationService INSTANCE = new AuthenticationService();
    private final BCryptPasswordEncoder passwordEncoder;
    private User currentUser;

    private AuthenticationService() {
        super();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public static AuthenticationService getInstance() {
        return INSTANCE;
    }

    public boolean login(String username, String password) {
        try {
            User user = daoFactory.getUserDAO().findByUsername(username);
            if (user != null && user.isActive() && verifyPassword(password, user.getPassword())) {
                currentUser = user;
                logger.info("User logged in successfully: {}", username);
                return true;
            }
            logger.warn("Failed login attempt for username: {}", username);
            return false;
        } catch (Exception e) {
            logger.error("Error during login for username: {}", username, e);
            return false;
        }
    }

    public User registerUser(String username, String email, String password, UserRole role, Long departmentId) {
        try {
            if (daoFactory.getUserDAO().findByUsername(username) != null) {
                throw new RuntimeException("Username already exists");
            }
            if (daoFactory.getUserDAO().findByEmail(email) != null) {
                throw new RuntimeException("Email already exists");
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(hashPassword(password));
            user.setRole(role);
            user.setDepartment(daoFactory.getDepartmentDAO().findById(departmentId));

            User savedUser = daoFactory.getUserDAO().save(user);
            logger.info("New user registered: {} with role {}", username, role);
            return savedUser;
        } catch (Exception e) {
            logger.error("Error registering user: {}", username, e);
            throw new RuntimeException("Failed to register user", e);
        }
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        try {
            if (currentUser != null && verifyPassword(oldPassword, currentUser.getPassword())) {
                currentUser.setPassword(hashPassword(newPassword));
                daoFactory.getUserDAO().update(currentUser);
                logger.info("Password changed for user: {}", currentUser.getUsername());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error changing password for user: {}", currentUser.getUsername(), e);
            return false;
        }
    }

    public void logout() {
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getUsername());
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public boolean canManageTimetables() {
        return hasRole(UserRole.ADMIN) || hasRole(UserRole.EXAMS_OFFICER) || hasRole(UserRole.LECTURER);
    }

    public boolean canApproveTimetables() {
        return hasRole(UserRole.ADMIN) || hasRole(UserRole.EXAMS_OFFICER);
    }

    public boolean canViewAllTimetables() {
        return hasRole(UserRole.ADMIN) || hasRole(UserRole.EXAMS_OFFICER);
    }

    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    private boolean verifyPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }
}