package com.mystery.kvm.server.KVMServer;

import com.mystery.libmystery.nio.Callback;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TransparentWindow {

    private ArrayList<Callback<Point>> moveListeners = new ArrayList<>();
    private ArrayList<Callback<Integer>> mousePressListeners = new ArrayList<>();
    private ArrayList<Callback<Integer>> mouseReleaseListeners = new ArrayList<>();

    private ArrayList<Callback<Integer>> keyPressListeners = new ArrayList<>();
    private ArrayList<Callback<Integer>> keyReleaseListeners = new ArrayList<>();

    private Stage stage;

    public void show() {

        KeyHook.blockWindowsKey((code) -> keyPressListeners.forEach((x) -> x.onSuccess(code)),
                (code) -> keyReleaseListeners.forEach((x) -> x.onSuccess(code)));

        Platform.runLater(() -> {
            try {
                stage = new Stage();
                start(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void hide() {
        KeyHook.unblockWindowsKey();
        Platform.runLater(() -> {
            stage.hide();
        });
    }

    private void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setOpacity(0.01d);
        stage.setAlwaysOnTop(true);
        stage.setX(0);
        stage.setY(0);
        stage.setWidth(Toolkit.getDefaultToolkit().getScreenSize().width);
        stage.setHeight(Toolkit.getDefaultToolkit().getScreenSize().height);

        VBox box = new VBox();
        final Scene scene = new Scene(box, stage.getWidth(), stage.getHeight());
        scene.setCursor(Cursor.NONE);
        scene.setFill(null);
        stage.setScene(scene);

        scene.setOnMouseMoved((e) -> {
            moveListeners.forEach((l) -> l.onSuccess(new Point((int) e.getScreenX(), (int) e.getScreenY())));
        });

        scene.setOnMouseDragged((e) -> {
            moveListeners.forEach((l) -> l.onSuccess(new Point((int) e.getScreenX(), (int) e.getScreenY())));
        });

        scene.setOnMousePressed((e) -> {
            MouseButton button = e.getButton();
            int val = button == MouseButton.PRIMARY ? 1 : button == MouseButton.SECONDARY ? 2 : button == MouseButton.MIDDLE ? 3 : 4;
            mousePressListeners.forEach((l) -> l.onSuccess(val));
        });

        scene.setOnMouseReleased((e) -> {
            MouseButton button = e.getButton();
            int val = button == MouseButton.PRIMARY ? 1 : button == MouseButton.SECONDARY ? 2 : button == MouseButton.MIDDLE ? 3 : 4;
            mouseReleaseListeners.forEach((l) -> l.onSuccess(val));
        });

        scene.setOnKeyPressed(this::keyPressed);
        scene.setOnKeyReleased(this::keyReleased);

        stage.show();
        System.out.println("done window.start");

    }

    private void keyReleased(KeyEvent e) {
        int code = e.getCode().impl_getCode();
        System.out.println("keyReleased:" + code);
        keyReleaseListeners.forEach((x) -> x.onSuccess(code));
    }

    private void keyPressed(KeyEvent e) { 
       int code = e.getCode().impl_getCode();   // this is probab;y about to stop working
       keyPressListeners.forEach((x) -> x.onSuccess(code));
    }

        
    void addMouseMoveListener(Callback<Point> cb) {
        this.moveListeners.add(cb);
    }

    void addMousePressListener(Callback<Integer> cb) {
        this.mousePressListeners.add(cb);
    }

    void addMouseReleaseListener(Callback<Integer> cb) {
        this.mouseReleaseListeners.add(cb);
    }

    void addKeyPressListener(Callback<Integer> cb) {
        this.keyPressListeners.add(cb);
    }

    void addKeyReleaseListener(Callback<Integer> cb) {
        this.keyReleaseListeners.add(cb);
    }

}
