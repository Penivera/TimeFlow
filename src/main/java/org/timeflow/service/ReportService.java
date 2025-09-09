package org.timeflow.service;

import org.timeflow.entity.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.timeflow.entity.*;

import org.timeflow.entity.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;

public class ReportService extends BaseService {

    // MODIFIED: This method now uses the correct DAO call and filters in-memory.
    public Map<String, Object> generateDepartmentUtilizationReport(Department department, SemesterType semester) {
        Map<String, Object> report = new HashMap<>();

        try {
            // Step 1: Fetch all timetables for the department.
            List<Timetable> allDepartmentTimetables = daoFactory.getTimetableDAO().findAllByDepartment(department);

            // Step 2: Filter the list by the selected semester.
            List<Timetable> departmentTimetablesInSemester = allDepartmentTimetables.stream()
                    .filter(t -> t.getSemester() == semester)
                    .collect(Collectors.toList());

            // Calculation logic now uses the correctly filtered list.
            int totalSlots = departmentTimetablesInSemester.size();
            long approvedSlots = departmentTimetablesInSemester.stream()
                    .filter(t -> t.getStatus() == TimetableStatus.APPROVED)
                    .count();
            long pendingSlots = departmentTimetablesInSemester.stream()
                    .filter(t -> t.getStatus() == TimetableStatus.PENDING_APPROVAL)
                    .count();
            long conflictedSlots = departmentTimetablesInSemester.stream()
                    .filter(t -> t.getStatus() == TimetableStatus.CONFLICTED)
                    .count();

            report.put("departmentName", department.getName());
            report.put("semesterName", semester.toString());
            report.put("totalSlots", totalSlots);
            report.put("approvedSlots", approvedSlots);
            report.put("pendingSlots", pendingSlots);
            report.put("conflictedSlots", conflictedSlots);

            logger.info("Generated utilization report for department: {}", department.getName());

        } catch (Exception e) {
            logger.error("Error generating department utilization report", e);
        }
        return report;
    }

    // This method's logic is correct as it fetches all conflicts and then filters.
    public Map<String, Object> generateConflictReport(SemesterType semester) {
        Map<String, Object> report = new HashMap<>();
        try {
            List<Conflict> allConflicts = daoFactory.getConflictDAO().findAll();

            List<Conflict> semesterConflicts = allConflicts.stream()
                    .filter(c -> c.getTimetable1().getSemester() == semester || c.getTimetable2().getSemester() == semester)
                    .collect(Collectors.toList());

            report.put("semesterName", semester.toString());
            report.put("totalConflicts", semesterConflicts.size());
            // ... (add more stats as needed)

            logger.info("Generated conflict report for semester: {}", semester.toString());
        } catch (Exception e) {
            logger.error("Error generating conflict report", e);
        }
        return report;
    }

    // This method's logic is correct as it calls the updated findByLecturer method.
    public Map<String, Object> generateLecturerWorkloadReport(Department department, SemesterType semester) {
        Map<String, Object> report = new HashMap<>();
        try {
            List<User> lecturers = daoFactory.getUserDAO().findLecturersByDepartment(department);
            Map<String, Integer> lecturerWorkload = new HashMap<>();

            for (User lecturer : lecturers) {
                List<Timetable> lecturerTimetables = daoFactory.getTimetableDAO()
                        .findByLecturer(lecturer, semester);
                lecturerWorkload.put(lecturer.getUsername(), lecturerTimetables.size());
            }

            report.put("departmentName", department.getName());
            report.put("semesterName", semester.toString());
            report.put("lecturerWorkloads", lecturerWorkload);

            logger.info("Generated lecturer workload report for department: {}", department.getName());
        } catch (Exception e) {
            logger.error("Error generating lecturer workload report", e);
        }
        return report;
    }
    public void generateTimetablePdf(User student, List<Timetable> timetables) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Timetable as PDF");
        fileChooser.setSelectedFile(new File(student.getUsername() + "_timetable.pdf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PdfWriter writer = new PdfWriter(fileToSave.getAbsolutePath())) {
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Add Title
                document.add(new Paragraph("Timetable for " + student.getUsername())
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(20)
                        .setBold());

                // Group and sort data
                Map<DayOfWeek, List<Timetable>> groupedByDay = timetables.stream()
                        .filter(t -> t.getDayOfWeek() != null)
                        .collect(Collectors.groupingBy(Timetable::getDayOfWeek));
                groupedByDay.values().forEach(list -> list.sort((t1, t2) -> t1.getStartTime().compareTo(t2.getStartTime())));

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                // Create table for each day
                for (DayOfWeek day : DayOfWeek.values()) {
                    if (day.getValue() > 5) continue; // Skip weekends
                    List<Timetable> daySchedules = groupedByDay.get(day);

                    if (daySchedules != null && !daySchedules.isEmpty()) {
                        document.add(new Paragraph(day.toString()).setFontSize(14).setBold().setMarginTop(15));
                        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 5, 2, 2}));
                        table.setWidth(UnitValue.createPercentValue(100));
                        table.addHeaderCell("Time").setBold();
                        table.addHeaderCell("Course").setBold();
                        table.addHeaderCell("Room").setBold();
                        table.addHeaderCell("Type").setBold();

                        for (Timetable entry : daySchedules) {
                            table.addCell(entry.getStartTime().format(timeFormatter) + " - " + entry.getEndTime().format(timeFormatter));
                            table.addCell(entry.getCourse().getName() + " (" + entry.getCourse().getCode() + ")");
                            table.addCell(entry.getRoom().getName());
                            table.addCell(entry.getType().toString());
                        }
                        document.add(table);
                    }
                }
                document.close();
                JOptionPane.showMessageDialog(null, "Timetable saved successfully as PDF.", "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                logger.info("Successfully generated PDF timetable for user {}", student.getUsername());
            } catch (Exception e) {
                logger.error("Error generating PDF for user {}", student.getUsername(), e);
                JOptionPane.showMessageDialog(null, "Could not save PDF: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // This method is also correct.
    public Map<String, Object> generateExamScheduleReport(Department department, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        try {
            List<Timetable> exams = daoFactory.getTimetableDAO()
                    .findExamsByDateRange(startDate, endDate, department);
            report.put("departmentName", department.getName());
            report.put("totalExams", exams.size());
            report.put("exams", exams);
            logger.info("Generated exam schedule report for department: {}", department.getName());
        } catch (Exception e) {
            logger.error("Error generating exam schedule report", e);
        }
        return report;
    }
}