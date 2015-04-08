
package com.mystery.kvm.common.messages;

import java.io.Serializable;


public class MouseRelease implements Serializable{

    private int button;

    public MouseRelease() {
    }

    public MouseRelease(int button) {
        this.button = button;
    }

    public int getButton() {
        return button;
    }

    
}
