
package com.mystery.kvm.common.messages;

import java.io.Serializable;


public class MonitorInfo implements Serializable{
    
    private int width;
    private int height;

    public MonitorInfo() {
    }

    
    public MonitorInfo(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
}
