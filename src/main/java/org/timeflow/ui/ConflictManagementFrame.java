package org.timeflow.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.dao.ConflictDAO;
import org.timeflow.entity.Conflict;
import org.timeflow.entity.Timetable;
import org.timeflow.entity.User;
import org.timeflow.service.ConflictDetectionService;
import org.timeflow.service.NotificationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ConflictManagementFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(ConflictManagementFrame.class);
    private final User user;
    private final ConflictDAO conflictDAO;
    private final ConflictDetectionService conflictService;
    private final NotificationService notificationService;
    private JTable conflictTable;
    private DefaultTableModel tableModel;
    private JButton manageButton;

    public ConflictManagementFrame(User user) {
        this.user = user;
        this.conflictDAO = new ConflictDAO();
        this.conflictService = new ConflictDetectionService();
        this.notificationService = new NotificationService();
        initComponents();
        setTitle("TimeFlow - Conflict Management");
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Unresolved Timetable Conflicts");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {
                "Conflict ID", "Type", "Status",
                "Course 1", "Lecturer 1", "Day/Date 1", "Time 1", "Room 1",
                "Course 2", "Lecturer 2", "Day/Date 2", "Time 2", "Room 2"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        conflictTable = new JTable(tableModel);
        conflictTable.setRowHeight(30);
        conflictTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Hide the ID column
        conflictTable.getColumnModel().getColumn(0).setMinWidth(0);
        conflictTable.getColumnModel().getColumn(0).setMaxWidth(0);
        conflictTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(conflictTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        manageButton = createStyledButton("Manage Selected Conflict");
        manageButton.setEnabled(false);
        buttonPanel.add(manageButton);

        JButton refreshButton = createStyledButton("Refresh");
        buttonPanel.add(refreshButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        // Add Listeners
        conflictTable.getSelectionModel().addListSelectionListener(e -> {
            manageButton.setEnabled(conflictTable.getSelectedRow() != -1);
        });

        manageButton.addActionListener(e -> handleManageConflict());
        refreshButton.addActionListener(e -> loadConflicts());

        loadConflicts();
    }

    private void loadConflicts() {
        tableModel.setRowCount(0);
        try {
            List<Conflict> conflicts = conflictDAO.findUnresolvedConflicts();
            for (Conflict c : conflicts) {
                Timetable t1 = c.getTimetable1();
                Timetable t2 = c.getTimetable2();

                // Null checks for safety
                String lecturer1 = (t1.getCourse().getLecturer() != null) ? t1.getCourse().getLecturer().getUsername() : "N/A";
                String lecturer2 = (t2.getCourse().getLecturer() != null) ? t2.getCourse().getLecturer().getUsername() : "N/A";

                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getType(),
                        c.getStatus(),
                        t1.getCourse().getCode(),
                        lecturer1,
                        t1.getDayOfWeek() != null ? t1.getDayOfWeek() : t1.getSpecificDate(),
                        t1.getStartTime() + " - " + t1.getEndTime(),
                        t1.getRoom().getName(),
                        t2.getCourse().getCode(),
                        lecturer2,
                        t2.getDayOfWeek() != null ? t2.getDayOfWeek() : t2.getSpecificDate(),
                        t2.getStartTime() + " - " + t2.getEndTime(),
                        t2.getRoom().getName()
                });
            }
            logger.info("Loaded {} unresolved conflicts.", conflicts.size());
        } catch (Exception e) {
            logger.error("Failed to load conflicts", e);
            JOptionPane.showMessageDialog(this, "Could not load conflicts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleManageConflict() {
        int selectedRow = conflictTable.getSelectedRow();
        if (selectedRow == -1) return;

        Long conflictId = (Long) tableModel.getValueAt(selectedRow, 0);
        Conflict conflict = conflictDAO.findById(conflictId);
        if (conflict == null) {
            JOptionPane.showMessageDialog(this, "Could not find the selected conflict.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object[] options = {"Email Lecturers to Coordinate", "Manually Mark as Resolved", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "What action would you like to take for this conflict?",
                "Manage Conflict",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]
        );

        if (choice == 0) { // Email Lecturers
            notificationService.notifyLecturersToResolve(conflict);
            JOptionPane.showMessageDialog(this, "An email has been sent to the involved lecturers.", "Email Sent", JOptionPane.INFORMATION_MESSAGE);
        } else if (choice == 1) { // Mark as Resolved
            handleResolveConflict(conflictId);
        }
    }

    private void handleResolveConflict(Long conflictId) {
        String resolutionNotes = JOptionPane.showInputDialog(
                this,
                "Please enter notes on how this conflict was resolved:",
                "Resolve Conflict",
                JOptionPane.PLAIN_MESSAGE
        );

        if (resolutionNotes != null && !resolutionNotes.trim().isEmpty()) {
            try {
                conflictService.resolveConflict(conflictId, resolutionNotes, user);
                JOptionPane.showMessageDialog(this, "Conflict marked as resolved.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadConflicts(); // Refresh the table
            } catch (Exception e) {
                logger.error("Failed to resolve conflict with ID {}", conflictId, e);
                JOptionPane.showMessageDialog(this, "Failed to resolve conflict: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (resolutionNotes != null) {
            JOptionPane.showMessageDialog(this, "Resolution notes cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
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