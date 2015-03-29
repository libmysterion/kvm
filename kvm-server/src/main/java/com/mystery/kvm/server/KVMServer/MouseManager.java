
package com.mystery.kvm.server.KVMServer;

import com.mystery.kvm.model.Transition;
import com.mystery.libmystery.nio.Callback;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;


public class MouseManager {

    private MouseMonitor mon = new MouseMonitor();
    private MouseLogicManagerThing managerThing;

    MouseManager(MouseLogicManagerThing managerThing) {
        this.managerThing = managerThing;
    }

    private Callback<Point> cb = (p) -> {
        managerThing.setMousePosition(p);  // he will deal with the scaling
    };

    public final void startMouseMonitor() {
        mon = new MouseMonitor();
        mon.onTick(cb);
        mon.start();
    }

    private void stopMouseMonitor() {
        mon.stop();
        mon = null;
    }

    private TransparentWindow transparentWindow;
    
    void onTransition(Transition transition) {
        // what about scales? --- i think the managerthing should have done that for us
        mouseTo(transition.getHostMousePosition());
        if (transition.isInsideHost()) {
            if(transparentWindow!=null){
                transparentWindow.hide();
                transparentWindow = null;
            }
            startMouseMonitor();
        } else {
            transparentWindow = new TransparentWindow();
            transparentWindow.addListener(cb);
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
