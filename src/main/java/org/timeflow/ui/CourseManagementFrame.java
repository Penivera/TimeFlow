package org.timeflow.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.dao.CourseDAO;
import org.timeflow.dao.DepartmentDAO;
import org.timeflow.dao.UserDAO;
import org.timeflow.entity.Course;
import org.timeflow.entity.Department;
import org.timeflow.entity.User;
import org.timeflow.entity.UserRole;
import org.timeflow.service.AuthenticationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseManagementFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(CourseManagementFrame.class);
    private User user;
    private CourseDAO courseDAO;
    private UserDAO userDAO;
    private DepartmentDAO departmentDAO;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private static final String NO_LECTURERS = "No lecturers available";
    private static final String NO_DEPARTMENTS = "No departments available";

    public CourseManagementFrame(User user) {
        this.user = user;
        this.courseDAO = new CourseDAO();
        this.userDAO = new UserDAO();
        this.departmentDAO = new DepartmentDAO();
        initComponents();
        setTitle("TimeFlow - Course Management");
        setSize(800, 600);
        setMinimumSize(new Dimension(400, 400));
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Course Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(7, 8, 9));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Code", "Name", "Credits", "Level", "Department", "Lecturer"};
        tableModel = new DefaultTableModel(columns, 0);
        courseTable = new JTable(tableModel);
        courseTable.setRowHeight(30);
        courseTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(courseTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = createStyledButton("Add Course");
        addButton.addActionListener(e -> {
            if (AuthenticationService.getInstance().hasRole(UserRole.ADMIN)) {
                showAddCourseDialog();
            } else {
                logger.warn("Non-admin user {} attempted to add course", user.getUsername());
                JOptionPane.showMessageDialog(this, "Only admins can add courses", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(addButton);

        JButton refreshButton = createStyledButton("Refresh");
        refreshButton.addActionListener(e -> loadCourses());
        buttonPanel.add(refreshButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        loadCourses();
    }

    private void showAddCourseDialog() {
        JDialog dialog = new JDialog(this, "Add New Course", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Course Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(nameLabel, gbc);

        JTextField nameField = new JTextField(20);
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        dialog.add(nameField, gbc);

        JLabel codeLabel = new JLabel("Course Code:");
        codeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(codeLabel, gbc);

        JTextField codeField = new JTextField(10);
        codeField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        dialog.add(codeField, gbc);

        JLabel creditsLabel = new JLabel("Credits:");
        creditsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(creditsLabel, gbc);

        JSpinner creditsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));
        creditsSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        dialog.add(creditsSpinner, gbc);

        JLabel levelLabel = new JLabel("Academic Level:");
        levelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(levelLabel, gbc);

        JSpinner levelSpinner = new JSpinner(new SpinnerNumberModel(100, 100, 500, 100));
        levelSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 3;
        dialog.add(levelSpinner, gbc);

        JLabel lecturerLabel = new JLabel("Lecturer:");
        lecturerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(lecturerLabel, gbc);

        JComboBox<Object> lecturerComboBox = new JComboBox<>();
        lecturerComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        lecturerComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User user) {
                    setText(user.getUsername());
                } else {
                    setText(value.toString());
                }
                return this;
            }
        });
        List<User> lecturers = userDAO.findByRole(UserRole.LECTURER);
        if (lecturers.isEmpty()) {
            logger.warn("No lecturers found for course creation");
            lecturerComboBox.addItem(NO_LECTURERS);
            lecturerComboBox.setEnabled(false);
        } else {
            for (User lecturer : lecturers) {
                lecturerComboBox.addItem(lecturer);
            }
            lecturerComboBox.setEnabled(true);
        }
        gbc.gridx = 1;
        gbc.gridy = 4;
        dialog.add(lecturerComboBox, gbc);

        JLabel deptLabel = new JLabel("Department:");
        deptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 5;
        dialog.add(deptLabel, gbc);

        JComboBox<Object> deptComboBox = new JComboBox<>();
        deptComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        deptComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Department dept) {
                    setText(dept.getName() + " (" + dept.getCode() + ")");
                } else {
                    setText(value.toString());
                }
                return this;
            }
        });
        List<Department> departments = departmentDAO.findAll();
        if (departments.isEmpty()) {
            logger.warn("No departments found for course creation");
            deptComboBox.addItem(NO_DEPARTMENTS);
            deptComboBox.setEnabled(false);
        } else {
            for (Department dept : departments) {
                deptComboBox.addItem(dept);
            }
            deptComboBox.setEnabled(true);
        }
        gbc.gridx = 1;
        gbc.gridy = 5;
        dialog.add(deptComboBox, gbc);

        JButton saveButton = createStyledButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String code = codeField.getText().trim().toUpperCase();
                int credits = (Integer) creditsSpinner.getValue();
                int level = (Integer) levelSpinner.getValue();
                Object lecturerObj = lecturerComboBox.getSelectedItem();
                Object deptObj = deptComboBox.getSelectedItem();

                if (name.isEmpty() || code.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name and code are required", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (lecturerObj instanceof String && lecturerObj.equals(NO_LECTURERS)) {
                    JOptionPane.showMessageDialog(dialog, "No valid lecturer selected", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (deptObj instanceof String && deptObj.equals(NO_DEPARTMENTS)) {
                    JOptionPane.showMessageDialog(dialog, "No valid department selected", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                User lecturer = (User) lecturerObj;
                Department dept = (Department) deptObj;
                Course course = new Course(name, code, credits, dept, lecturer, level);
                courseDAO.save(course);
                logger.info("Course added: {} ({}) by admin {}", name, code, user.getUsername());
                JOptionPane.showMessageDialog(dialog, "Course added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCourses();
                dialog.dispose();
            } catch (Exception ex) {
                logger.error("Failed to add course: {}", ex.getMessage(), ex);
                JOptionPane.showMessageDialog(dialog, "Failed to add course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        dialog.add(saveButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

// In CourseManagementFrame.java

    private void loadCourses() {
        tableModel.setRowCount(0);
        List<Course> courses;

        // Check if the current user is an Admin
        if (AuthenticationService.getInstance().hasRole(UserRole.ADMIN)) {
            logger.info("Admin user detected, loading all courses.");
            courses = courseDAO.findAll(); // Fetch all courses for the admin
        } else {
            // Original logic for other users with a department
            Department userDept = user.getDepartment();
            if (userDept == null) {
                logger.warn("User {} has no department, cannot load courses.", user.getUsername());
                JOptionPane.showMessageDialog(this, "Your user profile does not have a department assigned.", "Error", JOptionPane.ERROR_MESSAGE);
                return; // Exit if the user has no department
            }
            logger.info("Loading courses for department: {}", userDept.getName());
            courses = courseDAO.findByDepartment(userDept);
        }

        for (Course c : courses) {
            tableModel.addRow(new Object[]{
                    c.getCode(),
                    c.getName(),
                    c.getCredits(),
                    c.getLevel(),
                    c.getDepartment().getName(),
                    c.getLecturer() != null ? c.getLecturer().getUsername() : "N/A"
            });
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(7, 8, 9));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
}