package org.timeflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.dao.TimetableDAO;
import org.timeflow.dao.UserDAO;
import org.timeflow.entity.*;
import org.timeflow.util.Config;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for sending automated email reminders for upcoming classes
 */
public class ScheduledReminderService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledReminderService.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final NotificationService notificationService;
    private final TimetableDAO timetableDAO;
    private final UserDAO userDAO;

    public ScheduledReminderService() {
        this.notificationService = new NotificationService();
        this.timetableDAO = new TimetableDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Starts the scheduled reminder service
     * Runs daily at 8:00 AM to send reminders for the day
     */
    public void startReminderScheduler() {
        // Calculate initial delay to next 8:00 AM
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next8AM = now.toLocalDate().atTime(8, 0);
        if (now.isAfter(next8AM)) {
            next8AM = next8AM.plusDays(1);
        }
        
        long initialDelay = ChronoUnit.MINUTES.between(now, next8AM);
        
        // Schedule daily reminders at 8:00 AM
        scheduler.scheduleAtFixedRate(
            this::sendDailyReminders,
            initialDelay,
            TimeUnit.DAYS.toMinutes(1), // Run every 24 hours
            TimeUnit.MINUTES
        );
        
        logger.info("Scheduled reminder service started. Next run in {} minutes at {}", initialDelay, next8AM);
    }

    /**
     * Stops the scheduled reminder service
     */
    public void stopReminderScheduler() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("Scheduled reminder service stopped");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sends daily reminders for today's classes
     */
    private void sendDailyReminders() {
        try {
            logger.info("Starting daily reminder process...");
            LocalDate today = LocalDate.now();
            DayOfWeek dayOfWeek = today.getDayOfWeek();
            
            // Skip weekends
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                logger.info("Skipping reminders for weekend day: {}", dayOfWeek);
                return;
            }

            // Get all active users
            List<User> users = userDAO.findAll().stream()
                    .filter(User::isActive)
                    .collect(Collectors.toList());

            int remindersSent = 0;
            for (User user : users) {
                // Get today's timetable for user
                List<Timetable> todaysTimetable = getTodaysTimetable(user, dayOfWeek, today);
                
                if (!todaysTimetable.isEmpty()) {
                    sendDailyReminderEmail(user, todaysTimetable, today);
                    remindersSent++;
                }
            }
            
            logger.info("Daily reminders completed. Sent {} reminders", remindersSent);
            
        } catch (Exception e) {
            logger.error("Error sending daily reminders", e);
        }
    }

    /**
     * Gets today's timetable for a user
     */
    private List<Timetable> getTodaysTimetable(User user, DayOfWeek dayOfWeek, LocalDate date) {
        List<Timetable> allTimetables = timetableDAO.findAll();
        
        return allTimetables.stream()
                .filter(t -> t.getStatus() == TimetableStatus.APPROVED)
                .filter(t -> {
                    // Check if user is related to this timetable
                    // For students: check if they're in the same department and level
                    if (user.getRole() == UserRole.STUDENT) {
                        return t.getCourse().getDepartment().equals(user.getDepartment()) &&
                               t.getCourse().getLevel() == user.getLevel();
                    }
                    // For lecturers: check if they're teaching this course
                    else if (user.getRole() == UserRole.LECTURER) {
                        return t.getCourse().getLecturer() != null &&
                               t.getCourse().getLecturer().getId().equals(user.getId());
                    }
                    return false;
                })
                .filter(t -> {
                    // Check if timetable is for today
                    if (t.getSpecificDate() != null) {
                        return t.getSpecificDate().equals(date);
                    } else if (t.getDayOfWeek() != null) {
                        return t.getDayOfWeek().equals(dayOfWeek);
                    }
                    return false;
                })
                .sorted(Comparator.comparing(Timetable::getStartTime))
                .collect(Collectors.toList());
    }

    /**
     * Sends a daily reminder email to a user
     */
    private void sendDailyReminderEmail(User user, List<Timetable> timetables, LocalDate date) {
        try {
            String subject = "TimeFlow: Your Class Schedule for Today - " + date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
            String emailBody = createDailyReminderEmailBody(user, timetables, date);

            Session session = getEmailSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Config.SEND_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
            message.setSubject(subject);
            message.setText(emailBody);

            Transport.send(message);
            logger.debug("Sent daily reminder to {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send daily reminder to {}", user.getEmail(), e);
        }
    }

    /**
     * Sends reminder for an upcoming class (1 hour before)
     */
    public void sendUpcomingClassReminder(User user, Timetable timetable) {
        try {
            String subject = "Reminder: Class Starting Soon - " + timetable.getCourse().getName();
            String emailBody = createUpcomingClassReminderBody(user, timetable);

            Session session = getEmailSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Config.SEND_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
            message.setSubject(subject);
            message.setText(emailBody);

            Transport.send(message);
            logger.info("Sent upcoming class reminder to {} for {}", user.getEmail(), timetable.getCourse().getCode());
            
        } catch (Exception e) {
            logger.error("Failed to send upcoming class reminder to {}", user.getEmail(), e);
        }
    }

    /**
     * Creates the email body for daily reminder
     */
    private String createDailyReminderEmailBody(User user, List<Timetable> timetables, LocalDate date) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Good morning %s,\n\n", user.getUsername()));
        sb.append("Here's your schedule for today, ").append(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))).append(":\n\n");

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        
        for (Timetable timetable : timetables) {
            sb.append("• ").append(timetable.getStartTime().format(timeFormatter))
              .append(" - ").append(timetable.getEndTime().format(timeFormatter))
              .append("\n  ").append(timetable.getCourse().getName())
              .append(" (").append(timetable.getCourse().getCode()).append(")")
              .append("\n  Room: ").append(timetable.getRoom().getName())
              .append(" | Type: ").append(timetable.getType());
            
            if (user.getRole() == UserRole.STUDENT && timetable.getCourse().getLecturer() != null) {
                sb.append("\n  Lecturer: ").append(timetable.getCourse().getLecturer().getUsername());
            }
            
            sb.append("\n\n");
        }

        sb.append("Have a great day!\n\n");
        sb.append("---\n");
        sb.append("TimeFlow - Timetable Management System\n");
        sb.append("This is an automated reminder. Please do not reply to this email.\n");

        return sb.toString();
    }

    /**
     * Creates the email body for upcoming class reminder
     */
    private String createUpcomingClassReminderBody(User user, Timetable timetable) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Hi %s,\n\n", user.getUsername()));
        sb.append("This is a reminder that your class is starting soon!\n\n");

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        
        sb.append("Class: ").append(timetable.getCourse().getName())
          .append(" (").append(timetable.getCourse().getCode()).append(")\n");
        sb.append("Time: ").append(timetable.getStartTime().format(timeFormatter))
          .append(" - ").append(timetable.getEndTime().format(timeFormatter)).append("\n");
        sb.append("Room: ").append(timetable.getRoom().getName()).append("\n");
        sb.append("Type: ").append(timetable.getType()).append("\n");
        
        if (user.getRole() == UserRole.STUDENT && timetable.getCourse().getLecturer() != null) {
            sb.append("Lecturer: ").append(timetable.getCourse().getLecturer().getUsername()).append("\n");
        }

        sb.append("\nSee you there!\n\n");
        sb.append("---\n");
        sb.append("TimeFlow - Timetable Management System\n");

        return sb.toString();
    }

    /**
     * Gets email session configuration
     */
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
}
