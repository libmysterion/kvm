
package com.mystery.kvm.setup.connections;

import com.mystery.kvm.common.messages.MonitorInfo;

public class ConnectedMonitor {

    private boolean connected;
    private MonitorInfo monitorInfo;

    public ConnectedMonitor(boolean connected, MonitorInfo monitorInfo) {
        this.connected = connected;
        this.monitorInfo = monitorInfo;
    }
    
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public MonitorInfo getMonitorInfo() {
        return monitorInfo;
    }

    public void setMonitorInfo(MonitorInfo monitorInfo) {
        this.monitorInfo = monitorInfo;
    }
    
    
}
