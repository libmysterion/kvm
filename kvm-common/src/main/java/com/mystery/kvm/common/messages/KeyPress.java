package com.mystery.kvm.common.messages;

import java.io.Serializable;

public class KeyPress implements Serializable {
    
    private int keycode;

    public KeyPress() {
    }

    public KeyPress(int keycode) {
        this.keycode = keycode;
    }

    public int getKeycode() {
        return keycode;
    }
    
}
