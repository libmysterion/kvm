package com.mystery.kvm.client;

import com.mystery.kvm.common.messages.KeyPress;
import com.mystery.kvm.common.messages.KeyRelease;
import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.common.messages.MouseMove;
import com.mystery.kvm.common.messages.MousePress;
import com.mystery.kvm.common.messages.MouseRelease;
import com.mystery.libmystery.nio.Callback;
import com.mystery.libmystery.nio.NioClient;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Tray tray;
    private AutoJoin autojoin;
    
    private Callback<NioClient> attachClient = (nioClient) -> {

        tray.setClient(nioClient);
        
        //NioClient nioClient = new NioClient();
        //nioClient.connect("127.0.0.1", 7777).onSucess(()->{
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

        try {
            nioClient.send(new MonitorInfo(size.width, size.height, InetAddress.getLocalHost().getHostName()));
        } catch (UnknownHostException ex) {
            //if i connected to something i dont think this can really happpen....
            ex.printStackTrace();
        }

        nioClient.onMessage(MouseMove.class, (msg) -> {

            try {
                Robot r = new Robot();
                r.mouseMove(msg.getX(), msg.getY());
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
        
        nioClient.onDisconnect((c) -> {
            startAutoJoin();
        });

    };
    
    
    private void startAutoJoin() {
        
        if(autojoin == null){
            autojoin = new AutoJoin(attachClient);
            tray.setAutoJoin(autojoin);
        }
        
        try {
            autojoin.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        tray  = new Tray();
        tray.addAppToTray();
        startAutoJoin();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
