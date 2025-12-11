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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class MainDashboardFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainDashboardFrame.class);
    private AuthenticationService authService;
    
    // Modern color scheme matching LoginFrame
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SECONDARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);

    public MainDashboardFrame() {
        authService = AuthenticationService.getInstance();
        initComponents();
        setTitle("TimeFlow - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setMinimumSize(new Dimension(700, 550));
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

        setLayout(new BorderLayout(0, 0));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getUsername());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);

        JPanel rightHeaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeaderPanel.setOpaque(false);
        
        JButton profileButton = createHeaderButton("About Me");
        profileButton.addActionListener(e -> new AboutFrame(this).setVisible(true));
        
        JButton logoutButton = createHeaderButton("Logout");
        logoutButton.addActionListener(e -> {
            logger.info("User logged out: {}", user.getUsername());
            authService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });

        rightHeaderPanel.add(profileButton);
        rightHeaderPanel.add(logoutButton);

        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(rightHeaderPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Role label
        JLabel roleLabel = new JLabel("Role: " + user.getRole().toString());
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        roleLabel.setForeground(TEXT_COLOR);
        roleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        buttonPanel.setOpaque(false);

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

        mainPanel.add(roleLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JButton createHeaderButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 255, 255, 30));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 32));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 255, 255, 50));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 255, 255, 30));
            }
        });
        
        return button;
    }

    private void addAdminButtons(JPanel panel) {
        logger.info("Adding ADMIN buttons");
        panel.add(createDashboardButton("Create Schedule", PRIMARY_COLOR, 
            e -> new TimetableFrame(authService.getCurrentUser(), true).setVisible(true)));
        panel.add(createDashboardButton("Manage Timetables", SECONDARY_COLOR, 
            e -> new TimetableFrame(authService.getCurrentUser(), false).setVisible(true)));
        panel.add(createDashboardButton("Manage Courses", SUCCESS_COLOR, 
            e -> new CourseManagementFrame(authService.getCurrentUser()).setVisible(true)));
        panel.add(createDashboardButton("Resolve Conflicts", WARNING_COLOR, 
            e -> new ConflictManagementFrame(authService.getCurrentUser()).setVisible(true)));
        panel.add(createDashboardButton("Approve Schedules", new Color(155, 89, 182), 
            e -> new ApprovalDashboardFrame(authService.getCurrentUser()).setVisible(true)));
    }

    private void addLecturerButtons(JPanel panel) {
        logger.info("Adding LECTURER buttons");
        panel.add(createDashboardButton("Create Schedule", PRIMARY_COLOR, 
            e -> new TimetableFrame(authService.getCurrentUser(), true).setVisible(true)));
        panel.add(createDashboardButton("View My Timetable", SECONDARY_COLOR, 
            e -> new TimetableFrame(authService.getCurrentUser(), false).setVisible(true)));
        panel.add(createDashboardButton("View Conflicts", WARNING_COLOR, 
            e -> new ConflictManagementFrame(authService.getCurrentUser()).setVisible(true)));
    }

    private void addExamsOfficerButtons(JPanel panel) {
        logger.info("Adding EXAMS_OFFICER buttons");
        panel.add(createDashboardButton("Create Schedule", PRIMARY_COLOR, 
            e -> new TimetableFrame(authService.getCurrentUser(), true).setVisible(true)));
        panel.add(createDashboardButton("Manage Exam Timetable", SECONDARY_COLOR, 
            e -> new TimetableFrame(authService.getCurrentUser(), false).setVisible(true)));
        panel.add(createDashboardButton("View Conflicts", WARNING_COLOR, 
            e -> new ConflictManagementFrame(authService.getCurrentUser()).setVisible(true)));
        panel.add(createDashboardButton("Approve Schedules", SUCCESS_COLOR, 
            e -> new ApprovalDashboardFrame(authService.getCurrentUser()).setVisible(true)));
    }

    private void addStudentButtons(JPanel panel) {
        logger.info("Adding STUDENT buttons");

        TimetableService timetableService = new TimetableService();
        ReportService reportService = new ReportService();
        NotificationService notificationService = new NotificationService();
        User currentUser = authService.getCurrentUser();

        panel.add(createDashboardButton("View Timetable", PRIMARY_COLOR, 
            e -> new TimetableFrame(currentUser, false).setVisible(true)));

        JButton printPdfButton = createDashboardButton("Print Timetable", SECONDARY_COLOR);
        printPdfButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                List<Timetable> timetables = timetableService.getStudentTimetables(currentUser);
                if (timetables.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, 
                        "You have no approved schedules to print.", 
                        "No Timetable", 
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                reportService.generateTimetablePdf(currentUser, timetables);
            });
        });
        panel.add(printPdfButton);

        JButton emailButton = createDashboardButton("Email My Timetable", SUCCESS_COLOR);
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
                            JOptionPane.showMessageDialog(panel, 
                                "Timetable has been sent to your email.", 
                                "Email Sent", 
                                JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(panel, 
                                "Could not send email: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), 
                                "Error", 
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            }
        });
        panel.add(emailButton);
    }

    private JButton createDashboardButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(250, 80));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private JButton createDashboardButton(String text, Color bgColor, java.awt.event.ActionListener listener) {
        JButton button = createDashboardButton(text, bgColor);
        button.addActionListener(listener);
        return button;
    }
}