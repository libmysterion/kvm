
package com.mystery.kvm.common.messages;

import java.io.Serializable;


public class MonitorInfo implements Serializable{
    
    private int width;
    private int height;
    private String hostName;

    public MonitorInfo() {
    }

    
    public MonitorInfo(int width, int height, String hostName) {
        this.width = width;
        this.height = height;
        this.hostName = hostName;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public String getHostName(){
        return hostName;
    }
   
}
