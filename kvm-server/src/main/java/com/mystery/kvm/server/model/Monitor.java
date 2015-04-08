package com.mystery.kvm.server.model;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;

public class Monitor implements Serializable {

    private Point mousePosition;
    private Dimension size;
    private boolean active;
    private boolean isHost;
    private transient boolean connected;
    private String hostname;
    private int gridX;
    private int gridY;

    public Monitor() {
        connected = false;
    }

    public Monitor(String name, Dimension size, boolean active, boolean isHost, int gridX, int gridY, boolean connected) {
        hostname = name;
        mousePosition = new Point();
        this.size = size;
        this.active = active;
        this.isHost = isHost;
        this.gridX = gridX;
        this.gridY = gridY;
        this.connected = connected;
    }

    public boolean isHost() {
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

    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    public int getGridX() {
        return gridX;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public boolean isConnected() {
        return connected;
    }
    
     public void setConnected(boolean connected) {
        this.connected = connected;
    }

}
