package org.timeflow.service;

import org.timeflow.entity.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;
import java.util.Properties;
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
                return new PasswordAuthentication(Config.EMAIL, Config.EMAIL_PASSWORD);
            }
        });
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
                message.setFrom(new InternetAddress(Config.EMAIL));
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
                    message.setFrom(new InternetAddress(Config.EMAIL));
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
                    message.setFrom(new InternetAddress(Config.EMAIL));
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
                    message.setFrom(new InternetAddress(Config.EMAIL));
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
                message.setFrom(new InternetAddress(Config.EMAIL));
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
}