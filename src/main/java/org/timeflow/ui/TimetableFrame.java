package org.timeflow.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.dao.CourseDAO;
import org.timeflow.dao.RoomDAO;
import org.timeflow.entity.*;
import org.timeflow.service.AuthenticationService;
import org.timeflow.service.NotificationService;
import org.timeflow.service.TimetableService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TimetableFrame extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(TimetableFrame.class);
    private final User user;
    private final TimetableService timetableService;
    private final CourseDAO courseDAO;
    private final RoomDAO roomDAO;
    private final NotificationService notificationService;
    private Timetable timetableToEdit;
    private JPanel mainViewPanel;

    // Components for Create/Edit Mode
    private JComboBox<Course> courseComboBox;
    private JComboBox<DayOfWeek> dayComboBox;
    private JSpinner startTimeSpinner;
    private JSpinner endTimeSpinner;
    private JComboBox<Room> roomComboBox;
    private JComboBox<ActivityType> typeComboBox;
    private JComboBox<SemesterType> semesterComboBox;
    private JSpinner specificDateSpinner;
    private JCheckBox isSingleDayEventCheckBox;

    public TimetableFrame(User user, boolean createMode) {
        this(user, createMode, null);
    }

    public TimetableFrame(User user, boolean createMode, Timetable timetableToEdit) {
        super((Frame) null, true);
        this.user = user;
        this.timetableService = new TimetableService();
        this.courseDAO = new CourseDAO();
        this.roomDAO = new RoomDAO();
        this.notificationService = new NotificationService();
        this.timetableToEdit = timetableToEdit;
        initComponents(createMode);
        setTitle(createMode ? (timetableToEdit == null ? "Create Schedule" : "Edit Schedule") : "View/Manage Timetable");
        setSize(createMode ? 600 : 800, 700);
        setMinimumSize(new Dimension(createMode ? 450 : 600, 550));
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initComponents(boolean createMode) {
        if (createMode && AuthenticationService.getInstance().canManageTimetables()) {
            add(createSchedulePanel(), BorderLayout.CENTER);
        } else {
            mainViewPanel = new JPanel(new BorderLayout());
            mainViewPanel.add(new JScrollPane(createTimetableViewPanel()), BorderLayout.CENTER);
            add(mainViewPanel, BorderLayout.CENTER);
        }
    }

    private JPanel createTimetableViewPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        List<Timetable> timetables = getTimetablesForUser();
        Map<DayOfWeek, List<Timetable>> groupedByDay = timetables.stream()
                .filter(t -> t.getDayOfWeek() != null)
                .collect(Collectors.groupingBy(Timetable::getDayOfWeek));
        groupedByDay.values().forEach(list -> list.sort((t1, t2) -> t1.getStartTime().compareTo(t2.getStartTime())));

        boolean hasSchedules = false;
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.getValue() > 5) continue;
            List<Timetable> daySchedules = groupedByDay.get(day);

            if (daySchedules != null && !daySchedules.isEmpty()) {
                hasSchedules = true;
                JLabel dayLabel = new JLabel(day.toString());
                dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
                dayLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 220, 220)));
                dayLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                mainPanel.add(dayLabel);
                mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                for (Timetable entry : daySchedules) {
                    JPanel entryPanel = createTimetableEntryPanel(entry);
                    entryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    mainPanel.add(entryPanel);
                    mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                }
            }
        }

        if (!hasSchedules) {
            mainPanel.add(new JLabel("No schedules found for this timetable."));
        }
        return mainPanel;
    }

    private List<Timetable> getTimetablesForUser() {
        UserRole role = user.getRole();
        logger.info("Loading timetable view for user role: {}", role);
        switch (role) {
            case STUDENT:
                return timetableService.getStudentTimetables(user);
            case LECTURER:
                return timetableService.getLecturerTimetables(user);
            case EXAMS_OFFICER:
                return timetableService.getDepartmentalTimetables(user.getDepartment());
            case ADMIN:
                return timetableService.getAllTimetables();
            default:
                return Collections.emptyList();
        }
    }

    private JPanel createTimetableEntryPanel(Timetable entry) {
        JPanel panel = new JPanel(new BorderLayout(15, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(new Color(248, 249, 250));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String timeString = "<html>" + entry.getStartTime().format(formatter) + "<br>" + entry.getEndTime().format(formatter) + "</html>";
        JLabel timeLabel = new JLabel(timeString);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(timeLabel, BorderLayout.WEST);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        JLabel courseLabel = new JLabel(entry.getCourse().getName() + " (" + entry.getCourse().getCode() + ")");
        courseLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        detailsPanel.add(courseLabel);

        String lecturerInfo = (entry.getCourse().getLecturer() != null) ? " | " + entry.getCourse().getLecturer().getUsername() : "";
        JLabel locationLabel = new JLabel(entry.getRoom().getName() + " (" + entry.getType() + ")" + lecturerInfo);
        locationLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        detailsPanel.add(locationLabel);

        panel.add(detailsPanel, BorderLayout.CENTER);

        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.EXAMS_OFFICER) {
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem editItem = new JMenuItem("Edit Schedule");
            JMenuItem deleteItem = new JMenuItem("Delete Schedule");
            JMenuItem contactItem = new JMenuItem("Contact Lecturer");

            editItem.addActionListener(e -> handleEdit(entry));
            deleteItem.addActionListener(e -> handleDelete(entry));
            contactItem.addActionListener(e -> handleContactLecturer(entry));

            popupMenu.add(editItem);
            popupMenu.add(deleteItem);
            popupMenu.addSeparator();
            popupMenu.add(contactItem);

            contactItem.setEnabled(entry.getCourse().getLecturer() != null);

            panel.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
        }
        return panel;
    }

    private void handleContactLecturer(Timetable entry) {
        User lecturer = entry.getCourse().getLecturer();
        if (lecturer == null) {
            JOptionPane.showMessageDialog(this, "This course has no assigned lecturer.", "No Lecturer", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String message = JOptionPane.showInputDialog(this, "Enter message for " + lecturer.getUsername() + ":", "Contact Lecturer", JOptionPane.PLAIN_MESSAGE);
        if (message != null && !message.trim().isEmpty()) {
            notificationService.sendAdminInquiryToLecturer(user, lecturer, entry, message);
            JOptionPane.showMessageDialog(this, "Message sent.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleEdit(Timetable entry) {
        logger.info("User {} is editing timetable ID: {}", user.getUsername(), entry.getId());
        new TimetableFrame(user, true, entry).setVisible(true);
        refreshTimetableView();
    }

    private void handleDelete(Timetable entry) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this schedule?\n" + entry.getCourse().getCode() + " on " + (entry.getDayOfWeek() != null ? entry.getDayOfWeek() : entry.getSpecificDate()),
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                timetableService.deleteTimetable(entry.getId());
                refreshTimetableView();
            } catch (Exception e) {
                logger.error("Failed to delete timetable ID: {}", entry.getId(), e);
                JOptionPane.showMessageDialog(this, "Could not delete schedule: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshTimetableView() {
        mainViewPanel.removeAll();
        mainViewPanel.add(new JScrollPane(createTimetableViewPanel()), BorderLayout.CENTER);
        mainViewPanel.revalidate();
        mainViewPanel.repaint();
    }

    private JPanel createSchedulePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Course:"), gbc);
        courseComboBox = new JComboBox<>();
        loadCourses();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; formPanel.add(courseComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Semester:"), gbc);
        semesterComboBox = new JComboBox<>();
        loadSemesters();
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(semesterComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Type:"), gbc);
        typeComboBox = new JComboBox<>(ActivityType.values());
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(typeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Room:"), gbc);
        roomComboBox = new JComboBox<>();
        loadRooms();
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(roomComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Start Time:"), gbc);
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(startTimeSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("End Time:"), gbc);
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));
        gbc.gridx = 1; gbc.gridy = 5; formPanel.add(endTimeSpinner, gbc);

        isSingleDayEventCheckBox = new JCheckBox("Single-Day Event (e.g., Exam/Test)");
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; formPanel.add(isSingleDayEventCheckBox, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 7; formPanel.add(new JLabel("Day of Week:"), gbc);
        dayComboBox = new JComboBox<>(DayOfWeek.values());
        gbc.gridx = 1; gbc.gridy = 7; formPanel.add(dayComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 8; formPanel.add(new JLabel("Specific Date:"), gbc);
        specificDateSpinner = new JSpinner(new SpinnerDateModel());
        specificDateSpinner.setEditor(new JSpinner.DateEditor(specificDateSpinner, "yyyy-MM-dd"));
        gbc.gridx = 1; gbc.gridy = 8; formPanel.add(specificDateSpinner, gbc);

        isSingleDayEventCheckBox.addActionListener(e -> toggleEventFields());
        toggleEventFields();

        if (timetableToEdit != null) {
            prefillFormForEdit();
        }

        JButton saveButton = new JButton(timetableToEdit == null ? "Save Schedule" : "Update Schedule");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.addActionListener(e -> saveSchedule());
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; formPanel.add(saveButton, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private void prefillFormForEdit() {
        courseComboBox.setSelectedItem(timetableToEdit.getCourse());
        semesterComboBox.setSelectedItem(timetableToEdit.getSemester());
        typeComboBox.setSelectedItem(timetableToEdit.getType());
        roomComboBox.setSelectedItem(timetableToEdit.getRoom());

        Date startTime = Date.from(timetableToEdit.getStartTime().atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
        startTimeSpinner.setValue(startTime);
        Date endTime = Date.from(timetableToEdit.getEndTime().atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
        endTimeSpinner.setValue(endTime);

        if (timetableToEdit.getSpecificDate() != null) {
            isSingleDayEventCheckBox.setSelected(true);
            Date specificDate = Date.from(timetableToEdit.getSpecificDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            specificDateSpinner.setValue(specificDate);
        } else {
            isSingleDayEventCheckBox.setSelected(false);
            dayComboBox.setSelectedItem(timetableToEdit.getDayOfWeek());
        }
        toggleEventFields();
    }

    private void saveSchedule() {
        try {
            Course course = (Course) courseComboBox.getSelectedItem();
            SemesterType semester = (SemesterType) semesterComboBox.getSelectedItem();
            Room room = (Room) roomComboBox.getSelectedItem();
            ActivityType type = (ActivityType) typeComboBox.getSelectedItem();
            Date startTimeDate = (Date) startTimeSpinner.getValue();
            LocalTime startTime = startTimeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            Date endTimeDate = (Date) endTimeSpinner.getValue();
            LocalTime endTime = endTimeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            Timetable timetable = (timetableToEdit != null) ? timetableToEdit : new Timetable();
            timetable.setCourse(course);
            timetable.setSemester(semester);
            timetable.setRoom(room);
            timetable.setType(type);
            timetable.setStartTime(startTime);
            timetable.setEndTime(endTime);

            if (isSingleDayEventCheckBox.isSelected()) {
                Date specificDateValue = (Date) specificDateSpinner.getValue();
                timetable.setSpecificDate(specificDateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                timetable.setDayOfWeek(null);
            } else {
                timetable.setDayOfWeek((DayOfWeek) dayComboBox.getSelectedItem());
                timetable.setSpecificDate(null);
            }

            if (timetableToEdit != null) {
                timetableService.updateTimetable(timetable, user);
                JOptionPane.showMessageDialog(this, "Schedule updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Timetable createdTimetable = timetableService.createTimetable(timetable, user);
                String feedbackMessage = "Schedule created and is pending approval.";
                if (createdTimetable.getStatus() == TimetableStatus.CONFLICTED) {
                    feedbackMessage = "Schedule created with conflicts. Please check the conflict management screen.";
                }
                JOptionPane.showMessageDialog(this, feedbackMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
        } catch (Exception e) {
            logger.error("Failed to save schedule", e);
            JOptionPane.showMessageDialog(this, "Failed to save schedule: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleEventFields() {
        boolean isSingleDay = isSingleDayEventCheckBox.isSelected();
        specificDateSpinner.setEnabled(isSingleDay);
        dayComboBox.setEnabled(!isSingleDay);
    }

    private void loadCourses() {
        List<Course> courses;
        UserRole userRole = user.getRole();
        if (userRole == UserRole.LECTURER) {
            courses = courseDAO.findByLecturer(user);
        } else if (userRole == UserRole.ADMIN || userRole == UserRole.EXAMS_OFFICER) {
            courses = courseDAO.findAll();
        } else {
            Department userDept = user.getDepartment();
            courses = (userDept != null) ? courseDAO.findByDepartment(userDept) : Collections.emptyList();
        }
        courseComboBox.removeAllItems();
        if (courses.isEmpty()) {
            courseComboBox.setEnabled(false);
        } else {
            courses.forEach(courseComboBox::addItem);
            courseComboBox.setEnabled(true);
        }
    }

    private void loadRooms() {
        List<Room> rooms = roomDAO.findAll();
        roomComboBox.removeAllItems();
        if (rooms.isEmpty()) {
            roomComboBox.setEnabled(false);
        } else {
            rooms.forEach(roomComboBox::addItem);
            roomComboBox.setEnabled(true);
        }
    }

    private void loadSemesters() {
        semesterComboBox.setModel(new DefaultComboBoxModel<>(SemesterType.values()));
        semesterComboBox.setEnabled(true);
    }
}