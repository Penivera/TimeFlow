package org.timeflow.service;

import org.timeflow.entity.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ReportService extends BaseService {

    // MODIFIED: Parameter changed to SemesterType
    public Map<String, Object> generateDepartmentUtilizationReport(Department department, SemesterType semester) {
        Map<String, Object> report = new HashMap<>();

        try {
            List<Timetable> departmentTimetables = daoFactory.getTimetableDAO()
                    .findByDepartmentAndSemester(department, semester);

            // Calculation logic remains the same
            int totalSlots = departmentTimetables.size();
            long approvedSlots = departmentTimetables.stream()
                    .filter(t -> t.getStatus() == TimetableStatus.APPROVED)
                    .count();
            // ... (rest of the stats) ...

            report.put("departmentName", department.getName());
            report.put("semesterName", semester.toString()); // Use toString() for enum
            report.put("totalSlots", totalSlots);
            report.put("approvedSlots", approvedSlots);
            // ... (rest of the report puts) ...

            logger.info("Generated utilization report for department: {}", department.getName());

        } catch (Exception e) {
            logger.error("Error generating department utilization report", e);
        }
        return report;
    }

    // MODIFIED: Parameter changed to SemesterType
    public Map<String, Object> generateConflictReport(SemesterType semester) {
        Map<String, Object> report = new HashMap<>();

        try {
            List<Conflict> allConflicts = daoFactory.getConflictDAO().findAll();

            // Filter conflicts for the selected semester enum
            List<Conflict> semesterConflicts = allConflicts.stream()
                    .filter(c -> c.getTimetable1().getSemester() == semester || c.getTimetable2().getSemester() == semester)
                    .collect(Collectors.toList());

            // ... (calculation logic) ...

            report.put("semesterName", semester.toString()); // Use toString()
            report.put("totalConflicts", semesterConflicts.size());
            // ... (rest of the report puts) ...

            logger.info("Generated conflict report for semester: {}", semester.toString());

        } catch (Exception e) {
            logger.error("Error generating conflict report", e);
        }
        return report;
    }

    // MODIFIED: Parameter changed to SemesterType
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
            // ... (rest of the report puts) ...

            logger.info("Generated lecturer workload report for department: {}", department.getName());
        } catch (Exception e) {
            logger.error("Error generating lecturer workload report", e);
        }
        return report;
    }

    public Map<String, Object> generateExamScheduleReport(Department department, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        try {
            List<Timetable> exams = daoFactory.getTimetableDAO()
                    .findExamsByDateRange(startDate, endDate, department);

            // Group by date
            Map<LocalDate, List<Timetable>> examsByDate = new HashMap<>();
            for (Timetable exam : exams) {
                LocalDate examDate = exam.getSpecificDate();
                examsByDate.computeIfAbsent(examDate, k -> new ArrayList<>()).add(exam);
            }

            report.put("departmentName", department.getName());
            report.put("startDate", startDate);
            report.put("endDate", endDate);
            report.put("totalExams", exams.size());
            report.put("examsByDate", examsByDate);

            // Count by type
            long examCount = exams.stream().filter(e -> e.getType() == ActivityType.EXAM).count();
            long testCount = exams.stream().filter(e -> e.getType() == ActivityType.TEST).count();

            report.put("examCount", examCount);
            report.put("testCount", testCount);

            logger.info("Generated exam schedule report for department: {}", department.getName());

        } catch (Exception e) {
            logger.error("Error generating exam schedule report", e);
            throw new RuntimeException("Failed to generate exam report", e);
        }

        return report;
    }
}


