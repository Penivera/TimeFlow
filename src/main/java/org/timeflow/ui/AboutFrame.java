package org.timeflow.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;

public class AboutFrame extends JDialog {

    public AboutFrame(JFrame parent) {
        super(parent, "About", true);
        initComponents();
        setSize(450, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel appPanel = createAppInfoPanel();
        tabbedPane.addTab("About TimeFlow", null, appPanel, "Information about the application");

        JPanel creatorPanel = createCreatorInfoPanel();
        tabbedPane.addTab("About the Creator", null, creatorPanel, "Information about the developer");

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose());
        buttonPanel.add(okButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createAppInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("TimeFlow", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        panel.add(titleLabel, BorderLayout.NORTH);

        String aboutText = "<html>"
                + "<body style='text-align: center;'>"
                + "<b>Version:</b> 1.0.0<br><br>"
                + "A comprehensive timetable management system.<br><br>"
                + "Copyright (c) 2025 Peniel Ben<br>"
                + "This software is provided under the MIT License."
                + "</body>"
                + "</html>";

        JLabel contentLabel = new JLabel(aboutText);
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(contentLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCreatorInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);

        JLabel pictureLabel = new JLabel("Loading image...");
        pictureLabel.setPreferredSize(new Dimension(100, 100));
        pictureLabel.setMinimumSize(new Dimension(100, 100));
        pictureLabel.setMaximumSize(new Dimension(100, 100));
        pictureLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        pictureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadCreatorImage(pictureLabel);
        panel.add(pictureLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel nameLabel = new JLabel("Peniel Ben");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nameLabel);

        JLabel titleLabel = new JLabel("Software Developer");
        titleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createClickableLink("GitHub: PenielBen", "https://github.com/Penivera"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(createClickableLink("LinkedIn:Peniel Bengit ", "https://www.linkedin.com/peniel-ben"));

        return panel;
    }

    private void loadCreatorImage(JLabel imageLabel) {
        String imageUrl = "https://avatars.githubusercontent.com/u/168909090?v=4";

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    URL url = new URL(imageUrl);
                    BufferedImage image = ImageIO.read(url);
                    if (image != null) {
                        Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImage);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load image from URL: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon imageIcon = get();
                    if (imageIcon != null) {
                        imageLabel.setIcon(imageIcon);
                        imageLabel.setText(null);
                    } else {
                        imageLabel.setText("Image not found");
                    }
                } catch (Exception e) {
                    imageLabel.setText("Failed to load");
                }
            }
        };
        worker.execute();
    }

    private JLabel createClickableLink(String text, String url) {
        JLabel linkLabel = new JLabel("<html><a href=''>" + text + "</a></html>");
        linkLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AboutFrame.this,
                            "Could not open the link.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return linkLabel;
    }
}