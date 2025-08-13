package org.timeflow.service;

import org.timeflow.entity.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class ReportService extends BaseService {

    // Generate department utilization report
    public Map<String, Object> generateDepartmentUtilizationReport(Department department, Semester semester) {
        Map<String, Object> report = new HashMap<>();

        try {
            List<Timetable> departmentTimetables = daoFactory.getTimetableDAO()
                    .findByDepartmentAndSemester(department, semester);

            // Calculate statistics
            int totalSlots = departmentTimetables.size();
            long approvedSlots = departmentTimetables.stream()
                    .filter(t -> t.getStatus() == TimetableStatus.APPROVED)
                    .count();
            long conflictedSlots = departmentTimetables.stream()
                    .filter(t -> t.getStatus() == TimetableStatus.CONFLICTED)
                    .count();
            long pendingSlots = departmentTimetables.stream()
                    .filter(t -> t.getStatus() == TimetableStatus.PENDING_APPROVAL)
                    .count();

            // Count by activity type
            Map<ActivityType, Long> activityTypeCount = new HashMap<>();
            for (ActivityType type : ActivityType.values()) {
                long count = departmentTimetables.stream()
                        .filter(t -> t.getType() == type)
                        .count();
                activityTypeCount.put(type, count);
            }

            report.put("departmentName", department.getName());
            report.put("semesterName", semester.getName());
            report.put("totalSlots", totalSlots);
            report.put("approvedSlots", approvedSlots);
            report.put("conflictedSlots", conflictedSlots);
            report.put("pendingSlots", pendingSlots);
            report.put("utilizationRate", totalSlots > 0 ? (double) approvedSlots / totalSlots * 100 : 0);
            report.put("activityTypeBreakdown", activityTypeCount);

            logger.info("Generated utilization report for department: {}", department.getName());

        } catch (Exception e) {
            logger.error("Error generating department utilization report", e);
            throw new RuntimeException("Failed to generate report", e);
        }

        return report;
    }

    // Generate conflict analysis report
    public Map<String, Object> generateConflictReport(Semester semester) {
        Map<String, Object> report = new HashMap<>();

        try {
            List<Conflict> allConflicts = daoFactory.getConflictDAO().findAll();

            // Filter conflicts for current semester
            List<Conflict> semesterConflicts = allConflicts.stream()
                    .filter(c -> c.getTimetable1().getSemester().equals(semester) ||
                            c.getTimetable2().getSemester().equals(semester))
                    .toList();

            // Count by type
            Map<ConflictType, Long> typeCount = new HashMap<>();
            for (ConflictType type : ConflictType.values()) {
                long count = semesterConflicts.stream()
                        .filter(c -> c.getType() == type)
                        .count();
                typeCount.put(type, count);
            }

            // Count by status
            Map<ConflictStatus, Long> statusCount = new HashMap<>();
            for (ConflictStatus status : ConflictStatus.values()) {
                long count = semesterConflicts.stream()
                        .filter(c -> c.getStatus() == status)
                        .count();
                statusCount.put(status, count);
            }

            report.put("semesterName", semester.getName());
            report.put("totalConflicts", semesterConflicts.size());
            report.put("conflictsByType", typeCount);
            report.put("conflictsByStatus", statusCount);

            logger.info("Generated conflict report for semester: {}", semester.getName());

        } catch (Exception e) {
            logger.error("Error generating conflict report", e);
            throw new RuntimeException("Failed to generate conflict report", e);
        }

        return report;
    }

    // Generate lecturer workload report
    public Map<String, Object> generateLecturerWorkloadReport(Department department, Semester semester) {
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
            report.put("semesterName", semester.getName());
            report.put("lecturerWorkloads", lecturerWorkload);
            report.put("totalLecturers", lecturers.size());

            // Calculate average workload
            double averageWorkload = lecturerWorkload.values().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            report.put("averageWorkload", averageWorkload);

            logger.info("Generated lecturer workload report for department: {}", department.getName());

        } catch (Exception e) {
            logger.error("Error generating lecturer workload report", e);
            throw new RuntimeException("Failed to generate workload report", e);
        }

        return report;
    }

    // Generate exam schedule report
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
