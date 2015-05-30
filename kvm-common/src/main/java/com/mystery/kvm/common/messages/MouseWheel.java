package com.mystery.kvm.common.messages;

import java.io.Serializable;

public class MouseWheel implements Serializable{
    
    private int amount;

    public MouseWheel(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
    
    
}
