package com.mystery.kvm.common.transparentwindow;

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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransparentWindow {

    private final static Logger log = LoggerFactory.getLogger(TransparentWindow.class);

    private ArrayList<Callback<Point>> moveListeners = new ArrayList<>();
    private ArrayList<Callback<Integer>> mousePressListeners = new ArrayList<>();
    private ArrayList<Callback<Integer>> mouseReleaseListeners = new ArrayList<>();
    private ArrayList<Callback<Integer>> mouseScrollListeners = new ArrayList<>();

    private ArrayList<Callback<Integer>> keyPressListeners = new ArrayList<>();
    private ArrayList<Callback<Integer>> keyReleaseListeners = new ArrayList<>();

    private Stage undecoratedStage;

    public void show() {

        KeyHook.blockWindowsKey((code) -> keyPressListeners.forEach((x) -> x.onSuccess(code)),
                (code) -> keyReleaseListeners.forEach((x) -> x.onSuccess(code)));

        Platform.runLater(() -> {
            try {
                Stage offSceeenUtilityStage = new Stage(StageStyle.UTILITY);
                offSceeenUtilityStage.setWidth(0);
                offSceeenUtilityStage.setHeight(0);
                offSceeenUtilityStage.setX(Double.MAX_VALUE);
                offSceeenUtilityStage.setY(Double.MAX_VALUE);

                undecoratedStage = new Stage(StageStyle.UNDECORATED);
                undecoratedStage.initOwner(offSceeenUtilityStage);
                undecoratedStage.initModality(Modality.WINDOW_MODAL);
                offSceeenUtilityStage.show();
                start(undecoratedStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void hide() {
        log.debug("Hiding Transparent Window");
        KeyHook.unblockWindowsKey();
        Platform.runLater(() -> {
            undecoratedStage.hide();
        });
    }

    private void start(Stage stage) {
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
            int val = button == MouseButton.PRIMARY ? 1 : button == MouseButton.SECONDARY ? 3 : button == MouseButton.MIDDLE ? 2 : 4;
            mousePressListeners.forEach((l) -> l.onSuccess(val));
        });

        scene.setOnMouseReleased((e) -> {
            MouseButton button = e.getButton();
            int val = button == MouseButton.PRIMARY ? 1 : button == MouseButton.SECONDARY ? 3 : button == MouseButton.MIDDLE ? 2 : 4;
            mouseReleaseListeners.forEach((l) -> l.onSuccess(val));
        });

        scene.setOnScroll((e) -> {
            int val = e.getDeltaY() < 0 ? 1 : -1;
            mouseScrollListeners.forEach((l) -> l.onSuccess(val));
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

    public void addMouseMoveListener(Callback<Point> cb) {
        this.moveListeners.add(cb);
    }

    public void addMousePressListener(Callback<Integer> cb) {
        this.mousePressListeners.add(cb);
    }

    public void addMouseScrollListener(Callback<Integer> cb) {
        this.mouseScrollListeners.add(cb);
    }

    public void addMouseReleaseListener(Callback<Integer> cb) {
        this.mouseReleaseListeners.add(cb);
    }

    public void addKeyPressListener(Callback<Integer> cb) {
        this.keyPressListeners.add(cb);
    }

    public void addKeyReleaseListener(Callback<Integer> cb) {
        this.keyReleaseListeners.add(cb);
    }

}
