package org.timeflow.service;

import org.timeflow.entity.*;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

public class ConflictDetectionService extends BaseService {

    // Main conflict detection method
    public List<Conflict> detectConflicts(Timetable newTimetable) {
        List<Conflict> conflicts = new ArrayList<>();

        try {
            // 1. Check time and room conflicts
            List<Timetable> roomConflicts = daoFactory.getTimetableDAO()
                    .findRoomConflicts(
                            newTimetable.getRoom(), // Pass the Room object
                            newTimetable.getDayOfWeek(),
                            newTimetable.getStartTime(),
                            newTimetable.getEndTime(),
                            newTimetable.getSemester(),
                            newTimetable.getId()
                    );
            for (Timetable conflictingTimetable : roomConflicts) {
                ConflictType conflictType = determineConflictType(newTimetable, conflictingTimetable);
                if (conflictType != null) {
                    Conflict conflict = new Conflict(newTimetable, conflictingTimetable, conflictType);
                    conflicts.add(conflict);
                }
            }

            // 2. Check lecturer conflicts
            if (newTimetable.getCourse().getLecturer() != null) {
                List<Timetable> lecturerConflicts = daoFactory.getTimetableDAO()
                        .findLecturerConflicts(
                                newTimetable.getCourse().getLecturer(),
                                newTimetable.getDayOfWeek(),
                                newTimetable.getStartTime(),
                                newTimetable.getEndTime(),
                                newTimetable.getSemester(),
                                newTimetable.getId()
                        );

                for (Timetable conflictingTimetable : lecturerConflicts) {
                    // Check if this conflict already exists in our list
                    if (!conflictExists(conflicts, newTimetable, conflictingTimetable)) {
                        Conflict conflict = new Conflict(newTimetable, conflictingTimetable, ConflictType.LECTURER_CONFLICT);
                        conflicts.add(conflict);
                    }
                }
            }

            // 3. Check borrowed course conflicts
            conflicts.addAll(checkBorrowedCourseConflicts(newTimetable));

            logger.info("Detected {} conflicts for timetable ID: {}", conflicts.size(), newTimetable.getId());

        } catch (Exception e) {
            logger.error("Error detecting conflicts for timetable", e);
        }

        return conflicts;
    }

    // Determine a conflict type based on two timetables
    private ConflictType determineConflictType(Timetable t1, Timetable t2) {
        // Check room conflict
        if (t1.getRoom().getId().equals(t2.getRoom().getId()) && timesOverlap(t1, t2)) {
            return ConflictType.ROOM_CONFLICT;
        }

        // Check lecturer conflict
        if (t1.getCourse().getLecturer() != null && t2.getCourse().getLecturer() != null &&
                t1.getCourse().getLecturer().equals(t2.getCourse().getLecturer()) && timesOverlap(t1, t2)) {
            return ConflictType.LECTURER_CONFLICT;
        }

        // Check time conflict (same department, different courses)
        if (t1.getCourse().getDepartment().equals(t2.getCourse().getDepartment()) &&
                t1.getCourse().getLevel() == t2.getCourse().getLevel() && timesOverlap(t1, t2)) {
            return ConflictType.TIME_CONFLICT;
        }

        return null;
    }

    // Check if times overlap
    private boolean timesOverlap(Timetable t1, Timetable t2) {
        return t1.getDayOfWeek() == t2.getDayOfWeek() &&
                t1.getStartTime().isBefore(t2.getEndTime()) &&
                t1.getEndTime().isAfter(t2.getStartTime());
    }

    // Check borrowed course conflicts
    private List<Conflict> checkBorrowedCourseConflicts(Timetable newTimetable) {
        List<Conflict> borrowedConflicts = new ArrayList<>();

        try {
            // Find all departments that might have borrowed courses
            List<Department> allDepartments = daoFactory.getDepartmentDAO().findAll();

            for (Department dept : allDepartments) {
                if (!dept.equals(newTimetable.getCourse().getDepartment())) {
                    // Check if this department has borrowed courses from newTimetable's department
                    List<Course> borrowedCourses = daoFactory.getCourseDAO().findBorrowedCourses(dept);

                    for (Course borrowedCourse : borrowedCourses) {
                        if (borrowedCourse.getDepartment().equals(newTimetable.getCourse().getDepartment())) {
                            // Check conflicts with this borrowed course's timetables
                            List<Timetable> borrowedTimetables = daoFactory.getTimetableDAO()
                                    .findByCourse(borrowedCourse);

                            for (Timetable borrowedTimetable : borrowedTimetables) {
                                if (timesOverlap(newTimetable, borrowedTimetable)) {
                                    Conflict conflict = new Conflict(newTimetable, borrowedTimetable,
                                            ConflictType.BORROWED_COURSE_CONFLICT);
                                    borrowedConflicts.add(conflict);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error checking borrowed course conflicts", e);
        }

        return borrowedConflicts;
    }

    // Check if conflict already exists in the list
    private boolean conflictExists(List<Conflict> conflicts, Timetable t1, Timetable t2) {
        return conflicts.stream().anyMatch(c ->
                (c.getTimetable1().equals(t1) && c.getTimetable2().equals(t2)) ||
                        (c.getTimetable1().equals(t2) && c.getTimetable2().equals(t1))
        );
    }

    // Save detected conflicts
    public void saveConflicts(List<Conflict> conflicts) {
        for (Conflict conflict : conflicts) {
            // Check if conflict already exists in database
            Conflict existingConflict = daoFactory.getConflictDAO()
                    .findExistingConflict(conflict.getTimetable1(), conflict.getTimetable2());

            if (existingConflict == null) {
                daoFactory.getConflictDAO().save(conflict);
            }
        }
    }

    // Resolve conflict
    public void resolveConflict(Long conflictId, String resolution, User resolvedBy) {
        try {
            Conflict conflict = daoFactory.getConflictDAO().findById(conflictId);
            if (conflict != null) {
                conflict.setStatus(ConflictStatus.RESOLVED);
                conflict.setResolutionNotes(resolution);
                conflict.setResolvedBy(resolvedBy);
                conflict.setResolvedAt(java.time.LocalDateTime.now());
                daoFactory.getConflictDAO().update(conflict);

                logger.info("Conflict resolved by {}: {}", resolvedBy.getUsername(), conflictId);
            }
        } catch (Exception e) {
            logger.error("Error resolving conflict: {}", conflictId, e);
            throw new RuntimeException("Failed to resolve conflict", e);
        }
    }
}
