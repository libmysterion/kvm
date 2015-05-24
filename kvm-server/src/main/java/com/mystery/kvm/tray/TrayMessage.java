package com.mystery.kvm.tray;

import java.awt.TrayIcon;

public class TrayMessage {
    
   private String heading;
   private String message;
   private TrayIcon.MessageType type;

    public TrayMessage(String heading, String message, TrayIcon.MessageType type) {
        this.heading = heading;
        this.message = message;
        this.type = type;
    }

    public String getHeading() {
        return heading;
    }

    public String getMessage() {
        return message;
    }

    public TrayIcon.MessageType getType() {
        return type;
    }
   
   
    
}
