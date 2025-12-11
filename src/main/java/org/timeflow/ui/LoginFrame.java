package org.timeflow.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(LoginFrame.class);
    private JTextField usernameField;
    private JPasswordField passwordField;
    private AuthenticationService authService;
    
    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SECONDARY_COLOR = new Color(41, 128, 185);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);

    public LoginFrame() {
        authService = AuthenticationService.getInstance();
        initComponents();
        setTitle("TimeFlow - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 400);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Main content panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("TimeFlow");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Timetable Management System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 20, 10);
        mainPanel.add(subtitleLabel, gbc);

        // Reset insets and gridwidth
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridwidth = 1;

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(200, 32));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(usernameField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 32));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(passwordField, gbc);

        // Add Enter key listener to password field
        passwordField.addActionListener(e -> handleLogin());

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setOpaque(false);
        
        JButton loginButton = createStyledButton("Login", PRIMARY_COLOR);
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton);

        JButton signupButton = createStyledButton("Create Account", SECONDARY_COLOR);
        signupButton.addActionListener(e -> new SignupFrame(LoginFrame.this).setVisible(true));
        buttonPanel.add(signupButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 8, 10);
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(BACKGROUND_COLOR);
        footerPanel.setBorder(new EmptyBorder(8, 10, 8, 10));
        JLabel footerLabel = new JLabel("© 2025 TimeFlow - All Rights Reserved");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(TEXT_COLOR);
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
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

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both username and password.", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        logger.info("Login attempt: username={}", username);
        
        // Show loading cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        if (authService.login(username, password)) {
            logger.info("Login successful: username={}, role={}", username, authService.getCurrentUser().getRole());
            setCursor(Cursor.getDefaultCursor());
            dispose();
            new MainDashboardFrame().setVisible(true);
        } else {
            setCursor(Cursor.getDefaultCursor());
            logger.warn("Login failed: invalid credentials for username={}", username);
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password.\nPlease try again.", 
                "Login Failed", 
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            usernameField.requestFocus();
        }
    }
}