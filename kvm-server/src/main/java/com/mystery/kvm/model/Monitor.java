
package com.mystery.kvm.model;

import java.awt.Dimension;
import java.awt.Point;


public class Monitor {
    
    
    
    private Point mousePosition;
    private Dimension size;     // todo monitor the size of each monitor and update the scales
    private boolean active;
    private boolean isHost;
    private String hostname;
    
    public Monitor(String name, Dimension size, boolean active, boolean isHost){
        hostname = name;
        mousePosition = new Point();
        this.size = size;
        this.active = active;
        this.isHost = isHost;
    }
    
    public boolean isHost(){
        return isHost;
    }
    
    public Point getMousePosition() {
        return new Point(mousePosition);
    }

    public void setMousePosition(Point mousePosition) {
        this.mousePosition.setLocation(mousePosition.x, mousePosition.y);
    }

    public Dimension getSize() {
        return size;
    }

    public void setSize(Dimension size) {
        this.size = size;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getHostname() {
        return hostname;
    }
    
}
