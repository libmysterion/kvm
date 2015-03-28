package com.mystery.kvm.model;

import java.awt.Point;


public class Transition {
    
    private Point hostMousePosition;
    private boolean insideHost;

    public Transition(Point hostMousePosition, boolean insideHost) {
        this.hostMousePosition = hostMousePosition;
        this.insideHost = insideHost;
    }

    public Point getHostMousePosition() {
        return hostMousePosition;
    }

    public void setHostMousePosition(Point hostMousePosition) {
        this.hostMousePosition = hostMousePosition;
    }

    public boolean isInsideHost() {
        return insideHost;
    }

    public void setInsideHost(boolean insideHost) {
        this.insideHost = insideHost;
    }
    
    
}
