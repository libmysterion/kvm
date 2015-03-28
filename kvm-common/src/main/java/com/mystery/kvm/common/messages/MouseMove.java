
package com.mystery.kvm.common.messages;

import java.io.Serializable;


public class MouseMove implements Serializable{
    
    private int x;
    private int y;

    public MouseMove() {
    }

    
    public MouseMove(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    
    
    
}
