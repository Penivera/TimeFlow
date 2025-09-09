package org.timeflow.ui;

import com.formdev.flatlaf.FlatLightLaf;
import org.timeflow.dao.DepartmentDAO;
import org.timeflow.dao.UserDAO;
import org.timeflow.entity.Department;
import org.timeflow.entity.User;
import org.timeflow.entity.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SignupFrame extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(SignupFrame.class);
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<UserRole> roleComboBox;
    private JComboBox<Department> departmentComboBox;
    private JComboBox<Department> nonStudentDeptComboBox;
    private JSpinner levelSpinner;
    private JTextField deptNameField;
    private JTextField deptCodeField;
    private JTextField deptHeadField;
    private JRadioButton selectExistingDeptRadio;
    private JRadioButton createNewDeptRadio;
    private UserDAO userDAO;
    private DepartmentDAO departmentDAO;
    private CardLayout cardLayout;
    private JPanel departmentPanel;

    public SignupFrame(JFrame parent) {
        super(parent, true);
        userDAO = new UserDAO();
        departmentDAO = new DepartmentDAO();
        setupLookAndFeel();
        initComponents();
        setTitle("TimeFlow - Sign Up");
        setSize(450, 650);
        setMinimumSize(new Dimension(400, 600));
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            logger.error("Failed to set FlatLaf look-and-feel", e);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- ALL COMPONENTS ARE INITIALIZED HERE FIRST ---
        // Title
        JLabel titleLabel = new JLabel("Create Your Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        gbc.insets = new Insets(10, 5, 5, 5); // Reset insets for other components

        // Username
        gbc.gridy++; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy++;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        mainPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy++;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // Role
        gbc.gridx = 0; gbc.gridy++;
        mainPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        roleComboBox = new JComboBox<>(new UserRole[]{UserRole.STUDENT, UserRole.LECTURER, UserRole.EXAMS_OFFICER, UserRole.ADMIN});
        mainPanel.add(roleComboBox, gbc);

        // --- Dynamic Department Panel ---
        cardLayout = new CardLayout();
        departmentPanel = new JPanel(cardLayout);
        departmentPanel.setOpaque(false);

        JPanel studentPanel = createStudentPanel();
        departmentPanel.add(studentPanel, "STUDENT_PANEL");

        JPanel otherRolesPanel = createOtherRolesPanel();
        departmentPanel.add(otherRolesPanel, "OTHER_PANEL");

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        mainPanel.add(departmentPanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        JButton signupButton = new JButton("Sign Up");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(signupButton);
        buttonPanel.add(cancelButton);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // --- LISTENERS ARE ADDED AFTER INITIALIZATION ---
        roleComboBox.addActionListener(e -> toggleDepartmentView());
        signupButton.addActionListener(e -> handleSignup());
        cancelButton.addActionListener(e -> dispose());
        selectExistingDeptRadio.addActionListener(e -> toggleNewDeptFields(false));
        createNewDeptRadio.addActionListener(e -> toggleNewDeptFields(true));

        loadDepartments();
        toggleDepartmentView(); // Set initial view
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        departmentComboBox = new JComboBox<>();
        panel.add(departmentComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Academic Level:"), gbc);
        gbc.gridx = 1;
        levelSpinner = new JSpinner(new SpinnerNumberModel(100, 100, 500, 100));
        panel.add(levelSpinner, gbc);

        return panel;
    }

    private JPanel createOtherRolesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        selectExistingDeptRadio = new JRadioButton("Select Existing Department", true);
        createNewDeptRadio = new JRadioButton("Create New Department");
        ButtonGroup group = new ButtonGroup();
        group.add(selectExistingDeptRadio);
        group.add(createNewDeptRadio);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(selectExistingDeptRadio, gbc);
        gbc.gridy++;
        panel.add(createNewDeptRadio, gbc);

        gbc.gridy++; gbc.gridwidth = 1;
        panel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        nonStudentDeptComboBox = new JComboBox<>();
        panel.add(nonStudentDeptComboBox, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("New Dept. Name:"), gbc);
        gbc.gridx = 1;
        deptNameField = new JTextField(20);
        panel.add(deptNameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("New Dept. Code:"), gbc);
        gbc.gridx = 1;
        deptCodeField = new JTextField(20);
        panel.add(deptCodeField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("New H.O.D:"), gbc);
        gbc.gridx = 1;
        deptHeadField = new JTextField(20);
        panel.add(deptHeadField, gbc);

        return panel;
    }

    private void toggleDepartmentView() {
        UserRole selectedRole = (UserRole) roleComboBox.getSelectedItem();
        if (selectedRole == UserRole.STUDENT) {
            cardLayout.show(departmentPanel, "STUDENT_PANEL");
        } else {
            cardLayout.show(departmentPanel, "OTHER_PANEL");
            toggleNewDeptFields(createNewDeptRadio.isSelected());
        }
    }

    private void toggleNewDeptFields(boolean isCreatingNew) {
        nonStudentDeptComboBox.setEnabled(!isCreatingNew);
        deptNameField.setEditable(isCreatingNew);
        deptCodeField.setEditable(isCreatingNew);
        deptHeadField.setEditable(isCreatingNew);
    }

    private void loadDepartments() {
        try {
            List<Department> departments = departmentDAO.findAll();
            departmentComboBox.removeAllItems();
            nonStudentDeptComboBox.removeAllItems();

            if (departments == null || departments.isEmpty()) {
                departmentComboBox.setEnabled(false);
                nonStudentDeptComboBox.setEnabled(false);
                selectExistingDeptRadio.setEnabled(false);
                createNewDeptRadio.setSelected(true);
            } else {
                departmentComboBox.setEnabled(true);
                nonStudentDeptComboBox.setEnabled(true);
                selectExistingDeptRadio.setEnabled(true);
                DefaultComboBoxModel<Department> studentModel = new DefaultComboBoxModel<>();
                DefaultComboBoxModel<Department> otherModel = new DefaultComboBoxModel<>();
                for (Department dept : departments) {
                    studentModel.addElement(dept);
                    otherModel.addElement(dept);
                }
                departmentComboBox.setModel(studentModel);
                nonStudentDeptComboBox.setModel(otherModel);
            }
        } catch (Exception e) {
            logger.error("Error loading departments", e);
            JOptionPane.showMessageDialog(this, "Could not load departments.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        UserRole role = (UserRole) roleComboBox.getSelectedItem();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Signup Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Department department = null;
        if (role == UserRole.STUDENT) {
            department = (Department) departmentComboBox.getSelectedItem();
        } else { // For Admin, Lecturer, Exams Officer
            if (selectExistingDeptRadio.isSelected()) {
                department = (Department) nonStudentDeptComboBox.getSelectedItem();
            } else {
                String deptName = deptNameField.getText().trim();
                String deptCode = deptCodeField.getText().trim();
                String deptHead = deptHeadField.getText().trim();
                if (deptName.isEmpty() || deptCode.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Department name and code are required for a new department.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                department = new Department(deptName, deptCode, deptHead);
                departmentDAO.save(department); // Save the new department
                loadDepartments(); // Refresh the list of departments
            }
        }

        if (department == null && role != UserRole.ADMIN) {
            JOptionPane.showMessageDialog(this, "A department must be selected or created for this role.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setDepartment(department);
        user.setActive(true);

        if (role == UserRole.STUDENT) {
            user.setLevel((Integer) levelSpinner.getValue());
        }

        try {
            userDAO.save(user);
            JOptionPane.showMessageDialog(this, "Signup successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            logger.error("Signup failed", e);
            JOptionPane.showMessageDialog(this, "Signup failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}