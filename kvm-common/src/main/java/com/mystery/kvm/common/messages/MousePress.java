package com.mystery.kvm.common.messages;

import java.io.Serializable;

public class MousePress implements Serializable {

    private int button;

    public MousePress() {
    }

    public MousePress(int button) {
        this.button = button;
    }

    public int getButton() {
        return button;
    }

}
