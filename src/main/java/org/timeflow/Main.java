package org.timeflow;

import com.formdev.flatlaf.FlatLightLaf;
import org.timeflow.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set up the FlatLaf look-and-feel for a modern UI
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
        } catch (Exception e) {
            System.err.println("Failed to set FlatLaf look-and-feel: " + e.getMessage());
            e.printStackTrace();
        }

        // Launch the LoginFrame on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}