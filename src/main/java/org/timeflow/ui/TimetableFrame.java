package org.timeflow.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.dao.CourseDAO;
import org.timeflow.entity.*;
import org.timeflow.service.AuthenticationService;
import org.timeflow.service.TimetableService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.*;
import java.util.Date;
import java.util.List;

public class TimetableFrame extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(TimetableFrame.class);
    private final User user;
    private final boolean createMode;
    private final CourseDAO courseDAO;
    private final TimetableService timetableService;
    private JComboBox<Course> courseComboBox;
    private JComboBox<DayOfWeek> dayComboBox;
    private JSpinner startTimeSpinner;
    private JSpinner endTimeSpinner;
    private JTextField roomField;
    private JComboBox<ActivityType> typeComboBox;
    private JComboBox<SemesterType> semesterComboBox;
    private JSpinner specificDateSpinner;
    private JCheckBox isSingleDayEventCheckBox;

    public TimetableFrame(User user, boolean createMode) {
        super((Frame) null, true);
        this.user = user;
        this.createMode = createMode;
        // DAOs are now fetched from the service layer's DAOFactory
        this.courseDAO = new CourseDAO();
        this.timetableService = new TimetableService();
        initComponents();
        setTitle(createMode ? "Create Schedule" : "View Timetable");
        setSize(600, createMode ? 600 : 700);
        setMinimumSize(new Dimension(450, 550));
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        if (createMode && AuthenticationService.getInstance().canManageTimetables()) {
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 10, 5, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Course
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Course:"), gbc);
            courseComboBox = new JComboBox<>();
            loadCourses();
            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
            formPanel.add(courseComboBox, gbc);

            // Semester
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Semester:"), gbc);
            semesterComboBox = new JComboBox<>();
            loadSemesters();
            gbc.gridx = 1; gbc.gridy = 1;
            formPanel.add(semesterComboBox, gbc);

            // Other fields...
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Type:"), gbc);
            typeComboBox = new JComboBox<>(ActivityType.values());
            gbc.gridx = 1; gbc.gridy = 2;
            formPanel.add(typeComboBox, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Room:"), gbc);
            roomField = new JTextField(20);
            gbc.gridx = 1; gbc.gridy = 3;
            formPanel.add(roomField, gbc);

            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Start Time:"), gbc);
            startTimeSpinner = new JSpinner(new SpinnerDateModel());
            startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
            gbc.gridx = 1; gbc.gridy = 4;
            formPanel.add(startTimeSpinner, gbc);

            gbc.gridx = 0; gbc.gridy = 5;
            formPanel.add(new JLabel("End Time:"), gbc);
            endTimeSpinner = new JSpinner(new SpinnerDateModel());
            endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));
            gbc.gridx = 1; gbc.gridy = 5;
            formPanel.add(endTimeSpinner, gbc);

            isSingleDayEventCheckBox = new JCheckBox("Single-Day Event (e.g., Exam/Test)");
            gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
            formPanel.add(isSingleDayEventCheckBox, gbc);
            gbc.gridwidth = 1;

            gbc.gridx = 0; gbc.gridy = 7;
            formPanel.add(new JLabel("Day of Week:"), gbc);
            dayComboBox = new JComboBox<>(DayOfWeek.values());
            gbc.gridx = 1; gbc.gridy = 7;
            formPanel.add(dayComboBox, gbc);

            gbc.gridx = 0; gbc.gridy = 8;
            formPanel.add(new JLabel("Specific Date:"), gbc);
            specificDateSpinner = new JSpinner(new SpinnerDateModel());
            specificDateSpinner.setEditor(new JSpinner.DateEditor(specificDateSpinner, "yyyy-MM-dd"));
            gbc.gridx = 1; gbc.gridy = 8;
            formPanel.add(specificDateSpinner, gbc);

            isSingleDayEventCheckBox.addActionListener(e -> toggleEventFields());
            toggleEventFields();

            JButton saveButton = new JButton("Save Schedule");
            saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            saveButton.addActionListener(e -> saveSchedule());
            gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
            formPanel.add(saveButton, gbc);

            JScrollPane scrollPane = new JScrollPane(formPanel);
            scrollPane.setBorder(null);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
        } else {
            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"Course", "Day", "Start Time", "End Time", "Room", "Type", "Semester"}, 0);
            JTable timetableTable = new JTable(model);
            loadTimetableData(model);
            JScrollPane scrollPane = new JScrollPane(timetableTable);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
        }

        add(mainPanel, BorderLayout.CENTER);
    }

    private void toggleEventFields() {
        boolean isSingleDay = isSingleDayEventCheckBox.isSelected();
        specificDateSpinner.setEnabled(isSingleDay);
        dayComboBox.setEnabled(!isSingleDay);
    }

    private void saveSchedule() {
        try {
            Course course = (Course) courseComboBox.getSelectedItem();
            if (course == null || course.getId() == null) {
                JOptionPane.showMessageDialog(this, "Please select a valid course.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // --- FIX #1: The validation logic is simplified for the enum ---
            SemesterType semester = (SemesterType) semesterComboBox.getSelectedItem();
            if (semester == null) {
                JOptionPane.showMessageDialog(this, "Please select a valid semester.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String room = roomField.getText().trim();
            if (room.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Room field cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date startTimeDate = (Date) startTimeSpinner.getValue();
            Date endTimeDate = (Date) endTimeSpinner.getValue();
            LocalTime startTime = startTimeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalTime endTime = endTimeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
                JOptionPane.showMessageDialog(this, "Start time must be before end time.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ActivityType type = (ActivityType) typeComboBox.getSelectedItem();
            Timetable timetable = new Timetable();
            timetable.setCourse(course);
            timetable.setSemester(semester);
            timetable.setRoom(room);
            timetable.setType(type);
            timetable.setStartTime(startTime);
            timetable.setEndTime(endTime);

            if (isSingleDayEventCheckBox.isSelected()) {
                Date specificDateValue = (Date) specificDateSpinner.getValue();
                LocalDate specificDate = specificDateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                timetable.setSpecificDate(specificDate);
                timetable.setDayOfWeek(null);
            } else {
                DayOfWeek day = (DayOfWeek) dayComboBox.getSelectedItem();
                timetable.setDayOfWeek(day);
                timetable.setSpecificDate(null);
            }

            Timetable createdTimetable = timetableService.createTimetable(timetable, user);

            String feedbackMessage = "Schedule created and is pending approval.";
            if (createdTimetable.getStatus() == TimetableStatus.CONFLICTED) {
                feedbackMessage = "Schedule created, but conflicts were detected! Please check the conflict management screen.";
                logger.warn("Schedule created with conflicts for course: {}", course.getCode());
            } else {
                logger.info("Schedule created for course: {}", course.getCode());
            }

            JOptionPane.showMessageDialog(this, feedbackMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception e) {
            logger.error("Failed to save schedule: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Failed to save schedule: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCourses() {
        List<Course> courses;
        UserRole userRole = user.getRole();

        if (userRole == UserRole.LECTURER) {
            courses = courseDAO.findByLecturer(user);
        } else if (userRole == UserRole.ADMIN) {
            courses = courseDAO.findAll();
        } else {
            Department userDept = user.getDepartment();
            courses = (userDept != null) ? courseDAO.findByDepartment(userDept) : List.of();
        }

        courseComboBox.removeAllItems();
        if (courses.isEmpty()) {
            courseComboBox.setEnabled(false);
        } else {
            courses.forEach(courseComboBox::addItem);
            courseComboBox.setEnabled(true);
        }
    }

    private void loadSemesters() {
        semesterComboBox.setModel(new DefaultComboBoxModel<>(SemesterType.values()));
        semesterComboBox.setEnabled(true);
        logger.info("Loaded semester types into dropdown.");
    }

    private void loadTimetableData(DefaultTableModel model) {
        List<Timetable> timetables;
        if (user.getRole() == UserRole.STUDENT) {
            timetables = timetableService.getStudentTimetables(user);
        } else if (user.getRole() == UserRole.LECTURER) {
            timetables = timetableService.getLecturerTimetables(user);
        } else {
            timetables = timetableService.getStudentTimetables(user);
        }
        for (Timetable t : timetables) {
            model.addRow(new Object[]{
                    t.getCourse().getName(),
                    t.getSpecificDate() != null ? t.getSpecificDate() : t.getDayOfWeek(),
                    t.getStartTime(),
                    t.getEndTime(),
                    t.getRoom(),
                    t.getType(),
                    // --- FIX #2: Use .toString() to get the display name from the enum ---
                    t.getSemester().toString()
            });
        }
    }
}