package org.timeflow.ui;

import org.timeflow.entity.User;
import org.timeflow.entity.UserRole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainDashboardFrame extends JFrame {
    private User user;

    public MainDashboardFrame(User user) {
        this.user = user;
        initComponents();
        setTitle("TimeFlow - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getUsername());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(0, 102, 204));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonPanel.setBackground(Color.WHITE);

        if (user.getRole() == UserRole.ADMIN) {
            addAdminButtons(buttonPanel);
        } else if (user.getRole() == UserRole.LECTURER) {
            addLecturerButtons(buttonPanel);
        } else if (user.getRole() == UserRole.STUDENT) {
            addStudentButtons(buttonPanel);
        }

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void addAdminButtons(JPanel panel) {
        JButton timetableButton = createStyledButton("Manage Timetables");
        timetableButton.addActionListener(e -> new TimetableFrame(user).setVisible(true));
        panel.add(timetableButton);

        JButton courseButton = createStyledButton("Manage Courses");
        courseButton.addActionListener(e -> new CourseManagementFrame(user).setVisible(true));
        panel.add(courseButton);

        JButton conflictButton = createStyledButton("Resolve Conflicts");
        conflictButton.addActionListener(e -> new ConflictManagementFrame(user).setVisible(true));
        panel.add(conflictButton);

        JButton reportsButton = createStyledButton("View Reports");
        reportsButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Reports feature coming soon!"));
        panel.add(reportsButton);
    }

    private void addLecturerButtons(JPanel panel) {
        JButton viewTimetableButton = createStyledButton("View My Timetable");
        viewTimetableButton.addActionListener(e -> new TimetableFrame(user).setVisible(true));
        panel.add(viewTimetableButton);

        JButton conflictButton = createStyledButton("View Conflicts");
        conflictButton.addActionListener(e -> new ConflictManagementFrame(user).setVisible(true));
        panel.add(conflictButton);
    }

    private void addStudentButtons(JPanel panel) {
        JButton viewTimetableButton = createStyledButton("View Timetable");
        viewTimetableButton.addActionListener(e -> new TimetableFrame(user).setVisible(true));
        panel.add(viewTimetableButton);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(0, 102, 204));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
}