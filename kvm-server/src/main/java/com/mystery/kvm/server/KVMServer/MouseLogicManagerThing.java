package com.mystery.kvm.server.KVMServer;

import com.mystery.kvm.MouseMover;
import com.mystery.kvm.model.Monitor;
import com.mystery.kvm.model.MonitorSetup;
import com.mystery.kvm.model.Transition;
import java.awt.Point;

public class MouseLogicManagerThing {

    private final MonitorSetup setup;
    private MouseManager mouseManager = new MouseManager(this);
    private Monitor activeMonitor;
    private Monitor hostMonitor;
    private MouseMover mover;

    public MouseLogicManagerThing(MonitorSetup setup, MouseMover mover) {
        this.setup = setup;
        this.activeMonitor = setup.findActive();
        this.hostMonitor = activeMonitor;
        mouseManager.startMouseMonitor();
        this.mover = mover;
    }

    static final int BORDER = 2; // pixels

  

    // this method is called continuously by the mouse manager
    void setMousePosition(Point p) {

        try {
            // keep the model updated always
            DimensionScale scale = new DimensionScale(this.hostMonitor.getSize(), this.activeMonitor.getSize());
            Point scaledPoint = scale.scalePoint(p);
            this.activeMonitor.setMousePosition(scaledPoint);

            updateActiveClient();

            // all this could easily be based on the HOST monitor only
            // so no scaling errors need to be taken into account
            // todo...
            if (activeMonitor.getMousePosition().x > activeMonitor.getSize().width - BORDER) {
                this.doTransition(1, 0, BORDER, p.y);
            } else if (activeMonitor.getMousePosition().x < BORDER) {
                this.doTransition(-1, 0, hostMonitor.getSize().width - BORDER, p.y);
            } else if (activeMonitor.getMousePosition().y > activeMonitor.getSize().height - BORDER) {
                this.doTransition(0, 1, p.x, BORDER);
            } else if (activeMonitor.getMousePosition().y < BORDER) {
                this.doTransition(0, -1, p.x, hostMonitor.getSize().height - BORDER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void updateActiveClient() {
        Point pos = this.activeMonitor.getMousePosition();
        this.mover.move(activeMonitor.getHostname(), pos.x, pos.y);
    }

    // there is some recursion thing not taken into acount here
    // the move from the robot is gonna mess with this stuff and thro it into a wierd loop
    // ...soo ten i will robot the mouse onto the BORDER position
    // tghat way it wont try to transition again
    private void doTransition(int x, int y, int px, int py) {
        // System.out.println("doTransition");
        Monitor nextMonitor = this.setup.findFromCurrent(x, y);
        if (nextMonitor != null) {
            System.out.println("Moved monitor");
            this.activeMonitor.setActive(false);
            this.activeMonitor = nextMonitor;
            this.activeMonitor.setActive(true);
            this.mouseManager.onTransition(new Transition(new Point(px, py), activeMonitor == hostMonitor));

            // i need to mesage to the active client to tell him to hide his overlay window
            // and i begin to direct mouse location mesages to the active client...but thats sorta done
        }

    }

}
