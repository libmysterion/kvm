
package com.mystery.kvm.server.KVMServer;

import com.mystery.kvm.common.transparentwindow.TransparentWindow;
import com.mystery.kvm.server.model.Transition;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InputManager {

    private static final Logger log = LoggerFactory.getLogger(InputManager.class);
    
    private MouseMonitor mon = new MouseMonitor();
    private KVMServer kvm;

    InputManager(KVMServer managerThing) {
        this.kvm = managerThing;
    }

    private void onMouseMoved(Point p){
        kvm.setMousePosition(p);
    }
    
    public final void startMouseMonitor() {
        System.out.println("=====================here");
        log.trace("startMouseMonitor");
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
            transparentWindow.addMouseScrollListener((b) ->  kvm.mouseScrolled(b));
            
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
