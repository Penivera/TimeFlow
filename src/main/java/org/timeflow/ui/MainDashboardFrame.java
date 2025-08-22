package org.timeflow.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.service.AuthenticationService;
import org.timeflow.entity.User;
import org.timeflow.entity.UserRole;

import javax.swing.*;
import java.awt.*;

public class MainDashboardFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainDashboardFrame.class);
    private AuthenticationService authService;

    public MainDashboardFrame() {
        authService = AuthenticationService.getInstance();
        initComponents();
        setTitle("TimeFlow - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setMinimumSize(new Dimension(350, 500));
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initComponents() {
        User user = authService.getCurrentUser();
        logger.info("Initializing MainDashboardFrame for user: {}, role: {}",
                user != null ? user.getUsername() : "null",
                user != null ? user.getRole() : "null");

        if (user == null) {
            logger.warn("No user logged in, redirecting to LoginFrame");
            JOptionPane.showMessageDialog(this, "No user logged in. Please log in.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
            return;
        }

        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getUsername());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(7, 8, 9));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);

        JButton logoutButton = createStyledButton("Logout");
        logoutButton.addActionListener(e -> {
            logger.info("User logged out: {}", user.getUsername());
            authService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
        headerPanel.add(logoutButton, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonPanel.setBackground(Color.WHITE);

        switch (user.getRole()) {
            case ADMIN:
                addAdminButtons(buttonPanel);
                break;
            case LECTURER:
                addLecturerButtons(buttonPanel);
                break;
            case EXAMS_OFFICER:
                addExamsOfficerButtons(buttonPanel);
                break;
            case STUDENT:
                addStudentButtons(buttonPanel);
                break;
            default:
                logger.warn("Unknown role for user: {}, role: {}", user.getUsername(), user.getRole());
                JOptionPane.showMessageDialog(this, "Unknown user role", "Error", JOptionPane.ERROR_MESSAGE);
                addStudentButtons(buttonPanel);
        }

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void addAdminButtons(JPanel panel) {
        logger.info("Adding ADMIN buttons");
        JButton createScheduleButton = createStyledButton("Create Schedule");
        createScheduleButton.addActionListener(e -> new TimetableFrame(authService.getCurrentUser(), true).setVisible(true));
        panel.add(createScheduleButton);

        JButton timetableButton = createStyledButton("Manage Timetables");
        timetableButton.addActionListener(e -> new TimetableFrame(authService.getCurrentUser(), false).setVisible(true));
        panel.add(timetableButton);

        JButton courseButton = createStyledButton("Manage Courses");
        courseButton.addActionListener(e -> new CourseManagementFrame(authService.getCurrentUser()).setVisible(true));
        panel.add(courseButton);

        JButton conflictButton = createStyledButton("Resolve Conflicts");
        conflictButton.addActionListener(e -> new ConflictManagementFrame(authService.getCurrentUser()).setVisible(true));
        panel.add(conflictButton);
    }

    private void addLecturerButtons(JPanel panel) {
        logger.info("Adding LECTURER buttons");
        JButton createScheduleButton = createStyledButton("Create Schedule");
        createScheduleButton.addActionListener(e -> new TimetableFrame(authService.getCurrentUser(), true).setVisible(true));
        panel.add(createScheduleButton);

        JButton viewTimetableButton = createStyledButton("View My Timetable");
        viewTimetableButton.addActionListener(e -> new TimetableFrame(authService.getCurrentUser(), false).setVisible(true));
        panel.add(viewTimetableButton);

        JButton conflictButton = createStyledButton("View Conflicts");
        conflictButton.addActionListener(e -> new ConflictManagementFrame(authService.getCurrentUser()).setVisible(true));
        panel.add(conflictButton);
    }

    private void addExamsOfficerButtons(JPanel panel) {
        logger.info("Adding EXAMS_OFFICER buttons");
        JButton createScheduleButton = createStyledButton("Create Schedule");
        createScheduleButton.addActionListener(e -> new TimetableFrame(authService.getCurrentUser(), true).setVisible(true));
        panel.add(createScheduleButton);

        JButton viewTimetableButton = createStyledButton("Manage Exam Timetable");
        viewTimetableButton.addActionListener(e -> new TimetableFrame(authService.getCurrentUser(), false).setVisible(true));
        panel.add(viewTimetableButton);

        JButton conflictButton = createStyledButton("View Conflicts");
        conflictButton.addActionListener(e -> new ConflictManagementFrame(authService.getCurrentUser()).setVisible(true));
        panel.add(conflictButton);
    }

    private void addStudentButtons(JPanel panel) {
        logger.info("Adding STUDENT buttons");
        JButton viewTimetableButton = createStyledButton("View Timetable");
        viewTimetableButton.addActionListener(e -> new TimetableFrame(authService.getCurrentUser(), false).setVisible(true));
        panel.add(viewTimetableButton);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(7, 8, 9));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
}