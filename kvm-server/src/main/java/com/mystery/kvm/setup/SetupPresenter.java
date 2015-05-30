package com.mystery.kvm.setup;

import com.mystery.kvm.server.KVMServer.KVMServer;
import com.mystery.kvm.setup.connections.ConnectionsView;
import com.mystery.kvm.setup.monitors.MonitorsView;
import com.mystery.libmystery.event.EventEmitter;
import com.mystery.libmystery.event.Handler;
import com.mystery.libmystery.event.WeakHandler;
import com.mystery.libmystery.injection.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class SetupPresenter implements Initializable{

    @FXML
    private HBox hBox;

    @FXML
    private Button startButton;

    @Inject
    private Stage stage;

    @Inject
    private KVMServer kvm;

    @Inject 
    private EventEmitter emitter;
    

    @Override
    public void initialize(URL url, ResourceBundle bundle) {

        MonitorsView monitorsView = new MonitorsView();
        ConnectionsView connectionsView = new ConnectionsView();

        hBox.getChildren().add(monitorsView.getRootNode());
        hBox.getChildren().add(connectionsView.getRootNode());
        
        startButton.setOnAction((e) -> stage.hide());
           
        emitter.on("stage.hide", new WeakHandler<>(this.onStageHide));
    }
    
    private final Handler<Void> onStageHide = (v) -> {
        new GarbageCollectTask(500).start(); // i want to get all the presenters collected so I can see them remove their listeners from the server
    };

}

class GarbageCollectTask extends Thread {

    private int deferTime;

    GarbageCollectTask(int deferTime) {
        this.deferTime = deferTime;
    }

    @Override
    public void run() {

        try {
            Thread.sleep(deferTime);
        } catch (InterruptedException ex) {
            return;
        }

        System.gc();
    }
}
