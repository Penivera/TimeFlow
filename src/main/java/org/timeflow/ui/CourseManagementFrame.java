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
    private final User user;
    private final CourseDAO courseDAO;
    private final UserDAO userDAO;
    private final DepartmentDAO departmentDAO;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    // --- NEW: Buttons for edit and delete ---
    private JButton editButton;
    private JButton deleteButton;
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
        setMinimumSize(new Dimension(600, 400));
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

        String[] columns = {"ID", "Code", "Name", "Credits", "Level", "Department", "Lecturer"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells not editable
            }
        };
        courseTable = new JTable(tableModel);
        courseTable.setRowHeight(30);
        courseTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // --- NEW: Hide the ID column from view, but keep the data ---
        courseTable.getColumnModel().getColumn(0).setMinWidth(0);
        courseTable.getColumnModel().getColumn(0).setMaxWidth(0);
        courseTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(courseTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = createStyledButton("Add Course");
        addButton.addActionListener(e -> showAddOrEditDialog(null)); // Pass null for adding
        buttonPanel.add(addButton);

        // --- NEW: Initialize Edit and Delete buttons ---
        editButton = createStyledButton("Edit Selected");
        editButton.setEnabled(false); // Disabled by default
        editButton.addActionListener(e -> handleEditCourse());
        buttonPanel.add(editButton);

        deleteButton = createStyledButton("Delete Selected");
        deleteButton.setEnabled(false); // Disabled by default
        deleteButton.addActionListener(e -> handleDeleteCourse());
        buttonPanel.add(deleteButton);
        // --- End of new buttons ---

        JButton refreshButton = createStyledButton("Refresh");
        refreshButton.addActionListener(e -> loadCourses());
        buttonPanel.add(refreshButton);

        // --- NEW: Add a listener to the table selection ---
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean isRowSelected = courseTable.getSelectedRow() != -1;
                editButton.setEnabled(isRowSelected);
                deleteButton.setEnabled(isRowSelected);
            }
        });
        // --- End of new listener ---

        // Admins and Exams Officers can manage courses
        if (!AuthenticationService.getInstance().hasRole(UserRole.ADMIN) && !AuthenticationService.getInstance().hasRole(UserRole.EXAMS_OFFICER)) {
            addButton.setEnabled(false);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        loadCourses();
    }

    // --- MODIFIED: This dialog now handles both Add and Edit ---
    // In CourseManagementFrame.java, replace the existing showAddOrEditDialog method

    private void showAddOrEditDialog(Course courseToEdit) {
        boolean isEditMode = courseToEdit != null;
        String dialogTitle = isEditMode ? "Edit Course" : "Add New Course";
        JDialog dialog = new JDialog(this, dialogTitle, true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- UI Fields ---
        JTextField nameField = new JTextField(20);
        if (isEditMode) nameField.setText(courseToEdit.getName());

        JTextField codeField = new JTextField(10);
        if (isEditMode) codeField.setText(courseToEdit.getCode());

        JSpinner creditsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));
        if (isEditMode) creditsSpinner.setValue(courseToEdit.getCredits());

        JSpinner levelSpinner = new JSpinner(new SpinnerNumberModel(100, 100, 500, 100));
        if (isEditMode) levelSpinner.setValue(courseToEdit.getLevel());

        // --- Lecturer ComboBox Setup ---
        JComboBox<Object> lecturerComboBox = new JComboBox<>();
        // --- NEW: Add a custom renderer for the User object ---
        lecturerComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User user) {
                    setText(user.getUsername());
                } else if (value != null) {
                    setText(value.toString());
                }
                return this;
            }
        });
        List<User> lecturers = userDAO.findByRole(UserRole.LECTURER);
        if (lecturers.isEmpty()) {
            lecturerComboBox.addItem(NO_LECTURERS);
            lecturerComboBox.setEnabled(false);
        } else {
            lecturers.forEach(lecturerComboBox::addItem);
            if (isEditMode && courseToEdit.getLecturer() != null) {
                lecturerComboBox.setSelectedItem(courseToEdit.getLecturer());
            }
        }

        // --- Department ComboBox Setup ---
        JComboBox<Object> deptComboBox = new JComboBox<>();
        // --- NEW: Add a custom renderer for the Department object ---
        deptComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Department dept) {
                    setText(dept.getName());
                } else if (value != null) {
                    setText(value.toString());
                }
                return this;
            }
        });
        List<Department> departments = departmentDAO.findAll();
        if (departments.isEmpty()) {
            deptComboBox.addItem(NO_DEPARTMENTS);
            deptComboBox.setEnabled(false);
        } else {
            departments.forEach(deptComboBox::addItem);
            if (isEditMode && courseToEdit.getDepartment() != null) {
                deptComboBox.setSelectedItem(courseToEdit.getDepartment());
            }
        }

        // --- Dialog Layout and Save Button ---
        setupDialogComponents(dialog, gbc, nameField, codeField, creditsSpinner, levelSpinner, lecturerComboBox, deptComboBox);

        JButton saveButton = createStyledButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String code = codeField.getText().trim().toUpperCase();
                if (name.isEmpty() || code.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name and code are required", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Course course = isEditMode ? courseToEdit : new Course();
                course.setName(name);
                course.setCode(code);
                course.setCredits((Integer) creditsSpinner.getValue());
                course.setLevel((Integer) levelSpinner.getValue());

                // Handle potential placeholder strings
                Object lecturerObj = lecturerComboBox.getSelectedItem();
                if (lecturerObj instanceof User) {
                    course.setLecturer((User) lecturerObj);
                }
                Object deptObj = deptComboBox.getSelectedItem();
                if (deptObj instanceof Department) {
                    course.setDepartment((Department) deptObj);
                }

                if (isEditMode) {
                    courseDAO.update(course);
                    logger.info("Course updated: {} ({}) by user {}", name, code, user.getUsername());
                } else {
                    courseDAO.save(course);
                    logger.info("Course added: {} ({}) by user {}", name, code, user.getUsername());
                }
                JOptionPane.showMessageDialog(dialog, "Course saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCourses();
                dialog.dispose();
            } catch (Exception ex) {
                logger.error("Failed to save course: {}", ex.getMessage(), ex);
                JOptionPane.showMessageDialog(dialog, "Failed to save course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    // Helper to keep initComponents clean
    private void setupDialogComponents(JDialog dialog, GridBagConstraints gbc, Component... components) {
        String[] labels = {"Course Name:", "Course Code:", "Credits:", "Academic Level:", "Lecturer:", "Department:"};
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = i;
            dialog.add(label, gbc);
            gbc.gridx = 1;
            dialog.add(components[i], gbc);
        }
    }


    // --- NEW: Handler for the Edit button action ---
    private void handleEditCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Retrieve the ID from our hidden first column
        Long courseId = (Long) tableModel.getValueAt(selectedRow, 0);
        Course courseToEdit = courseDAO.findById(courseId);
        if (courseToEdit != null) {
            showAddOrEditDialog(courseToEdit);
        } else {
            JOptionPane.showMessageDialog(this, "Could not find the selected course in the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- NEW: Handler for the Delete button action ---
    private void handleDeleteCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long courseId = (Long) tableModel.getValueAt(selectedRow, 0);
        String courseName = (String) tableModel.getValueAt(selectedRow, 2);

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the course: " + courseName + "?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                courseDAO.deleteById(courseId);
                logger.info("Course with ID {} deleted by user {}", courseId, user.getUsername());
                loadCourses(); // Refresh the table
            } catch (Exception e) {
                logger.error("Failed to delete course with ID {}: {}", courseId, e.getMessage(), e);
                JOptionPane.showMessageDialog(this, "Failed to delete course. It may be in use in a timetable.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        List<Course> courses;

        if (AuthenticationService.getInstance().hasRole(UserRole.ADMIN)) {
            logger.info("Admin user detected, loading all courses.");
            courses = courseDAO.findAll();
        } else {
            Department userDept = user.getDepartment();
            if (userDept == null) {
                logger.warn("User {} has no department, cannot load courses.", user.getUsername());
                return;
            }
            logger.info("Loading courses for department: {}", userDept.getName());
            courses = courseDAO.findByDepartment(userDept);
        }

        for (Course c : courses) {
            tableModel.addRow(new Object[]{
                    c.getId(), // Add the ID here
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