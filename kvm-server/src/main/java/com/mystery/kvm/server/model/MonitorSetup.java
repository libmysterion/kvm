package com.mystery.kvm.server.model;

import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.libmystery.persistence.PersistantObject;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MonitorSetup extends PersistantObject {

    static String path = "./monitors_setup";
    
    private List<Monitor> monitors;

    public MonitorSetup() {
        super(path, true);
        if(monitors == null){
            monitors = new ArrayList<>();
        }
    }
    public MonitorSetup(boolean load) {
        super(path, load);
        if(monitors == null){
            monitors = new ArrayList<>();
        }
    }

    public List<Monitor> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
    }
    
    public void connectClient(String hostname) {
        monitors.stream()
                .filter((m) -> m.getHostname().equals(hostname))
                .forEach((m) -> m.setConnected(true));
    }

    public void disconnectClient(String hostname) {
        monitors.stream()
                .filter((m) -> m.getHostname().equals(hostname))
                .forEach((m) -> m.setConnected(false));
    }

    public Monitor findActive() {
        return monitors.stream().filter((m) -> m.isActive()).collect(Collectors.toList()).get(0);
    }

    public Monitor findHost() {
        List<Monitor> collect = monitors.stream().filter((m) -> m.isHost()).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            return collect.get(0);
        }
        return null;
    }

    public final void addMonitor(Monitor monitor) {
        monitors.add(monitor);
    }

    public boolean isTileOccupied(int tile_x, int tile_y) {
        return !monitors.stream()
                .filter((m) -> m.getGridX() == tile_x)
                .filter((m) -> m.getGridY() == tile_y)
                .collect(Collectors.toList())
                .isEmpty();
    }

    public Monitor getMonitor(int tile_x, int tile_y) {
        List<Monitor> collect = monitors.stream()
                .filter((m) -> m.getGridX() == tile_x)
                .filter((m) -> m.getGridY() == tile_y)
                .collect(Collectors.toList());
        if (!collect.isEmpty()) {
            return collect.get(0);
        }
        return null;
    }

    public Monitor findFromCurrent(int offsetX, int offsetY) {
        Monitor active = findActive();
        return getMonitor(offsetX + active.getGridX(), offsetY + active.getGridY());
    }

    public void setSize(String hostName, MonitorInfo mi) {
        monitors.stream()
                .filter((m) -> m.getHostname().equals(hostName))
                .forEach((m) -> m.setSize(new Dimension(mi.getWidth(), mi.getHeight())));
    }

    public boolean hasHost(String hostname) {
        List<Monitor> collect = monitors.stream()
                .filter((m) -> m.getHostname().equals(hostname))
                .collect(Collectors.toList());

        return (!collect.isEmpty());
    }

    public Dimension getSize(String hostname) {
        List<Monitor> collect = monitors.stream()
                .filter((m) -> m.getHostname().equals(hostname))
                .collect(Collectors.toList());

        if (!collect.isEmpty()) {
            return collect.get(0).getSize();
        } else {
            return null;
        }
    }

    public void remove(String hostname) {
        monitors.removeIf((m) -> m.getHostname().equals(hostname));
    }

    public Monitor getMonitor(String hostname) {
        List<Monitor> collect = monitors.stream()
                .filter((m) -> m.getHostname().equals(hostname))
                .collect(Collectors.toList());

        if (!collect.isEmpty()) {
            return collect.get(0);
        } else {
            return null;
        }
    }

}
