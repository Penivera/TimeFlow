package org.timeflow.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.dao.CourseDAO;
import org.timeflow.dao.SemesterDAO;
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
    private User user;
    private boolean createMode;
    private CourseDAO courseDAO;
    private SemesterDAO semesterDAO;
    private TimetableService timetableService;
    private JComboBox<Course> courseComboBox;
    private JComboBox<DayOfWeek> dayComboBox;
    private JSpinner startTimeSpinner;
    private JSpinner endTimeSpinner;
    private JTextField roomField;
    private JComboBox<ActivityType> typeComboBox;
    private JComboBox<Semester> semesterComboBox;
    private JSpinner specificDateSpinner;

    public TimetableFrame(User user, boolean createMode) {
        super((Frame) null, true);
        this.user = user;
        this.createMode = createMode;
        courseDAO = new CourseDAO();
        semesterDAO = new SemesterDAO();
        timetableService = new TimetableService();
        initComponents();
        setTitle(createMode ? "Create Schedule" : "View Timetable");
        setSize(600, createMode ? 500 : 700);
        setMinimumSize(new Dimension(400, 400));
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
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel courseLabel = new JLabel("Course:");
            courseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(courseLabel, gbc);

            courseComboBox = new JComboBox<>();
            courseComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
            courseComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Course course) {
                        setText(course.getName() + " (" + course.getCode() + ")");
                    }
                    return this;
                }
            });
            loadCourses();
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            formPanel.add(courseComboBox, gbc);

            JLabel semesterLabel = new JLabel("Semester:");
            semesterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 1;
            formPanel.add(semesterLabel, gbc);

            semesterComboBox = new JComboBox<>();
            semesterComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
            semesterComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Semester semester) {
                        setText(semester.getName() + " (" + semester.getAcademicYear() + ")");
                    }
                    return this;
                }
            });
            loadSemesters();
            gbc.gridx = 1;
            gbc.gridy = 1;
            formPanel.add(semesterComboBox, gbc);

            JLabel dayLabel = new JLabel("Day:");
            dayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 2;
            formPanel.add(dayLabel, gbc);

            dayComboBox = new JComboBox<>(DayOfWeek.values());
            dayComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 2;
            formPanel.add(dayComboBox, gbc);

            JLabel startTimeLabel = new JLabel("Start Time:");
            startTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 3;
            formPanel.add(startTimeLabel, gbc);

            startTimeSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
            startTimeSpinner.setEditor(startTimeEditor);
            startTimeSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 3;
            formPanel.add(startTimeSpinner, gbc);

            JLabel endTimeLabel = new JLabel("End Time:");
            endTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 4;
            formPanel.add(endTimeLabel, gbc);

            endTimeSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
            endTimeSpinner.setEditor(endTimeEditor);
            endTimeSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 4;
            formPanel.add(endTimeSpinner, gbc);

            JLabel roomLabel = new JLabel("Room:");
            roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 5;
            formPanel.add(roomLabel, gbc);

            roomField = new JTextField(20);
            roomField.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 5;
            formPanel.add(roomField, gbc);

            JLabel typeLabel = new JLabel("Type:");
            typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 6;
            formPanel.add(typeLabel, gbc);

            typeComboBox = new JComboBox<>(ActivityType.values());
            typeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 6;
            formPanel.add(typeComboBox, gbc);

            JLabel specificDateLabel = new JLabel("Specific Date (optional):");
            specificDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 7;
            formPanel.add(specificDateLabel, gbc);

            specificDateSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(specificDateSpinner, "yyyy-MM-dd");
            specificDateSpinner.setEditor(dateEditor);
            specificDateSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 7;
            formPanel.add(specificDateSpinner, gbc);

            JButton saveButton = new JButton("Save Schedule");
            saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            saveButton.setBackground(new Color(7, 8, 9));
            saveButton.setForeground(Color.WHITE);
            saveButton.addActionListener(e -> saveSchedule());
            gbc.gridx = 0;
            gbc.gridy = 8;
            gbc.gridwidth = 2;
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

    private void loadCourses() {
        List<Course> courses;
        if (user.getRole() == UserRole.LECTURER) {
            courses = courseDAO.findByLecturer(user);
            logger.info("Loaded {} courses for lecturer: {}", courses.size(), user.getUsername());
        } else {
            courses = courseDAO.findByDepartment(user.getDepartment());
            logger.info("Loaded {} courses for department: {}", courses.size(), user.getDepartment().getName());
        }
        courseComboBox.removeAllItems();
        if (courses.isEmpty()) {
            logger.warn("No courses found for user: {}", user.getUsername());
            courseComboBox.addItem(new Course("No courses available", "", 0, null, null, 0));
            courseComboBox.setEnabled(false);
            JOptionPane.showMessageDialog(this, "No courses available. Please contact the admin.", "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            for (Course course : courses) {
                courseComboBox.addItem(course);
            }
            courseComboBox.setEnabled(true);
        }
    }

    private void loadSemesters() {
        List<Semester> semesters = semesterDAO.findAll();
        logger.info("Loaded {} semesters", semesters.size());
        semesterComboBox.removeAllItems();
        if (semesters.isEmpty()) {
            logger.warn("No semesters found");
            semesterComboBox.addItem(new Semester("No semesters available", null, null, ""));
            semesterComboBox.setEnabled(false);
            JOptionPane.showMessageDialog(this, "No semesters available. Please contact the admin.", "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            for (Semester semester : semesters) {
                semesterComboBox.addItem(semester);
            }
            semesterComboBox.setEnabled(true);
        }
    }

    private void loadTimetableData(DefaultTableModel model) {
        List<Timetable> timetables;
        if (user.getRole() == UserRole.STUDENT) {
            timetables = timetableService.getStudentTimetables(user);
        } else if (user.getRole() == UserRole.LECTURER) {
            timetables = timetableService.getLecturerTimetables(user);
        } else {
            timetables = timetableService.getStudentTimetables(user); // ADMIN/EXAMS_OFFICER see all
        }
        logger.info("Loaded {} timetables for user: {}", timetables.size(), user.getUsername());
        for (Timetable t : timetables) {
            model.addRow(new Object[]{
                    t.getCourse().getName(),
                    t.getDayOfWeek(),
                    t.getStartTime(),
                    t.getEndTime(),
                    t.getRoom(),
                    t.getType(),
                    t.getSemester().getName()
            });
        }
    }

    private void saveSchedule() {
        try {
            Course course = (Course) courseComboBox.getSelectedItem();
            if (course == null || course.getCode().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a valid course", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Semester semester = (Semester) semesterComboBox.getSelectedItem();
            if (semester == null || semester.getName().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a valid semester", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            DayOfWeek day = (DayOfWeek) dayComboBox.getSelectedItem();
            Date startTimeDate = (Date) startTimeSpinner.getValue();
            Date endTimeDate = (Date) endTimeSpinner.getValue();
            LocalTime startTime = startTimeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalTime endTime = endTimeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            String room = roomField.getText().trim();
            ActivityType type = (ActivityType) typeComboBox.getSelectedItem();
            Date specificDateValue = (Date) specificDateSpinner.getValue();
            LocalDate specificDate = specificDateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (room.isEmpty() || day == null || type == null) {
                JOptionPane.showMessageDialog(this, "All required fields must be filled", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Timetable timetable = new Timetable(course, day, startTime, endTime, room, type, semester);
            timetable.setSpecificDate(specificDate.equals(LocalDate.now()) && specificDate.isBefore(semester.getStartDate()) ? null : specificDate);

            timetableService.createTimetable(timetable, user);
            logger.info("Schedule created for course: {}, day: {}, time: {}-{}, room: {}", course.getCode(), day, startTime, endTime, room);
            JOptionPane.showMessageDialog(this, "Schedule created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            logger.error("Failed to save schedule: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Failed to save schedule: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}