package com.mystery.kvm.tray;

import com.mystery.libmystery.event.EventEmitter;
import com.mystery.libmystery.nio.MioServer;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import javafx.application.Platform;
import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;

public class TrayPresenter {

    @Inject
    private EventEmitter emitter;
    
    @Inject
    private MioServer server;

    private TrayIcon trayIcon;

    @PostConstruct
    private void init() {
        emitter.on(TrayMessage.class, (msg) -> {
            SwingUtilities.invokeLater(() -> {
                if (trayIcon != null) {
                    trayIcon.displayMessage(msg.getHeading(), msg.getMessage(), msg.getType());
    }
            });
        });
    }

    private void trayOnAction(java.awt.event.ActionEvent e) {
        Platform.runLater(() -> {
            emitter.emit("showSetupView", null);
        });
    }
    
    public void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }

            // set up a system tray icon.
            SystemTray tray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(ImageIO.read(this.getClass().getResourceAsStream("/server/icons/monitor-icon-16x16.png")));

            // if the user double-clicks on the tray icon, show the setup screen
            trayIcon.addActionListener(this::trayOnAction);
            java.awt.MenuItem openItem = new java.awt.MenuItem("Configure");
            openItem.addActionListener(this::trayOnAction);
           
            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);
           
            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
                Platform.exit();
                tray.remove(trayIcon);
                try {
                    server.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            });

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // add the application tray icon to the system tray.
            tray.add(trayIcon);

        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }
}
