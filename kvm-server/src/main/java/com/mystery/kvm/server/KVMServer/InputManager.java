
package com.mystery.kvm.server.KVMServer;

import com.mystery.kvm.server.model.Transition;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;


public class InputManager {

    private MouseMonitor mon = new MouseMonitor();
    private KVMServer kvm;

    InputManager(KVMServer managerThing) {
        this.kvm = managerThing;
    }

    private void onMouseMoved(Point p){
        kvm.setMousePosition(p);
    }
    
    public final void startMouseMonitor() {
        mon = new MouseMonitor();
        mon.onTick(this::onMouseMoved);
        mon.start();
    }

    private void stopMouseMonitor() {
        mon.stop();
        mon = null;
    }

    private TransparentWindow transparentWindow;
    
    void onTransition(Transition transition) {
        mouseTo(transition.getHostMousePosition());
        if (transition.isInsideHost()) {
            if(transparentWindow!=null){
                transparentWindow.hide();
                transparentWindow = null;
            }
            startMouseMonitor();
        } else {
            transparentWindow = new TransparentWindow();
           
            transparentWindow.addMouseMoveListener(this::onMouseMoved);
            transparentWindow.addMousePressListener((b) ->  kvm.mousePressed(b));
            transparentWindow.addMouseReleaseListener((b) ->  kvm.mouseReleased(b));
            transparentWindow.addKeyPressListener((k) ->  kvm.keyPressed(k));
            transparentWindow.addKeyReleaseListener((k) ->  kvm.keyReleased(k));
            
            
            transparentWindow.show();
            
            stopMouseMonitor();
        }
    }

    private void mouseTo(Point p) {
        try {
            Robot r = new Robot();
            r.mouseMove(p.x, p.y);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }
}
