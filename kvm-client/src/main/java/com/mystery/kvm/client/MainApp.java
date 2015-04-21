package com.mystery.kvm.client;

import com.mystery.kvm.common.messages.KeyPress;
import com.mystery.kvm.common.messages.KeyRelease;
import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.common.messages.MouseMove;
import com.mystery.kvm.common.messages.MousePress;
import com.mystery.kvm.common.messages.MouseRelease;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        AutoJoin j = new AutoJoin((nioClient) -> {

            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            nioClient.send(new MonitorInfo(size.width, size.height));

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

        });

        j.start();
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
