package org.timeflow.service;
import org.timeflow.entity.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;
import java.util.Properties;
import org.timeflow.util.*;

public class NotificationService extends BaseService {

    // Email configuration
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

    // Notify students about approved timetable
    public void notifyTimetableApproved(Timetable timetable) {
        try {
            // Get all students in the department and level
            List<User> students = daoFactory.getUserDAO()
                    .findStudentsByDepartmentAndLevel(
                            timetable.getCourse().getDepartment(),
                            timetable.getCourse().getLevel()
                    );
        } finally {

        }
    }

        String createConflictEmailBody(Conflict conflict, String perspective){
            return String.format("""
                            <html>
                            <body>
                                <h2>Timetable Conflict Detected</h2>
                                <p>Dear Lecturer,</p>
                                <p>A scheduling conflict has been detected involving %s.</p>
                            
                                <div style="border: 1px solid #ffcc99; padding: 10px; margin: 10px 0; background-color: #fff8f0;">
                                    <h3>Conflict Details:</h3>
                                    <p><strong>Conflict Type:</strong> %s</p>
                                    <p><strong>Detected:</strong> %s</p>
                                </div>
                            
                                <div style="border: 1px solid #ccc; padding: 10px; margin: 10px 0;">
                                    <h3>Course 1: %s (%s)</h3>
                                    <p><strong>Day:</strong> %s | <strong>Time:</strong> %s - %s | <strong>Room:</strong> %s</p>
                                    <p><strong>Lecturer:</strong> %s</p>
                                </div>
                            
                                <div style="border: 1px solid #ccc; padding: 10px; margin: 10px 0;">
                                    <h3>Course 2: %s (%s)</h3>
                                    <p><strong>Day:</strong> %s | <strong>Time:</strong> %s - %s | <strong>Room:</strong> %s</p>
                                    <p><strong>Lecturer:</strong> %s</p>
                                </div>
                            
                                <p>Please coordinate with the other lecturer and the exams office to resolve this conflict.</p>
                                <p>You can appeal this conflict if you believe there's an error or special arrangement.</p>
                            
                                <p>Best regards,<br>
                                TimeFlow - Academic Scheduling System</p>
                            </body>
                            </html>
                            """,
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

        String createConflictOfficerEmailBody (Conflict conflict){
            return String.format("""
                            <html>
                            <body>
                                <h2>Timetable Conflict Requires Review</h2>
                                <p>Dear Exams Officer,</p>
                                <p>A timetable conflict has been detected and requires your review.</p>
                            
                                <div style="border: 1px solid #ff9999; padding: 10px; margin: 10px 0; background-color: #fff0f0;">
                                    <h3>Conflict Summary:</h3>
                                    <p><strong>Type:</strong> %s</p>
                                    <p><strong>Status:</strong> %s</p>
                                    <p><strong>Detected:</strong> %s</p>
                                </div>
                            
                                <div style="border: 1px solid #ccc; padding: 10px; margin: 10px 0;">
                                    <h3>Conflicting Schedules:</h3>
                                    <table border="1" style="border-collapse: collapse; width: 100%%;">
                                        <tr style="background-color: #f0f0f0;">
                                            <th>Course</th>
                                            <th>Lecturer</th>
                                            <th>Day</th>
                                            <th>Time</th>
                                            <th>Room</th>
                                        </tr>
                                        <tr>
                                            <td>%s (%s)</td>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%s - %s</td>
                                            <td>%s</td>
                                        </tr>
                                        <tr>
                                            <td>%s (%s)</td>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%s - %s</td>
                                            <td>%s</td>
                                        </tr>
                                    </table>
                                </div>
                            
                                <p>Please log into TimeFlow to review and resolve this conflict.</p>
                            
                                <p>Best regards,<br>
                                TimeFlow - Academic Scheduling System</p>
                            </body>
                            </html>
                            """,
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

        String createCompleteTimetableEmailBody (List < Timetable > timetables, Semester semester, Department department,
        int level){
            StringBuilder tableRows = new StringBuilder();

            for (Timetable tt : timetables) {
                tableRows.append(String.format("""
                                <tr>
                                    <td>%s</td>
                                    <td>%s (%s)</td>
                                    <td>%s</td>
                                    <td>%s - %s</td>
                                    <td>%s</td>
                                    <td>%s</td>
                                </tr>
                                """,
                        tt.getDayOfWeek(),
                        tt.getCourse().getName(),
                        tt.getCourse().getCode(),
                        tt.getStartTime(),
                        tt.getEndTime(),
                        tt.getRoom(),
                        tt.getType()
                ));
            }

            return String.format("""
                            <html>
                            <body>
                                <h2>Complete Timetable - %s</h2>
                                <p>Dear Student,</p>
                                <p>Please find below your complete timetable for <strong>%s %s, Level %d</strong>.</p>
                            
                                <table border="1" style="border-collapse: collapse; width: 100%%; margin: 20px 0;">
                                    <tr style="background-color: #f0f0f0;">
                                        <th>Day</th>
                                        <th>Course</th>
                                        <th>Time</th>
                                        <th>Room</th>
                                        <th>Type</th>
                                    </tr>
                                    %s
                                </table>
                            
                                <div style="border: 1px solid #ccffcc; padding: 10px; margin: 10px 0; background-color: #f0fff0;">
                                    <h3>Important Notes:</h3>
                                    <ul>
                                        <li>This timetable is now official and approved by the exams office</li>
                                        <li>Please save this email for your records</li>
                                        <li>Any changes will be communicated separately</li>
                                        <li>Ensure you attend all scheduled classes and examinations</li>
                                    </ul>
                                </div>
                            
                                <p>For any questions or concerns, please contact the exams office.</p>
                            
                                <p>Best regards,<br>
                                TimeFlow - Academic Scheduling System<br>
                                %s</p>
                            </body>
                            </html>
                            """,
                    semester.getName(),
                    department.getName(),
                    semester.getName(),
                    level,
                    tableRows.toString(),
                    department.getName()
            );
        }

        public void notifyConflicts(List<Conflict> conflicts) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'notifyConflicts'");
        }

        public void notifyTimetableRejected(Timetable timetable, String reason) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'notifyTimetableRejected'");
        }
    }

