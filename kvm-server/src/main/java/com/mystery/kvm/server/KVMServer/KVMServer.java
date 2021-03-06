package com.mystery.kvm.server.KVMServer;

import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.server.model.Monitor;
import com.mystery.kvm.server.model.MonitorSetup;
import com.mystery.kvm.server.model.Transition;
import com.mystery.kvm.tray.TrayMessage;
import com.mystery.libmystery.event.DualHandler;
import com.mystery.libmystery.event.EventEmitter;
import com.mystery.libmystery.injection.Inject;
import com.mystery.libmystery.injection.PostConstruct;
import com.mystery.libmystery.injection.Property;
import com.mystery.libmystery.injection.Singleton;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.MioServer;
import java.awt.Point;
import java.awt.TrayIcon;
import javafx.stage.Stage;
import sun.util.logging.resources.logging;

@Singleton
public class KVMServer {

    private final Object setupLock = new Object();

    private MonitorSetup setup;
    private InputManager mouseManager = new InputManager(this);
    private Monitor activeMonitor;
    private Monitor hostMonitor;

    @Inject
    private MioServer server;

    @Inject
    private Stage configStage;

    @Inject
    private MouseMessager messager;

    @Inject
    private EventEmitter emitter;

    @Property
    private String newMonitorBalloonHeader;

    @Property
    private String newMonitorBalloonText;

    @Property
    private String monitorReconnectBalloonHeader;

    @Property
    private String monitorReconnectBalloonText;

    @PostConstruct
    void initialise() {
        mouseManager.startMouseMonitor();
        emitter.on("client.connect", clientConnected);
        emitter.on("client.disconnect", clientDisconnected);
    }

    public void setConfiguration(MonitorSetup setup) {

        synchronized (setupLock) {
            System.out.println("set config----");
            this.setup = setup;
            this.hostMonitor = setup.findHost();
            this.activeMonitor = hostMonitor;
        }

    }

    public MonitorSetup getConfiguration() {
        return setup;
    }

    static final int BORDER = 2; // pixels

    // this method is called continuously by the mouse manager
    void setMousePosition(Point p) {
        synchronized (setupLock) {
            if (this.setup != null && hostMonitor != null) {
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
        }
    }

    void updateActiveClient() {
        Point pos = this.activeMonitor.getMousePosition();
        this.messager.move(activeMonitor.getHostname(), pos.x, pos.y);
    }

    private void doTransition(int x, int y, int px, int py) {
        Monitor nextMonitor = this.setup.findFromCurrent(x, y);
        if (nextMonitor != null && nextMonitor.isConnected()) {
           
            this.messager.deactivate(this.activeMonitor.getHostname());
            this.activeMonitor.setActive(false);

            this.activeMonitor = nextMonitor;
            this.messager.activate(this.activeMonitor.getHostname());
            this.activeMonitor.setActive(true);
            this.mouseManager.onTransition(new Transition(new Point(px, py), activeMonitor == hostMonitor));
        }

    }

    void mousePressed(int button) {
        messager.mousePress(this.activeMonitor.getHostname(), button);
    }

    void mouseReleased(int button) {
        messager.mouseRelease(this.activeMonitor.getHostname(), button);
    }
    
    void mouseScrolled (int amount) {
        messager.mouseWheel(this.activeMonitor.getHostname(), amount);
    }

    void keyPressed(int k) {
        messager.keyPress(this.activeMonitor.getHostname(), k);
    }

    void keyReleased(int k) {
        messager.keyRelease(this.activeMonitor.getHostname(), k);
    }

    private final DualHandler<AsynchronousObjectSocketChannel, MonitorInfo> clientConnected = (client, monitor) -> {

        if (setup != null) {

            setup.connectMonitor(monitor);

            if (!configStage.isShowing()) {
                boolean isInGridConfig = setup.hasMonitor(monitor);
                if (isInGridConfig) {
                    emitter.emit(TrayMessage.class, new TrayMessage(monitorReconnectBalloonHeader, monitor.getHostName() + monitorReconnectBalloonText, TrayIcon.MessageType.INFO));
                } else {
                    emitter.emit(TrayMessage.class, new TrayMessage(newMonitorBalloonHeader, monitor.getHostName() + newMonitorBalloonText, TrayIcon.MessageType.INFO));
                }
            }
        }

    };

    private final DualHandler<AsynchronousObjectSocketChannel, MonitorInfo> clientDisconnected = (client, monitorInfo) -> {
        if (setup != null) {    // setup null if not started yet

            setup.disconnectMonitor(monitorInfo);
            Monitor monitor = setup.getMonitor(monitorInfo.getHostName());

            if (activeMonitor == monitor) {
                this.activeMonitor.setActive(false);
                this.activeMonitor = hostMonitor;
                this.activeMonitor.setActive(true);
                int w = activeMonitor.getSize().width / 2;
                int h = activeMonitor.getSize().height / 2;
                this.mouseManager.onTransition(new Transition(new Point(w, h), true));
            }

        }

    };

}
