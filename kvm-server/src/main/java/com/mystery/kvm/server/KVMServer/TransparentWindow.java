package com.mystery.kvm.server.KVMServer;

import com.mystery.libmystery.nio.Callback;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TransparentWindow  {
    
    private ArrayList<Callback<Point>> listeners = new ArrayList<>();
    

    private Stage stage;
            
    public void show() {
        stage = new Stage();
        start(stage);
    }
    
    public void hide() {
        stage.hide();
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
        scene.setCursor(Cursor.HAND);
        scene.setFill(null);
        stage.setScene(scene);
        
        scene.onMouseMovedProperty().set((e)-> {
            listeners.forEach((l) -> l.onSuccess(new Point((int)e.getScreenX(), (int)e.getScreenY())));
        });
   
        scene.onMouseClickedProperty().set((e)->{
            stage.hide();
        });
        
        stage.show();
    }

    void addListener(Callback<Point> cb) {
        this.listeners.add(cb);
    }
    
}
