package com.mystery.kvm.model;

public class MonitorSetup {

    private Monitor host;
    private Monitor[][] grid;
 
    public MonitorSetup(int size) {
        grid = new Monitor[size][size];
    }

    public Monitor findActive() {
        for (Monitor[] row : grid) {
            for (Monitor monitor : row) {
                if (monitor != null && monitor.isActive()) {
                    return monitor;
                }
            }
        }
        throw new IllegalStateException("Could not determine the active monitor");
    }

    public final void addMonitor(int tile_x, int tile_y, Monitor monitor) {
        // too add validation logic to make sure that the monitors are touching each other in the grid
        grid[tile_y][tile_x] = monitor;
    }

    public Monitor getMonitor(int tile_x, int tile_y) {
        if (isTileOccupied(tile_x, tile_y)) {
            return _getMonitor(tile_x, tile_y);
        }
        return null;
    }

    public boolean isTileOccupied(int tile_x, int tile_y) {
        if (tile_y >= grid.length) {
            return false;
        }

        if (tile_x >= grid[tile_y].length) {
            return false;
        }

        return (_getMonitor(tile_x, tile_y) != null);
    }

    private Monitor _getMonitor(int tile_x, int tile_y) {
        return grid[tile_y][tile_x];
    }

    public Monitor findFromCurrent(int x, int y) {
        for (int gy = 0; gy < grid.length; gy++){
            Monitor[] row = grid[gy];
            for (int gx = 0; gx < grid[gy].length; gx++){
                Monitor monitor = row[gx];
                if(monitor!=null && monitor.isActive()){
                    System.out.println("got the new active dude " + (gx + x) + ":" + (gy + y));
                    return this.getMonitor(gx + x, gy + y);
                }
            }
        }
       throw new IllegalStateException("Could not determine the active monitor"); 
    }
}
