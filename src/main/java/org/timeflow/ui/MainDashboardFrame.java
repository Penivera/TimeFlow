package org.timeflow.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.entity.Timetable;
import org.timeflow.service.AuthenticationService;
import org.timeflow.entity.User;
import org.timeflow.entity.UserRole;
import org.timeflow.service.NotificationService;
import org.timeflow.service.ReportService;
import org.timeflow.service.TimetableService;

import javax.swing.*;
import java.awt.*;
import java.util.List; // <-- FIX: Added the missing import

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

        JButton profileButton = new JButton("About Me");
        profileButton.setForeground(new Color(7, 8, 9));
        profileButton.setHorizontalAlignment(SwingConstants.LEFT);
        profileButton.addActionListener(e -> new AboutFrame(this).setVisible(true));
        headerPanel.add(profileButton);

        JButton logoutButton = createStyledButton("Logout");
        logoutButton.addActionListener(e -> {
            logger.info("User logged out: {}", user.getUsername());
            authService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
        headerPanel.add(logoutButton, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 20, 20)); // Adjusted grid layout for more buttons
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
        panel.add(createStyledButton("Create Schedule", e -> new TimetableFrame(authService.getCurrentUser(), true).setVisible(true)));
        panel.add(createStyledButton("Manage Timetables", e -> new TimetableFrame(authService.getCurrentUser(), false).setVisible(true)));
        panel.add(createStyledButton("Manage Courses", e -> new CourseManagementFrame(authService.getCurrentUser()).setVisible(true)));
        panel.add(createStyledButton("Resolve Conflicts", e -> new ConflictManagementFrame(authService.getCurrentUser()).setVisible(true)));
        panel.add(createStyledButton("Approve Schedules", e -> new ApprovalDashboardFrame(authService.getCurrentUser()).setVisible(true)));
    }

    private void addLecturerButtons(JPanel panel) {
        logger.info("Adding LECTURER buttons");
        panel.add(createStyledButton("Create Schedule", e -> new TimetableFrame(authService.getCurrentUser(), true).setVisible(true)));
        panel.add(createStyledButton("View My Timetable", e -> new TimetableFrame(authService.getCurrentUser(), false).setVisible(true)));
        panel.add(createStyledButton("View Conflicts", e -> new ConflictManagementFrame(authService.getCurrentUser()).setVisible(true)));
    }

    private void addExamsOfficerButtons(JPanel panel) {
        logger.info("Adding EXAMS_OFFICER buttons");
        panel.add(createStyledButton("Create Schedule", e -> new TimetableFrame(authService.getCurrentUser(), true).setVisible(true)));
        panel.add(createStyledButton("Manage Exam Timetable", e -> new TimetableFrame(authService.getCurrentUser(), false).setVisible(true)));
        panel.add(createStyledButton("View Conflicts", e -> new ConflictManagementFrame(authService.getCurrentUser()).setVisible(true)));
        panel.add(createStyledButton("Approve Schedules", e -> new ApprovalDashboardFrame(authService.getCurrentUser()).setVisible(true)));
    }

    // --- FIX: This is the single, correct version of the method ---
    private void addStudentButtons(JPanel panel) {
        logger.info("Adding STUDENT buttons");

        TimetableService timetableService = new TimetableService();
        ReportService reportService = new ReportService();
        NotificationService notificationService = new NotificationService();
        User currentUser = authService.getCurrentUser();

        panel.add(createStyledButton("View Timetable", e -> new TimetableFrame(currentUser, false).setVisible(true)));

        JButton printPdfButton = createStyledButton("Print Timetable to PDF");
        printPdfButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                List<Timetable> timetables = timetableService.getStudentTimetables(currentUser);
                if (timetables.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "You have no approved schedules to print.", "No Timetable", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                reportService.generateTimetablePdf(currentUser, timetables);
            });
        });
        panel.add(printPdfButton);

        JButton emailButton = createStyledButton("Email My Timetable");
        emailButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    panel,
                    "This will send the timetable to your registered email: " + currentUser.getEmail() + "\nDo you want to continue?",
                    "Confirm Email",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        List<Timetable> timetables = timetableService.getStudentTimetables(currentUser);
                        if (timetables.isEmpty()) {
                            throw new IllegalStateException("You have no approved schedules to email.");
                        }
                        notificationService.sendTimetableToStudent(currentUser, timetables);
                        return null;
                    }

                    @Override
                    protected void done() {
                        panel.setCursor(Cursor.getDefaultCursor());
                        try {
                            get();
                            JOptionPane.showMessageDialog(panel, "Timetable has been sent to your email.", "Email Sent", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(panel, "Could not send email: " + ex.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            }
        });
        panel.add(emailButton);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(7, 8, 9));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    // Overloaded helper for cleaner code
    private JButton createStyledButton(String text, java.awt.event.ActionListener listener) {
        JButton button = createStyledButton(text);
        button.addActionListener(listener);
        return button;
    }
}