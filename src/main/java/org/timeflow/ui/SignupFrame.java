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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class SignupFrame extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(SignupFrame.class);
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<UserRole> roleComboBox;
    private JComboBox<Department> departmentComboBox;
    private JTextField deptNameField;
    private JTextField deptCodeField;
    private JTextField deptHeadField;
    private JButton signupButton;
    private JButton cancelButton;
    private UserDAO userDAO;
    private DepartmentDAO departmentDAO;
    private JPanel departmentPanel;
    private CardLayout cardLayout;

    public SignupFrame(JFrame parent) {
        super(parent, true);
        userDAO = new UserDAO();
        departmentDAO = new DepartmentDAO();
        setupLookAndFeel();
        initComponents();
        setTitle("TimeFlow - Sign Up");
        setSize(400, 550);
        setMinimumSize(new Dimension(350, 450));
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 1),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            UIManager.put("PasswordField.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 1),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            UIManager.put("ComboBox.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 1),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            UIManager.put("Button.background", Color.BLACK);
            UIManager.put("Button.foreground", Color.WHITE);
        } catch (Exception e) {
            logger.error("Failed to set FlatLaf look-and-feel: {}", e.getMessage(), e);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                logger.error("Failed to set system look-and-feel: {}", ex.getMessage(), ex);
            }
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("TimeFlow Sign Up");
        titleLabel.setFont(getPreferredFont("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        usernameLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        usernameField.setForeground(Color.BLACK);
        usernameField.setBackground(new Color(240, 240, 240));
        usernameField.setOpaque(true);
        usernameField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(usernameField, gbc);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        emailLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        emailField.setForeground(Color.BLACK);
        emailField.setBackground(new Color(240, 240, 240));
        emailField.setOpaque(true);
        emailField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        passwordField.setForeground(Color.BLACK);
        passwordField.setBackground(new Color(240, 240, 240));
        passwordField.setOpaque(true);
        passwordField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(passwordField, gbc);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(roleLabel, gbc);

        roleComboBox = new JComboBox<>(new UserRole[]{UserRole.STUDENT, UserRole.LECTURER, UserRole.ADMIN, UserRole.EXAMS_OFFICER});
        roleComboBox.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        roleComboBox.setBackground(new Color(240, 240, 240));
        roleComboBox.setForeground(Color.BLACK);
        roleComboBox.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 4;
        mainPanel.add(roleComboBox, gbc);

        // Department panel with CardLayout
        departmentPanel = new JPanel();
        cardLayout = new CardLayout();
        departmentPanel.setLayout(cardLayout);
        departmentPanel.setBackground(Color.WHITE);
        departmentPanel.setPreferredSize(new Dimension(300, 100)); // Ensure panel has sufficient size

        // Dropdown panel for STUDENT
        JPanel dropdownPanel = new JPanel(new GridBagLayout());
        dropdownPanel.setBackground(Color.WHITE);
        dropdownPanel.setPreferredSize(new Dimension(300, 50)); // Ensure panel sizes correctly
        JLabel departmentLabel = new JLabel("Department:");
        departmentLabel.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        departmentLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        dropdownPanel.add(departmentLabel, gbc);

        departmentComboBox = new JComboBox<Department>();
        departmentComboBox.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        departmentComboBox.setBackground(new Color(240, 240, 240));
        departmentComboBox.setForeground(Color.BLACK);
        departmentComboBox.setPreferredSize(new Dimension(200, 30));
        loadDepartments();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dropdownPanel.add(departmentComboBox, gbc);

        // Text fields panel for non-STUDENT
        JPanel textFieldsPanel = new JPanel(new GridBagLayout());
        textFieldsPanel.setBackground(Color.WHITE);
        textFieldsPanel.setPreferredSize(new Dimension(300, 100)); // Ensure panel sizes correctly

        JLabel deptNameLabel = new JLabel("Department Name:");
        deptNameLabel.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        deptNameLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        textFieldsPanel.add(deptNameLabel, gbc);

        deptNameField = new JTextField(20);
        deptNameField.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        deptNameField.setForeground(Color.BLACK);
        deptNameField.setBackground(new Color(240, 240, 240));
        deptNameField.setOpaque(true);
        deptNameField.setPreferredSize(new Dimension(200, 30));
        deptNameField.setEditable(true); // Ensure editable
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        textFieldsPanel.add(deptNameField, gbc);

        JLabel deptCodeLabel = new JLabel("Department Code:");
        deptCodeLabel.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        deptCodeLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        textFieldsPanel.add(deptCodeLabel, gbc);

        deptCodeField = new JTextField(20);
        deptCodeField.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        deptCodeField.setForeground(Color.BLACK);
        deptCodeField.setBackground(new Color(240, 240, 240));
        deptCodeField.setOpaque(true);
        deptCodeField.setPreferredSize(new Dimension(200, 30));
        deptCodeField.setEditable(true); // Ensure editable
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        textFieldsPanel.add(deptCodeField, gbc);

        JLabel deptHeadLabel = new JLabel("Head of Department:");
        deptHeadLabel.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        deptHeadLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 2;
        textFieldsPanel.add(deptHeadLabel, gbc);

        deptHeadField = new JTextField(20);
        deptHeadField.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        deptHeadField.setForeground(Color.BLACK);
        deptHeadField.setBackground(new Color(240, 240, 240));
        deptHeadField.setOpaque(true);
        deptHeadField.setPreferredSize(new Dimension(200, 30));
        deptHeadField.setEditable(true); // Ensure editable
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        textFieldsPanel.add(deptHeadField, gbc);

        departmentPanel.add(dropdownPanel, "DROPDOWN");
        departmentPanel.add(textFieldsPanel, "TEXT_FIELDS");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(departmentPanel, gbc);

        // Switch panel based on role
        roleComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserRole selectedRole = (UserRole) roleComboBox.getSelectedItem();
                logger.info("Role selected: {}", selectedRole);
                if (selectedRole == UserRole.STUDENT) {
                    cardLayout.show(departmentPanel, "DROPDOWN");
                } else {
                    cardLayout.show(departmentPanel, "TEXT_FIELDS");
                }
                departmentPanel.revalidate();
                departmentPanel.repaint();
            }
        });

        // Initialize with STUDENT view
        cardLayout.show(departmentPanel, "DROPDOWN");
        departmentPanel.revalidate();
        departmentPanel.repaint();

        signupButton = new JButton("Sign Up");
        signupButton.setFont(getPreferredFont("Segoe UI", Font.BOLD, 14));
        signupButton.setBackground(Color.BLACK);
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
        addButtonHoverEffect(signupButton);
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSignup();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(signupButton, gbc);

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(getPreferredFont("Segoe UI", Font.BOLD, 14));
        cancelButton.setBackground(Color.BLACK);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        addButtonHoverEffect(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(cancelButton, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void addButtonHoverEffect(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(51, 51, 51));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.BLACK);
            }
        });
    }

    private Font getPreferredFont(String fontName, int style, int size) {
        Font font = new Font(fontName, style, size);
        return font.getFamily().equals(fontName) ? font : new Font("Arial", style, size);
    }

    private void loadDepartments() {
        try {
            List<Department> departments = departmentDAO.findAll();
            logger.info("Loaded {} departments", departments.size());
            departmentComboBox.removeAllItems();
            if (departments.isEmpty()) {
                departmentComboBox.addItem(new Department("No departments available", "", null));
                departmentComboBox.setEnabled(false);
            } else {
                for (Department dept : departments) {
                    departmentComboBox.addItem(dept);
                }
                departmentComboBox.setEnabled(true);
            }
            departmentComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Department) {
                        setText(((Department) value).getName());
                    }
                    return this;
                }
            });
        } catch (Exception e) {
            logger.error("Error loading departments: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Failed to load departments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        UserRole role = (UserRole) roleComboBox.getSelectedItem();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields", "Signup Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userDAO.findByUsername(username) != null) {
            JOptionPane.showMessageDialog(this, "Username already exists", "Signup Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userDAO.findByEmail(email) != null) {
            JOptionPane.showMessageDialog(this, "Email already exists", "Signup Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Department department;
        if (role == UserRole.STUDENT) {
            department = (Department) departmentComboBox.getSelectedItem();
            if (department == null || department.getName().equals("No departments available")) {
                JOptionPane.showMessageDialog(this, "Please select a valid department", "Signup Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            String deptName = deptNameField.getText().trim();
            String deptCode = deptCodeField.getText().trim();
            String deptHead = deptHeadField.getText().trim();

            if (deptName.isEmpty() || deptCode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Department name and code are required", "Signup Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!deptCode.matches("^[A-Za-z0-9]+$")) {
                JOptionPane.showMessageDialog(this, "Department code must be alphanumeric", "Signup Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (departmentDAO.findByName(deptName) != null) {
                JOptionPane.showMessageDialog(this, "Department name already exists", "Signup Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (departmentDAO.findByCode(deptCode) != null) {
                JOptionPane.showMessageDialog(this, "Department code already exists", "Signup Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            department = new Department(deptName, deptCode, deptHead.isEmpty() ? null : deptHead);
            try {
                departmentDAO.save(department);
                logger.info("Department created: {}", deptName);
                loadDepartments(); // Refresh dropdown
            } catch (Exception e) {
                logger.error("Failed to save department: {}", e.getMessage(), e);
                JOptionPane.showMessageDialog(this, "Failed to save department: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setDepartment(department);
        user.setActive(true);

        try {
            userDAO.save(user);
            logger.info("User created: {}", username);
            JOptionPane.showMessageDialog(this, "Signup successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            logger.error("Signup failed: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Signup failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}