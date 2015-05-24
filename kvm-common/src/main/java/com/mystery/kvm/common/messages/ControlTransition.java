package com.mystery.kvm.common.messages;

import java.io.Serializable;

public class ControlTransition implements Serializable{

    
    private boolean active;

    public ControlTransition(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
   
    
    
}
