package com.mystery.kvm.client;

import com.mystery.kvm.common.messages.ConnectClient;
import com.mystery.kvm.common.messages.ControlTransition;
import com.mystery.kvm.common.messages.DisconnectClient;
import com.mystery.kvm.common.messages.KeyPress;
import com.mystery.kvm.common.messages.KeyRelease;
import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.common.messages.MouseMove;
import com.mystery.kvm.common.messages.MousePress;
import com.mystery.kvm.common.messages.MouseRelease;
import com.mystery.kvm.common.messages.MouseWheel;
import com.mystery.kvm.common.transparentwindow.TransparentWindow;
import com.mystery.libmystery.nio.NioClient;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KVMClient {

    private static final Logger log = LoggerFactory.getLogger(KVMClient.class);
    private NioClient nioClient;
    private MonitorInfo serverMonitorInfo;
    private Tray tray;

    private TransparentWindow transparentWindow;

    public KVMClient(NioClient nioClient, Tray tray) {
        log.debug("KVM Client connected");
        this.nioClient = nioClient;
        this.tray = tray;
        attach();
    }

    private void attach() {
        //NioClient nioClient = new NioClient();
        //nioClient.connect("127.0.0.1", 7777).onSucess(()->{
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

        try {
            nioClient.send(new MonitorInfo(size.width, size.height, InetAddress.getLocalHost().getHostName()));
        } catch (UnknownHostException ex) {
            //if i connected to something i dont think this can really happpen....
            ex.printStackTrace();
        }

        nioClient.onMessage(ControlTransition.class, (msg) -> {
            log.debug("onControlTransition");
            if (!msg.isActive()) {
                if (transparentWindow == null) {
                    transparentWindow = new TransparentWindow();
                    transparentWindow.show();
                }
            } else {
                if (transparentWindow != null) {
                    transparentWindow.hide();
                    transparentWindow = null;
                }
            }
        });

        nioClient.onMessage(MouseMove.class, (msg) -> {

            try {
                Robot r = new Robot();
                r.mouseMove(msg.getX(), msg.getY());
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        });

        nioClient.onMessage(MouseWheel.class, (msg) -> {

            try {
                Robot r = new Robot();
                r.mouseWheel(msg.getAmount());
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        });

        nioClient.onMessage(MousePress.class, (msg) -> {

            try {
                Robot r = new Robot();
                switch (msg.getButton()) {
                    case 1:
                        r.mousePress(InputEvent.BUTTON1_MASK);  // == javafx primary
                        break;
                    case 2:
                        r.mousePress(InputEvent.BUTTON2_MASK);  // = javafx secondary
                        break;
                    case 3:
                        r.mousePress(InputEvent.BUTTON3_MASK);  // == javafx middle
                    }
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        });

        nioClient.onMessage(MouseRelease.class, (msg) -> {
            try {
                Robot r = new Robot();
                switch (msg.getButton()) {
                    case 1:
                        r.mouseRelease(InputEvent.BUTTON1_MASK);  // == javafx primary
                        break;
                    case 2:
                        r.mouseRelease(InputEvent.BUTTON2_MASK);  // = javafx secondary
                        break;
                    case 3:
                        r.mouseRelease(InputEvent.BUTTON3_MASK);  // == javafx middle
                    }
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        });

        nioClient.onMessage(KeyPress.class, (msg) -> {
            try {
                Robot r = new Robot();
                r.keyPress(msg.getKeycode());
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        });

        nioClient.onMessage(KeyRelease.class, (msg) -> {
            try {
                Robot r = new Robot();
                r.keyRelease(msg.getKeycode());
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        });

        nioClient.onMessage(MonitorInfo.class, (monitorInfo) -> {
            log.debug("Received MonitorInfo");
            serverMonitorInfo = monitorInfo;
            tray.addClient(this);
        });
        
        nioClient.onMessage(DisconnectClient.class, (msg)->{
            tray.removedByServer(this);
        });

        nioClient.onDisconnect((c) -> {
            if (transparentWindow != null) {
                transparentWindow.hide();
            }
            tray.removeClient(this);
        });
    }

    void close() {
        try {
            nioClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MonitorInfo getMonitorInfo() {
        return this.serverMonitorInfo;
    }

    void connectToServer() {
        this.nioClient.send(new ConnectClient());
    }

    void disconnectFromServer() {
        this.nioClient.send(new DisconnectClient());
    }

}
