package org.timeflow.ui;

import com.formdev.flatlaf.FlatLightLaf;
import org.timeflow.dao.UserDAO;
import org.timeflow.entity.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;
    private UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        setupLookAndFeel();
        initComponents();
        setTitle("TimeFlow - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setMinimumSize(new Dimension(350, 300));
        setLocationRelativeTo(null);
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
            UIManager.put("Button.background", Color.BLACK);
            UIManager.put("Button.foreground", Color.WHITE);
        } catch (Exception e) {
            System.err.println("Failed to set FlatLaf look-and-feel: " + e.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
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

        JLabel titleLabel = new JLabel("TimeFlow Login");
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
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        passwordField.setForeground(Color.BLACK);
        passwordField.setBackground(new Color(240, 240, 240));
        passwordField.setOpaque(true);
        passwordField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);

        loginButton = new JButton("Login");
        loginButton.setFont(getPreferredFont("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(Color.BLACK);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        addButtonHoverEffect(loginButton);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(loginButton, gbc);

        signupButton = new JButton("Sign Up");
        signupButton.setFont(getPreferredFont("Segoe UI", Font.BOLD, 14));
        signupButton.setBackground(Color.BLACK);
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
        addButtonHoverEffect(signupButton);
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SignupFrame(LoginFrame.this).setVisible(true);
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(signupButton, gbc);

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

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            dispose();
            new MainDashboardFrame(user).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}