package org.timeflow;

import com.formdev.flatlaf.FlatLightLaf;
import org.timeflow.ui.LoginFrame;
import org.timeflow.service.DataSeeder;
import org.timeflow.service.ScheduledReminderService;
import javax.swing.*;
import org.timeflow.util.Config;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
        } catch (Exception e) {
            System.err.println("Failed to set FlatLaf look-and-feel: " + e.getMessage());
            e.printStackTrace();
        }
        new DataSeeder().seedInitialData();
        
        // Start the automated reminder service
        ScheduledReminderService reminderService = new ScheduledReminderService();
        reminderService.startReminderScheduler();
        
        // Add shutdown hook to gracefully stop the scheduler
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down reminder service...");
            reminderService.stopReminderScheduler();
        }));
        
        System.out.println(Config.USERNAME);
        System.out.println(Config.EMAIL_PASSWORD);

        // Launch the LoginFrame on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}