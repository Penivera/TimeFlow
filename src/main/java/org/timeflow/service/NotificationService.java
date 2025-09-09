package org.timeflow.service;

import org.timeflow.entity.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.timeflow.util.*;

public class NotificationService extends BaseService {

    private Session getEmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", Config.SMTP_HOST);
        props.put("mail.smtp.port", Config.SMTP_PORT);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Config.USERNAME, Config.EMAIL_PASSWORD);
            }
        });
    }
    public void sendTimetableToStudent(User student, List<Timetable> timetables) {
        try {
            String subject = "Your Current Timetable from TimeFlow";
            String emailBody = createStudentTimetableEmailBody(student, timetables);

            Session session = getEmailSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Config.SEND_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(student.getEmail()));
            message.setSubject(subject);
            message.setText(emailBody); // Using setText for plain text format

            Transport.send(message);
            logger.info("Sent timetable email to {}", student.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send timetable email to {}", student.getEmail(), e);
            throw new RuntimeException("Failed to send email. Please check your connection or configuration.", e);
        }
    }

    // --- ADD THIS HELPER METHOD for the email body ---
    private String createStudentTimetableEmailBody(User student, List<Timetable> timetables) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Dear %s,\n\nHere is your current approved timetable:\n\n", student.getUsername()));

        Map<DayOfWeek, List<Timetable>> groupedByDay = timetables.stream()
                .filter(t -> t.getDayOfWeek() != null)
                .collect(Collectors.groupingBy(Timetable::getDayOfWeek));
        groupedByDay.values().forEach(list -> list.sort((t1, t2) -> t1.getStartTime().compareTo(t2.getStartTime())));

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.getValue() > 5) continue;
            List<Timetable> daySchedules = groupedByDay.get(day);
            if (daySchedules != null && !daySchedules.isEmpty()) {
                sb.append("--- ").append(day.toString()).append(" ---\n");
                for (Timetable entry : daySchedules) {
                    sb.append(String.format("%s - %s | %s (%s) | Room: %s\n",
                            entry.getStartTime().format(timeFormatter),
                            entry.getEndTime().format(timeFormatter),
                            entry.getCourse().getName(),
                            entry.getCourse().getCode(),
                            entry.getRoom().getName()));
                }
                sb.append("\n");
            }
        }

        sb.append("Best regards,\nTimeFlow System");
        return sb.toString();
    }

    // In NotificationService.java

    public void notifyTimetableApproved(Timetable timetable) {
        try {
            List<User> students = daoFactory.getUserDAO()
                    .findStudentsByDepartmentAndLevel(
                            timetable.getCourse().getDepartment(),
                            timetable.getCourse().getLevel()
                    );
            Session session = getEmailSession();
            for (User student : students) {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(Config.SEND_FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(student.getEmail()));
                message.setSubject("Timetable Approved for " + timetable.getCourse().getName());
                message.setText(String.format(
                        "Dear %s,\n\nThe timetable for %s (%s) has been approved.\n\n" +
                                "Details:\nDay: %s\nTime: %s - %s\nRoom: %s\nType: %s\nSemester: %s\n\n" +
                                "Best regards,\nTimeFlow",
                        student.getUsername(),
                        timetable.getCourse().getName(),
                        timetable.getCourse().getCode(),
                        timetable.getSpecificDate() != null ? timetable.getSpecificDate() : timetable.getDayOfWeek(), // Handle single-day events
                        timetable.getStartTime(),
                        timetable.getEndTime(),
                        timetable.getRoom(),
                        timetable.getType(),
                        timetable.getSemester().toString() // MODIFIED: Changed .getName() to .toString()
                ));
                Transport.send(message);
                logger.info("Sent approval notification to {}", student.getEmail());
            }
        } catch (Exception e) {
            logger.error("Failed to send timetable approval notification", e);
        }
    }

    public void notifyConflicts(List<Conflict> conflicts) {
        try {
            Session session = getEmailSession();
            for (Conflict conflict : conflicts) {
                // Notify lecturer 1
                User lecturer1 = conflict.getTimetable1().getCourse().getLecturer();
                if (lecturer1 != null) {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(Config.SEND_FROM));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(lecturer1.getEmail()));
                    message.setSubject("Timetable Conflict Detected");
                    message.setText(createConflictEmailBody(conflict, lecturer1.getUsername()));
                    Transport.send(message);
                    logger.info("Sent conflict notification to {}", lecturer1.getEmail());
                }
                // Notify lecturer 2
                User lecturer2 = conflict.getTimetable2().getCourse().getLecturer();
                if (lecturer2 != null) {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(Config.SEND_FROM));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(lecturer2.getEmail()));
                    message.setSubject("Timetable Conflict Detected");
                    message.setText(createConflictEmailBody(conflict, lecturer2.getUsername()));
                    Transport.send(message);
                    logger.info("Sent conflict notification to {}", lecturer2.getEmail());
                }
                // Notify exams officer
                List<User> officers = daoFactory.getUserDAO().findByRole(UserRole.EXAMS_OFFICER);
                for (User officer : officers) {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(Config.SEND_FROM));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(officer.getEmail()));
                    message.setSubject("Timetable Conflict Requires Review");
                    message.setText(createConflictOfficerEmailBody(conflict));
                    Transport.send(message);
                    logger.info("Sent conflict notification to exams officer {}", officer.getEmail());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to send conflict notifications", e);
        }
    }

    public void notifyTimetableRejected(Timetable timetable, String reason) {
        try {
            User lecturer = timetable.getCourse().getLecturer();
            if (lecturer != null) {
                Session session = getEmailSession();
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(Config.SEND_FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(lecturer.getEmail()));
                message.setSubject("Timetable Rejected for " + timetable.getCourse().getName());
                message.setText(String.format(
                        "Dear %s,\n\nThe timetable for %s (%s) was rejected.\n\n" +
                                "Reason: %s\n\nPlease revise and resubmit.\n\n" +
                                "Best regards,\nTimeFlow",
                        lecturer.getUsername(),
                        timetable.getCourse().getName(),
                        timetable.getCourse().getCode(),
                        reason
                ));
                Transport.send(message);
                logger.info("Sent rejection notification to {}", lecturer.getEmail());
            }
        } catch (Exception e) {
            logger.error("Failed to send timetable rejection notification", e);
        }
    }

    String createConflictEmailBody(Conflict conflict, String perspective) {
        return String.format(
                "Dear %s,\n\nA scheduling conflict has been detected.\n\n" +
                        "Conflict Details:\n" +
                        "Type: %s\nDetected: %s\n\n" +
                        "Course 1: %s (%s)\nDay: %s | Time: %s - %s | Room: %s\nLecturer: %s\n\n" +
                        "Course 2: %s (%s)\nDay: %s | Time: %s - %s | Room: %s\nLecturer: %s\n\n" +
                        "Please coordinate with the other lecturer and the exams office to resolve this conflict.\n\n" +
                        "Best regards,\nTimeFlow",
                perspective,
                conflict.getType(),
                conflict.getDetectedAt(),
                conflict.getTimetable1().getCourse().getName(),
                conflict.getTimetable1().getCourse().getCode(),
                conflict.getTimetable1().getDayOfWeek(),
                conflict.getTimetable1().getStartTime(),
                conflict.getTimetable1().getEndTime(),
                conflict.getTimetable1().getRoom(),
                conflict.getTimetable1().getCourse().getLecturer().getUsername(),
                conflict.getTimetable2().getCourse().getName(),
                conflict.getTimetable2().getCourse().getCode(),
                conflict.getTimetable2().getDayOfWeek(),
                conflict.getTimetable2().getStartTime(),
                conflict.getTimetable2().getEndTime(),
                conflict.getTimetable2().getRoom(),
                conflict.getTimetable2().getCourse().getLecturer().getUsername()
        );
    }

    String createConflictOfficerEmailBody(Conflict conflict) {
        return String.format(
                "Dear Exams Officer,\n\nA timetable conflict requires your review.\n\n" +
                        "Conflict Summary:\nType: %s\nStatus: %s\nDetected: %s\n\n" +
                        "Conflicting Schedules:\n" +
                        "Course\tLecturer\tDay\tTime\tRoom\n" +
                        "%s (%s)\t%s\t%s\t%s - %s\t%s\n" +
                        "%s (%s)\t%s\t%s\t%s - %s\t%s\n\n" +
                        "Please log into TimeFlow to review and resolve this conflict.\n\n" +
                        "Best regards,\nTimeFlow",
                conflict.getType(),
                conflict.getStatus(),
                conflict.getDetectedAt(),
                conflict.getTimetable1().getCourse().getName(),
                conflict.getTimetable1().getCourse().getCode(),
                conflict.getTimetable1().getCourse().getLecturer().getUsername(),
                conflict.getTimetable1().getDayOfWeek(),
                conflict.getTimetable1().getStartTime(),
                conflict.getTimetable1().getEndTime(),
                conflict.getTimetable1().getRoom(),
                conflict.getTimetable2().getCourse().getName(),
                conflict.getTimetable2().getCourse().getCode(),
                conflict.getTimetable2().getCourse().getLecturer().getUsername(),
                conflict.getTimetable2().getDayOfWeek(),
                conflict.getTimetable2().getStartTime(),
                conflict.getTimetable2().getEndTime(),
                conflict.getTimetable2().getRoom()
        );
    }

    String createCompleteTimetableEmailBody(List<Timetable> timetables, Semester semester, Department department, int level) {
        StringBuilder tableRows = new StringBuilder();
        for (Timetable tt : timetables) {
            tableRows.append(String.format(
                    "%s\t%s (%s)\t%s - %s\t%s\t%s\n",
                    tt.getDayOfWeek(),
                    tt.getCourse().getName(),
                    tt.getCourse().getCode(),
                    tt.getStartTime(),
                    tt.getEndTime(),
                    tt.getRoom(),
                    tt.getType()
            ));
        }
        return String.format(
                "Dear Student,\n\nPlease find below your complete timetable for %s %s, Level %d.\n\n" +
                        "Day\tCourse\tTime\tRoom\tType\n%s\n\n" +
                        "Important Notes:\n" +
                        "- This timetable is now official and approved by the exams office\n" +
                        "- Please save this email for your records\n" +
                        "- Any changes will be communicated separately\n" +
                        "- Ensure you attend all scheduled classes and examinations\n\n" +
                        "For any questions or concerns, please contact the exams office.\n\n" +
                        "Best regards,\nTimeFlow\n%s",
                department.getName(),
                semester.getName(),
                level,
                tableRows.toString(),
                department.getName()
        );
    }

    // In NotificationService.java

    public void notifyLecturersToResolve(Conflict conflict) {
        try {
            User lecturer1 = conflict.getTimetable1().getCourse().getLecturer();
            User lecturer2 = conflict.getTimetable2().getCourse().getLecturer();

            if (lecturer1 == null || lecturer2 == null) {
                logger.warn("Could not send resolution email; one or both lecturers are null for conflict ID {}", conflict.getId());
                return;
            }

            String subject = "Action Required: Please Resolve Timetable Conflict";
            String emailBody = createLecturerResolutionEmailBody(conflict, lecturer1, lecturer2);

            Session session = getEmailSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Config.SEND_FROM));
            // Send the email to both lecturers
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(lecturer1.getEmail() + "," + lecturer2.getEmail()));
            message.setSubject(subject);
            message.setText(emailBody);

            Transport.send(message);
            logger.info("Sent conflict resolution email to {} and {} for conflict ID {}", lecturer1.getEmail(), lecturer2.getEmail(), conflict.getId());

        } catch (Exception e) {
            logger.error("Failed to send conflict resolution email for conflict ID {}", conflict.getId(), e);
            // We don't re-throw the exception here to avoid crashing the UI for a non-critical email failure.
        }
    }

    // Helper method to create the email body
    private String createLecturerResolutionEmailBody(Conflict conflict, User lec1, User lec2) {
        Timetable t1 = conflict.getTimetable1();
        Timetable t2 = conflict.getTimetable2();

        return String.format(
                "Dear %s and %s,\n\nA timetable conflict has been identified that requires your coordination to resolve.\n\n" +
                        "Conflict Type: %s\n\n" +
                        "--- Schedule 1 ---\n" +
                        "Course: %s (%s)\n" +
                        "Lecturer: %s\n" +
                        "Time: %s, %s - %s\n" +
                        "Room: %s\n\n" +
                        "--- Schedule 2 ---\n" +
                        "Course: %s (%s)\n" +
                        "Lecturer: %s\n" +
                        "Time: %s, %s - %s\n" +
                        "Room: %s\n\n" +
                        "Please discuss and decide which schedule needs to be modified. " +
                        "Once you have agreed on a solution, one of you should resubmit the corrected schedule.\n\n" +
                        "Thank you,\nTimeFlow Administration",
                lec1.getUsername(), lec2.getUsername(),
                conflict.getType(),
                t1.getCourse().getName(), t1.getCourse().getCode(), lec1.getUsername(),
                t1.getDayOfWeek() != null ? t1.getDayOfWeek() : t1.getSpecificDate(), t1.getStartTime(), t1.getEndTime(),
                t1.getRoom().getName(),
                t2.getCourse().getName(), t2.getCourse().getCode(), lec2.getUsername(),
                t2.getDayOfWeek() != null ? t2.getDayOfWeek() : t2.getSpecificDate(), t2.getStartTime(), t2.getEndTime(),
                t2.getRoom().getName()
        );
    }

    // --- ADD THIS NEW METHOD ---
    public void sendAdminInquiryToLecturer(User admin, User lecturer, Timetable timetable, String message) {
        try {
            String subject = "Inquiry regarding your schedule for " + timetable.getCourse().getCode();
            String emailBody = createInquiryEmailBody(admin, lecturer, timetable, message);

            Session session = getEmailSession();
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(Config.SEND_FROM));
            // Send to the lecturer
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(lecturer.getEmail()));
            // CC the admin for their records
            mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(admin.getEmail()));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(emailBody);

            Transport.send(mimeMessage);
            logger.info("Sent inquiry from {} to {} regarding timetable ID {}", admin.getUsername(), lecturer.getUsername(), timetable.getId());

        } catch (Exception e) {
            logger.error("Failed to send inquiry email for timetable ID {}", timetable.getId(), e);
            // We don't re-throw to avoid crashing the UI for a non-critical email failure
        }
    }
    private String createInquiryEmailBody(User admin, User lecturer, Timetable timetable, String message) {
        return String.format(
                "Dear %s,\n\n" +
                        "%s from the administrative office has sent an inquiry regarding one of your scheduled classes:\n\n" +
                        "--- Schedule Details ---\n" +
                        "Course: %s (%s)\n" +
                        "Time: %s, %s - %s\n" +
                        "Room: %s\n\n" +
                        "--- Message ---\n" +
                        "%s\n\n" +
                        "Please review and follow up if necessary. You can reply to this email to respond to %s.\n\n" +
                        "Thank you,\nTimeFlow System",
                lecturer.getUsername(),
                admin.getUsername(),
                timetable.getCourse().getName(), timetable.getCourse().getCode(),
                timetable.getDayOfWeek() != null ? timetable.getDayOfWeek() : timetable.getSpecificDate(),
                timetable.getStartTime(), timetable.getEndTime(),
                timetable.getRoom().getName(),
                message,
                admin.getUsername()
        );
    }
}