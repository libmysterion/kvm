package com.mystery.kvm.setup.monitors;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.Objects;
import javafx.beans.property.SimpleBooleanProperty;


public class GridMonitor implements Serializable {

    private final String hostname;
    private final Dimension size;
    private final boolean host;
    private transient SimpleBooleanProperty connected;
    private boolean v;
    private String alias;

    public GridMonitor(String hostname, Dimension dims, boolean host, boolean connected, String alias) {
        this.hostname = hostname;
        this.size = dims;
        this.host = host;
        this.connected = new SimpleBooleanProperty(connected);
        v = connected;
        this.connected.addListener((a,o, n)-> v = n);
        this.alias = alias;
    }

    public String getAlias(){
        return this.alias;
    }
    
    public boolean isConnected() {
        return connectedProperty().get();
    }

    
    public Dimension getSize() {
        return size;
    }

    public String getHostname() {
        return hostname;
    }

    @Override
    public String toString() {
        return this.hostname;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.hostname);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GridMonitor other = (GridMonitor) obj;
        if (!Objects.equals(this.hostname, other.hostname)) {
            return false;
        }
        return true;
    }

    public boolean isHost() {
        return this.host;
    }

    void setConnected(boolean b) {
        this.connectedProperty().set(b);
    }
    
    SimpleBooleanProperty connectedProperty(){
        // once deserialized
        if(this.connected ==null){
            this.connected = new SimpleBooleanProperty(v);
            this.connected.addListener((a,o, n)-> v = n); 
        }        
        return this.connected;
    }
    
}