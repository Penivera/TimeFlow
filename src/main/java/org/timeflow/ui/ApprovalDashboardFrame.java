package org.timeflow.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.dao.TimetableDAO;
import org.timeflow.entity.Timetable;
import org.timeflow.entity.TimetableStatus;
import org.timeflow.entity.User;
import org.timeflow.service.TimetableService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ApprovalDashboardFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(ApprovalDashboardFrame.class);
    private final User user;
    private final TimetableDAO timetableDAO;
    private final TimetableService timetableService;
    private JTable pendingTable;
    private DefaultTableModel tableModel;
    private JButton approveButton;
    private JButton rejectButton;

    public ApprovalDashboardFrame(User user) {
        this.user = user;
        this.timetableDAO = new TimetableDAO();
        this.timetableService = new TimetableService();
        initComponents();
        setTitle("TimeFlow - Approve Schedules");
        setSize(1000, 600);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Schedules Pending Approval");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"ID", "Course", "Day/Date", "Time", "Room", "Type", "Semester"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        pendingTable = new JTable(tableModel);
        pendingTable.setRowHeight(30);
        pendingTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pendingTable.getColumnModel().getColumn(0).setMinWidth(0);
        pendingTable.getColumnModel().getColumn(0).setMaxWidth(0);
        pendingTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(pendingTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        approveButton = createStyledButton("Approve Selected");
        rejectButton = createStyledButton("Reject Selected");
        JButton refreshButton = createStyledButton("Refresh");

        approveButton.setEnabled(false);
        rejectButton.setEnabled(false);

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        pendingTable.getSelectionModel().addListSelectionListener(e -> {
            boolean isRowSelected = pendingTable.getSelectedRow() != -1;
            approveButton.setEnabled(isRowSelected);
            rejectButton.setEnabled(isRowSelected);
        });

        approveButton.addActionListener(e -> handleApprove());
        rejectButton.addActionListener(e -> handleReject());
        refreshButton.addActionListener(e -> loadPendingSchedules());

        loadPendingSchedules();
    }

    private void loadPendingSchedules() {
        tableModel.setRowCount(0);
        try {
            List<Timetable> pending = timetableDAO.findByStatus(TimetableStatus.PENDING_APPROVAL);
            for (Timetable t : pending) {
                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getCourse().getCode() + " - " + t.getCourse().getName(),
                        t.getSpecificDate() != null ? t.getSpecificDate().toString() : t.getDayOfWeek().toString(),
                        t.getStartTime() + " - " + t.getEndTime(),
                        t.getRoom(),
                        t.getType(),
                        t.getSemester().toString()
                });
            }
            logger.info("Loaded {} pending schedules.", pending.size());
        } catch (Exception e) {
            logger.error("Failed to load pending schedules", e);
            JOptionPane.showMessageDialog(this, "Could not load data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void handleApprove() {
        int selectedRow = pendingTable.getSelectedRow();
        if (selectedRow == -1) return;

        Long timetableId = (Long) tableModel.getValueAt(selectedRow, 0);
        int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to approve this schedule?", "Confirm Approval", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            approveButton.setEnabled(false);

            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    timetableService.approveTimetable(timetableId, user);
                    return "Schedule approved successfully.";
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(ApprovalDashboardFrame.this, result, "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        // --- MODIFIED: Catch the specific conflict error ---
                        String errorMessage = e.getCause().getMessage();
                        logger.error("Error approving timetable with ID {}: {}", timetableId, errorMessage);

                        if (errorMessage.contains("unresolved conflicts")) {
                            JOptionPane.showMessageDialog(
                                    ApprovalDashboardFrame.this,
                                    "This schedule cannot be approved because it has unresolved conflicts.\n" +
                                            "Please go to the 'Resolve Conflicts' screen to manage them first.",
                                    "Approval Failed: Unresolved Conflicts",
                                    JOptionPane.WARNING_MESSAGE
                            );
                        } else {
                            JOptionPane.showMessageDialog(ApprovalDashboardFrame.this, "Failed to approve schedule: " + errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } finally {
                        loadPendingSchedules();
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            };
            worker.execute();
        }
    }

    private void handleReject() {
        int selectedRow = pendingTable.getSelectedRow();
        if (selectedRow == -1) return;

        Long timetableId = (Long) tableModel.getValueAt(selectedRow, 0);
        String reason = JOptionPane.showInputDialog(this, "Please provide a reason for rejection:");

        if (reason != null && !reason.trim().isEmpty()) {
            try {
                timetableService.rejectTimetable(timetableId, reason, user);
                JOptionPane.showMessageDialog(this, "Schedule rejected.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPendingSchedules();
            } catch (Exception e) {
                logger.error("Error rejecting timetable with ID {}", timetableId, e);
                JOptionPane.showMessageDialog(this, "Failed to reject schedule: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (reason != null) {
            JOptionPane.showMessageDialog(this, "Rejection reason cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
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