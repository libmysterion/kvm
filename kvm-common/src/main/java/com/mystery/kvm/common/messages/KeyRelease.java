package com.mystery.kvm.common.messages;

import java.io.Serializable;


public class KeyRelease implements Serializable {
    
    private int keycode;

    public KeyRelease() {
    }

    public KeyRelease(int keycode) {
        this.keycode = keycode;
    }

    public int getKeycode() {
        return keycode;
    }
    
}
