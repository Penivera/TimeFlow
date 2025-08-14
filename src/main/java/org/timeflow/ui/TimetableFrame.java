package org.timeflow.ui;

import org.timeflow.dao.TimetableDAO;
import org.timeflow.entity.Timetable;
import org.timeflow.entity.User;
import org.timeflow.entity.UserRole;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TimetableFrame extends JFrame {
    private User user;
    private TimetableDAO timetableDAO;
    private JTable timetableTable;
    private DefaultTableModel tableModel;

    public TimetableFrame(User user) {
        this.user = user;
        this.timetableDAO = new TimetableDAO();
        initComponents();
        setTitle("TimeFlow - Timetable");
        setSize(1000, 600);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Timetable");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Course", "Day", "Start Time", "End Time", "Room", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        timetableTable = new JTable(tableModel);
        timetableTable.setRowHeight(30);
        timetableTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(timetableTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);
        JButton refreshButton = createStyledButton("Refresh");
        refreshButton.addActionListener(e -> loadTimetables());
        filterPanel.add(refreshButton);

        if (user.getRole() == UserRole.ADMIN) {
            JButton addButton = createStyledButton("Add Timetable");
            addButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Add timetable feature coming soon!"));
            filterPanel.add(addButton);
        }

        mainPanel.add(filterPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        loadTimetables();
    }

    private void loadTimetables() {
        tableModel.setRowCount(0);
        List<Timetable> timetables = user.getRole() == UserRole.ADMIN ?
                timetableDAO.findBySemester(null) : // Replace null with actual semester
                timetableDAO.findByLecturer(user, null); // Replace null with actual semester
        for (Timetable t : timetables) {
            tableModel.addRow(new Object[]{
                    t.getCourse().getName(),
                    t.getDayOfWeek(),
                    t.getStartTime(),
                    t.getEndTime(),
                    t.getRoom(),
                    t.getStatus()
            });
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(0, 102, 204));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
}