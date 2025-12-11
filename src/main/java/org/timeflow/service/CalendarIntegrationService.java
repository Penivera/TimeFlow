package org.timeflow.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timeflow.entity.Timetable;
import org.timeflow.entity.User;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Service for integrating with Google Calendar and exporting to iCalendar format
 */
public class CalendarIntegrationService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(CalendarIntegrationService.class);
    private static final String APPLICATION_NAME = "TimeFlow Calendar Integration";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    /**
     * Creates an authorized Credential object for Google Calendar API
     * @param userId The user ID to identify the credentials
     * @return An authorized Credential object
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, String userId) throws IOException {
        // Load client secrets - users would need to configure this
        InputStream in = new FileInputStream("credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(userId);
    }

    /**
     * Syncs user's timetable to Google Calendar
     * @param user The user whose timetable to sync
     * @param timetables List of timetables to sync
     * @return Success message or error
     */
    public String syncToGoogleCalendar(User user, List<Timetable> timetables) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, user.getId().toString()))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            int synced = 0;
            for (Timetable timetable : timetables) {
                if (timetable.getDayOfWeek() != null) {
                    // Create recurring event for weekly schedule
                    Event event = createRecurringEvent(timetable);
                    service.events().insert("primary", event).execute();
                    synced++;
                } else if (timetable.getSpecificDate() != null) {
                    // Create single event for specific date
                    Event event = createSingleEvent(timetable);
                    service.events().insert("primary", event).execute();
                    synced++;
                }
            }
            
            logger.info("Synced {} timetable entries to Google Calendar for user {}", synced, user.getUsername());
            return String.format("Successfully synced %d classes to Google Calendar", synced);
            
        } catch (FileNotFoundException e) {
            logger.error("Google Calendar credentials file not found", e);
            return "Error: Google Calendar credentials not configured. Please set up credentials.json file.";
        } catch (Exception e) {
            logger.error("Failed to sync to Google Calendar for user {}", user.getUsername(), e);
            return "Error syncing to Google Calendar: " + e.getMessage();
        }
    }

    /**
     * Creates a recurring Google Calendar event for weekly timetable entry
     */
    private Event createRecurringEvent(Timetable timetable) {
        Event event = new Event()
                .setSummary(timetable.getCourse().getName() + " (" + timetable.getCourse().getCode() + ")")
                .setDescription(String.format("Type: %s\nRoom: %s\nLecturer: %s",
                        timetable.getType(),
                        timetable.getRoom().getName(),
                        timetable.getCourse().getLecturer() != null ? timetable.getCourse().getLecturer().getUsername() : "N/A"))
                .setLocation(timetable.getRoom().getName());

        // Calculate the next occurrence of this day of week
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextOccurrence = now.with(java.time.temporal.TemporalAdjusters.nextOrSame(timetable.getDayOfWeek()));
        LocalDateTime startDateTime = nextOccurrence.with(timetable.getStartTime());
        LocalDateTime endDateTime = nextOccurrence.with(timetable.getEndTime());

        ZonedDateTime startZoned = startDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime endZoned = endDateTime.atZone(ZoneId.systemDefault());

        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(Date.from(startZoned.toInstant())))
                .setTimeZone(ZoneId.systemDefault().getId());
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(Date.from(endZoned.toInstant())))
                .setTimeZone(ZoneId.systemDefault().getId());
        event.setEnd(end);

        // Set recurrence for weekly event (semester duration)
        String[] recurrence = new String[] {"RRULE:FREQ=WEEKLY;COUNT=15"}; // 15 weeks typical semester
        event.setRecurrence(Collections.singletonList(recurrence[0]));

        return event;
    }

    /**
     * Creates a single Google Calendar event for specific date
     */
    private Event createSingleEvent(Timetable timetable) {
        Event event = new Event()
                .setSummary(timetable.getCourse().getName() + " (" + timetable.getCourse().getCode() + ")")
                .setDescription(String.format("Type: %s\nRoom: %s\nLecturer: %s",
                        timetable.getType(),
                        timetable.getRoom().getName(),
                        timetable.getCourse().getLecturer() != null ? timetable.getCourse().getLecturer().getUsername() : "N/A"))
                .setLocation(timetable.getRoom().getName());

        LocalDateTime startDateTime = timetable.getSpecificDate().atTime(timetable.getStartTime());
        LocalDateTime endDateTime = timetable.getSpecificDate().atTime(timetable.getEndTime());

        ZonedDateTime startZoned = startDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime endZoned = endDateTime.atZone(ZoneId.systemDefault());

        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(Date.from(startZoned.toInstant())))
                .setTimeZone(ZoneId.systemDefault().getId());
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(Date.from(endZoned.toInstant())))
                .setTimeZone(ZoneId.systemDefault().getId());
        event.setEnd(end);

        return event;
    }

    /**
     * Exports timetable to iCalendar (.ics) format for compatibility with various calendar apps
     * @param user The user whose timetable to export
     * @param timetables List of timetables to export
     * @param outputFile The file to write the iCalendar data to
     * @return Success message or error
     */
    public String exportToICalendar(User user, List<Timetable> timetables, File outputFile) {
        try {
            net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
            calendar.getProperties().add(new ProdId("-//TimeFlow//Timetable Export//EN"));
            calendar.getProperties().add(Version.VERSION_2_0);
            calendar.getProperties().add(CalScale.GREGORIAN);

            for (Timetable timetable : timetables) {
                if (timetable.getDayOfWeek() != null) {
                    // Create recurring event for weekly schedule
                    VEvent vEvent = createRecurringICalEvent(timetable);
                    calendar.getComponents().add(vEvent);
                } else if (timetable.getSpecificDate() != null) {
                    // Create single event for specific date
                    VEvent vEvent = createSingleICalEvent(timetable);
                    calendar.getComponents().add(vEvent);
                }
            }

            // Write to file
            FileOutputStream fout = new FileOutputStream(outputFile);
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, fout);
            fout.close();

            logger.info("Exported timetable to iCalendar format for user {}: {}", user.getUsername(), outputFile.getAbsolutePath());
            return "Successfully exported timetable to " + outputFile.getName();
            
        } catch (Exception e) {
            logger.error("Failed to export timetable to iCalendar for user {}", user.getUsername(), e);
            return "Error exporting to iCalendar: " + e.getMessage();
        }
    }

    /**
     * Creates a recurring iCalendar event for weekly timetable entry
     */
    private VEvent createRecurringICalEvent(Timetable timetable) throws Exception {
        // Calculate the next occurrence of this day of week
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextOccurrence = now.with(java.time.temporal.TemporalAdjusters.nextOrSame(timetable.getDayOfWeek()));
        LocalDateTime startDateTime = nextOccurrence.with(timetable.getStartTime());
        LocalDateTime endDateTime = nextOccurrence.with(timetable.getEndTime());

        ZonedDateTime startZoned = startDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime endZoned = endDateTime.atZone(ZoneId.systemDefault());

        VEvent event = new VEvent(
                new net.fortuna.ical4j.model.DateTime(Date.from(startZoned.toInstant())),
                new net.fortuna.ical4j.model.DateTime(Date.from(endZoned.toInstant())),
                timetable.getCourse().getName() + " (" + timetable.getCourse().getCode() + ")"
        );

        // Add UID
        event.getProperties().add(new Uid(java.util.UUID.randomUUID().toString()));
        
        // Add description
        event.getProperties().add(new Description(
                String.format("Type: %s\nRoom: %s\nLecturer: %s",
                        timetable.getType(),
                        timetable.getRoom().getName(),
                        timetable.getCourse().getLecturer() != null ? timetable.getCourse().getLecturer().getUsername() : "N/A")
        ));
        
        // Add location
        event.getProperties().add(new Location(timetable.getRoom().getName()));
        
        // Add recurrence rule for weekly event
        Recur recur = new Recur.Builder()
                .frequency(Recur.Frequency.WEEKLY)
                .count(15) // 15 weeks typical semester
                .build();
        event.getProperties().add(new RRule(recur));

        return event;
    }

    /**
     * Creates a single iCalendar event for specific date
     */
    private VEvent createSingleICalEvent(Timetable timetable) throws Exception {
        LocalDateTime startDateTime = timetable.getSpecificDate().atTime(timetable.getStartTime());
        LocalDateTime endDateTime = timetable.getSpecificDate().atTime(timetable.getEndTime());

        ZonedDateTime startZoned = startDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime endZoned = endDateTime.atZone(ZoneId.systemDefault());

        VEvent event = new VEvent(
                new net.fortuna.ical4j.model.DateTime(Date.from(startZoned.toInstant())),
                new net.fortuna.ical4j.model.DateTime(Date.from(endZoned.toInstant())),
                timetable.getCourse().getName() + " (" + timetable.getCourse().getCode() + ")"
        );

        // Add UID
        event.getProperties().add(new Uid(java.util.UUID.randomUUID().toString()));
        
        // Add description
        event.getProperties().add(new Description(
                String.format("Type: %s\nRoom: %s\nLecturer: %s",
                        timetable.getType(),
                        timetable.getRoom().getName(),
                        timetable.getCourse().getLecturer() != null ? timetable.getCourse().getLecturer().getUsername() : "N/A")
        ));
        
        // Add location
        event.getProperties().add(new Location(timetable.getRoom().getName()));

        return event;
    }
}
