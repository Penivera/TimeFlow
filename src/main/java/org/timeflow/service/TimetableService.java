package org.timeflow.service;

import org.timeflow.entity.*;
import java.time.LocalDateTime;
import java.util.List;

public class TimetableService extends BaseService {

    private final ConflictDetectionService conflictService;
    private final NotificationService notificationService;

    public TimetableService() {
        super();
        this.conflictService = new ConflictDetectionService();
        this.notificationService = new NotificationService();
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

    // Update existing timetable
    public Timetable updateTimetable(Timetable timetable, User updatedBy) {
        try {
            // Re-detect conflicts for updated timetable
            List<Conflict> conflicts = conflictService.detectConflicts(timetable);

            if (!conflicts.isEmpty()) {
                timetable.setStatus(TimetableStatus.CONFLICTED);
                conflictService.saveConflicts(conflicts);
                notificationService.notifyConflicts(conflicts);
            } else if (timetable.getStatus() == TimetableStatus.CONFLICTED) {
                // Was conflicted but now resolved
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

            // Notify students about approved timetable
            notificationService.notifyTimetableApproved(timetable);

            logger.info("Timetable approved by {}: {}", approvedBy.getUsername(), timetableId);

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
    public List<Timetable> getStudentTimetables(User student) {
        try {
            return daoFactory.getTimetableDAO().findApprovedTimetables(
                    student.getDepartment(),
                    getCurrentLevel(student), // You'll need to implement this method
                    daoFactory.getSemesterDAO().getCurrentSemester());
        } catch (Exception e) {
            logger.error("Error getting student timetables for: {}", student.getUsername(), e);
            throw new RuntimeException("Failed to get student timetables", e);
        }
    }

    // Get timetables for lecturer
    public List<Timetable> getLecturerTimetables(User lecturer) {
        try {
            return daoFactory.getTimetableDAO().findByLecturer(
                    lecturer,
                    daoFactory.getSemesterDAO().getCurrentSemester());
        } catch (Exception e) {
            logger.error("Error getting lecturer timetables for: {}", lecturer.getUsername(), e);
            throw new RuntimeException("Failed to get lecturer timetables", e);
        }
    }

    // Helper methods
    private boolean canApproveTimetables(User user) {
        return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.EXAMS_OFFICER;
    }

    private int getCurrentLevel(User student) {
        // Implementation depends on your business logic
        // This could be stored in user profile or calculated based on enrollment date
        return 1; // Placeholder
    }
}
