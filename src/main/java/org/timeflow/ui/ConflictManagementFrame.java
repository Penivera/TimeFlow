package org.timeflow.ui;

import org.timeflow.dao.TimetableDAO;
import org.timeflow.entity.Timetable;
import org.timeflow.entity.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;

public class ConflictManagementFrame extends JFrame {
    private User user;
    private TimetableDAO timetableDAO;
    private JTable conflictTable;
    private DefaultTableModel tableModel;

    public ConflictManagementFrame(User user) {
        this.user = user;
        this.timetableDAO = new TimetableDAO();
        initComponents();
        setTitle("TimeFlow - Conflict Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Timetable Conflicts");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(7, 8, 9));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Course", "Day", "Time", "Room", "Conflict Type"};
        tableModel = new DefaultTableModel(columns, 0);
        conflictTable = new JTable(tableModel);
        conflictTable.setRowHeight(30);
        conflictTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(conflictTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        JButton refreshButton = createStyledButton("Refresh");
        refreshButton.addActionListener(e -> loadConflicts());
        buttonPanel.add(refreshButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        loadConflicts();
    }

    private void loadConflicts() {
        tableModel.setRowCount(0);
        List<Timetable> conflicts = timetableDAO.findConflictingTimetables(
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), null, null, null);
        for (Timetable t : conflicts) {
            tableModel.addRow(new Object[]{
                    t.getCourse().getName(),
                    t.getDayOfWeek(),
                    t.getStartTime() + " - " + t.getEndTime(),
                    t.getRoom(),
                    "Time/Room Conflict"
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