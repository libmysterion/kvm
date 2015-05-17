package com.mystery.kvm.client;

import com.mystery.libmystery.nio.NioClient;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import javafx.application.Platform;
import javax.imageio.ImageIO;

public class Tray {

    
    private NioClient client;

    private TrayIcon trayIcon;
    private AutoJoin autojoin;

    private void Tray() {
        // todo - i need an onConnected here to display the info bubble
//        client.onConnected(new WeakHandler(c) -> {
//            SwingUtilities.invokeLater(() -> {
//                if (trayIcon != null) {
//                    // display some tray message
//                    //trayIcon.displayMessage(msg.getHeading(), msg.getMessage(), msg.getType());
//                }
//            });
//        });
    }

    public void setClient(NioClient client) {
        this.client = client;
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
            trayIcon = new TrayIcon(ImageIO.read(this.getClass().getResourceAsStream("/icons/monitor-icon-16x16.png")));

           
           
            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
                Platform.exit();
                tray.remove(trayIcon);
                try {
                    if (client != null) {
                        client.close();
                    }
                    if(autojoin!=null){
                        autojoin.stop();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // add the application tray icon to the system tray.
            tray.add(trayIcon);

        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }

    void setAutoJoin(AutoJoin autojoin) {
        this.autojoin = autojoin;
    }
}
