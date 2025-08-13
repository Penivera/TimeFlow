package org.timeflow.service;

public class ServiceFactory {
    private static ServiceFactory instance;

    private AuthenticationService authService;
    private TimetableService timetableService;
    private ConflictDetectionService conflictService;
    private NotificationService notificationService;
    private ReportService reportService;

    private ServiceFactory() {
        initializeServices();
    }

    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    private void initializeServices() {
        authService = new AuthenticationService();
        timetableService = new TimetableService();
        conflictService = new ConflictDetectionService();
        notificationService = new NotificationService();
        reportService = new ReportService();
    }

    public AuthenticationService getAuthenticationService() { return authService; }
    public TimetableService getTimetableService() { return timetableService; }
    public ConflictDetectionService getConflictDetectionService() { return conflictService; }
    public NotificationService getNotificationService() { return notificationService; }
    public ReportService getReportService() { return reportService; }
}
