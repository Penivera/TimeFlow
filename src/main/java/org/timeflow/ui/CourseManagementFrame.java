package org.timeflow.ui;

import org.timeflow.dao.CourseDAO;
import org.timeflow.entity.Course;
import org.timeflow.entity.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseManagementFrame extends JFrame {
    private User user;
    private CourseDAO courseDAO;
    private JTable courseTable;
    private DefaultTableModel tableModel;

    public CourseManagementFrame(User user) {
        this.user = user;
        this.courseDAO = new CourseDAO();
        initComponents();
        setTitle("TimeFlow - Course Management");
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Course Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Code", "Name", "Credits", "Level", "Department"};
        tableModel = new DefaultTableModel(columns, 0);
        courseTable = new JTable(tableModel);
        courseTable.setRowHeight(30);
        courseTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(courseTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        JButton addButton = createStyledButton("Add Course");
        addButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Add course feature coming soon!"));
        buttonPanel.add(addButton);

        JButton refreshButton = createStyledButton("Refresh");
        refreshButton.addActionListener(e -> loadCourses());
        buttonPanel.add(refreshButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        loadCourses();
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        List<Course> courses = courseDAO.findByDepartment(user.getDepartment());
        for (Course c : courses) {
            tableModel.addRow(new Object[]{
                    c.getCode(),
                    c.getName(),
                    c.getCredits(),
                    c.getLevel(),
                    c.getDepartment().getName()
            });
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(0, 102, 204));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
}