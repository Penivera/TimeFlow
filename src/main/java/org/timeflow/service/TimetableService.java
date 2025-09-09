package org.timeflow.service;

import org.timeflow.entity.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


public class TimetableService extends BaseService {

    private final ConflictDetectionService conflictService;
    private final NotificationService notificationService;

    public TimetableService() {
        super();
        this.conflictService = new ConflictDetectionService();
        this.notificationService = new NotificationService();
    }
    public List<Timetable> getDepartmentalTimetables(Department department) {
        if (department == null) {
            logger.warn("Attempted to get departmental timetables for a null department.");
            return Collections.emptyList();
        }
        try {
            return daoFactory.getTimetableDAO().findAllByDepartment(department);
        } catch (Exception e) {
            logger.error("Error getting timetables for department: {}", department.getName(), e);
            return Collections.emptyList();
        }
    }

    // Create new timetable entry
    public Timetable createTimetable(Timetable timetable, User createdBy) {
        try {
            // Set initial status
            timetable.setStatus(TimetableStatus.DRAFT);
            timetable.setCreatedAt(LocalDateTime.now());

            // Save timetable
            Timetable savedTimetable = daoFactory.getTimetableDAO().save(timetable);

            // Detect conflicts
            List<Conflict> conflicts = conflictService.detectConflicts(savedTimetable);

            if (!conflicts.isEmpty()) {
                // Set status to conflicted and save conflicts
                savedTimetable.setStatus(TimetableStatus.CONFLICTED);
                daoFactory.getTimetableDAO().update(savedTimetable);
                conflictService.saveConflicts(conflicts);

                // Notify affected parties
                notificationService.notifyConflicts(conflicts);

                logger.warn("Timetable created with {} conflicts: {}", conflicts.size(), savedTimetable.getId());
            } else {
                // No conflicts, set to pending approval
                savedTimetable.setStatus(TimetableStatus.PENDING_APPROVAL);
                daoFactory.getTimetableDAO().update(savedTimetable);

                logger.info("Timetable created successfully: {}", savedTimetable.getId());
            }

            return savedTimetable;

        } catch (Exception e) {
            logger.error("Error creating timetable", e);
            throw new RuntimeException("Failed to create timetable", e);
        }
    }

    // Approve timetable
    public void approveTimetable(Long timetableId, User approvedBy) {
        try {
            Timetable timetable = daoFactory.getTimetableDAO().findById(timetableId);
            if (timetable == null) {
                throw new RuntimeException("Timetable not found");
            }

            // Check if user has approval permissions
            if (!canApproveTimetables(approvedBy)) {
                throw new RuntimeException("User does not have approval permissions");
            }

            // Check for unresolved conflicts
            List<Conflict> conflicts = daoFactory.getConflictDAO().findByTimetable(timetable);
            boolean hasUnresolvedConflicts = conflicts.stream()
                    .anyMatch(
                            c -> c.getStatus() == ConflictStatus.DETECTED || c.getStatus() == ConflictStatus.APPEALED);

            if (hasUnresolvedConflicts) {
                throw new RuntimeException("Cannot approve timetable with unresolved conflicts");
            }

            timetable.setStatus(TimetableStatus.APPROVED);
            timetable.setApprovedAt(LocalDateTime.now());
            timetable.setApprovedBy(approvedBy);

            daoFactory.getTimetableDAO().update(timetable);
            logger.info("Timetable approved by {}: {}", approvedBy.getUsername(), timetableId);

            try {
                notificationService.notifyTimetableApproved(timetable);
            } catch (Exception emailException) {
                logger.error("APPROVAL SUCCEEDED, but failed to send notification emails for timetable ID {}: {}",
                        timetableId, emailException.getMessage());
                // We re-throw this as a specific, non-critical exception so the UI can optionally report it
                throw new RuntimeException("Approval was successful, but notification emails could not be sent. Please check your network or email configuration.", emailException);
            }


        } catch (RuntimeException e) {

            logger.error("Error approving timetable: {}", timetableId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error approving timetable: {}", timetableId, e);
            throw new RuntimeException("Failed to approve timetable", e);
        }
    }

    // Reject timetable
    public void rejectTimetable(Long timetableId, String reason, User rejectedBy) {
        try {
            Timetable timetable = daoFactory.getTimetableDAO().findById(timetableId);
            if (timetable == null) {
                throw new RuntimeException("Timetable not found");
            }

            timetable.setStatus(TimetableStatus.REJECTED);
            timetable.setDescription(reason);

            daoFactory.getTimetableDAO().update(timetable);

            // Notify lecturer about rejection
            notificationService.notifyTimetableRejected(timetable, reason);

            logger.info("Timetable rejected by {}: {}", rejectedBy.getUsername(), timetableId);

        } catch (Exception e) {
            logger.error("Error rejecting timetable: {}", timetableId, e);
            throw new RuntimeException("Failed to reject timetable", e);
        }
    }

    // Get timetables for student view
    // In TimetableService.java, replace the getStudentTimetables method

    public List<Timetable> getStudentTimetables(User student) {
        try {
            // Simple logic to determine current semester
            int month = java.time.LocalDate.now().getMonthValue();
            SemesterType currentSemester = (month >= 2 && month <= 7) ? SemesterType.SECOND_SEMESTER : SemesterType.FIRST_SEMESTER;

            return daoFactory.getTimetableDAO().findApprovedTimetables(
                    student.getDepartment(),
                    getCurrentLevel(student), // You'll need to implement this method
                    currentSemester);
        } catch (Exception e) {
            logger.error("Error getting student timetables for: {}", student.getUsername(), e);
            throw new RuntimeException("Failed to get student timetables", e);
        }
    }

    // Get timetables for lecturer
    // In TimetableService.java

    public List<Timetable> getLecturerTimetables(User lecturer) {
        try {
            // --- MODIFIED: Added logic to determine the current semester ---
            // This mirrors the logic we used in getStudentTimetables
            int month = java.time.LocalDate.now().getMonthValue();
            SemesterType currentSemester = (month >= 2 && month <= 7) ? SemesterType.SECOND_SEMESTER : SemesterType.FIRST_SEMESTER;

            return daoFactory.getTimetableDAO().findByLecturer(
                    lecturer,
                    currentSemester); // Pass the determined current semester
        } catch (Exception e) {
            logger.error("Error getting lecturer timetables for: {}", lecturer.getUsername(), e);
            throw new RuntimeException("Failed to get lecturer timetables", e);
        }
    }

    public List<Timetable> getAllTimetables() {
        try {
            return daoFactory.getTimetableDAO().findAll();
        } catch (Exception e) {
            logger.error("Error getting all timetables", e);
            return Collections.emptyList();
        }
    }

    // Helper methods
    private boolean canApproveTimetables(User user) {
        return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.EXAMS_OFFICER;
    }

    private int getCurrentLevel(User student) {
        if (student != null && student.getRole() == UserRole.STUDENT) {
            return student.getLevel();
        }
        // Return a default value for non-students or if student is null
        return 0;
    }
    // In TimetableService.java
    public Timetable updateTimetable(Timetable timetable, User updatedBy) {
        try {
            List<Conflict> conflicts = conflictService.detectConflicts(timetable);
            if (!conflicts.isEmpty()) {
                timetable.setStatus(TimetableStatus.CONFLICTED);
                conflictService.saveConflicts(conflicts);
                notificationService.notifyConflicts(conflicts);
            } else if (timetable.getStatus() == TimetableStatus.CONFLICTED) {
                timetable.setStatus(TimetableStatus.PENDING_APPROVAL);
            }
            Timetable updatedTimetable = daoFactory.getTimetableDAO().update(timetable);
            logger.info("Timetable updated by {}: {}", updatedBy.getUsername(), timetable.getId());
            return updatedTimetable;
        } catch (Exception e) {
            logger.error("Error updating timetable: {}", timetable.getId(), e);
            throw new RuntimeException("Failed to update timetable", e);
        }
    }

    public void deleteTimetable(Long timetableId) {
        try {
            Timetable timetableToDelete = daoFactory.getTimetableDAO().findById(timetableId);
            if (timetableToDelete != null) {
                List<Conflict> relatedConflicts = daoFactory.getConflictDAO().findByTimetable(timetableToDelete);
                for (Conflict conflict : relatedConflicts) {
                    daoFactory.getConflictDAO().delete(conflict);
                    logger.info("Deleted associated conflict with ID: {}", conflict.getId());
                }
                daoFactory.getTimetableDAO().deleteById(timetableId);
                logger.info("Successfully deleted timetable with ID: {}", timetableId);
            }
        } catch (Exception e) {
            logger.error("Error deleting timetable with ID: {}", timetableId, e);
            throw new RuntimeException("Failed to delete timetable", e);
        }
    }

}
