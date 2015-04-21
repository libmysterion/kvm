package com.mystery.kvm.setup;

import com.mystery.kvm.server.KVMServer.KVMServer;
import com.mystery.kvm.server.model.MonitorSetup;
import com.mystery.kvm.setup.connections.ConnectionsPresenter;
import com.mystery.kvm.setup.connections.ConnectionsView;
import com.mystery.kvm.setup.monitors.MonitorsPresenter;
import com.mystery.kvm.setup.monitors.MonitorsView;
import com.mystery.libmystery.event.EventEmitter;
import com.mystery.libmystery.event.Handler;
import com.mystery.libmystery.event.WeakHandler;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javax.inject.Inject;

public class SetupPresenter implements Initializable {

    @FXML
    private HBox hBox;

    @FXML
    private Button startButton;

    private MonitorsPresenter monitorsPresenter;

    @Inject
    private Stage stage;

    @Inject
    private KVMServer kvm;

    @Inject 
    private EventEmitter emitter;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        MonitorsView monitorsView = new MonitorsView();
        ConnectionsView connectionsView = new ConnectionsView();

        hBox.getChildren().add(monitorsView.getView());
        hBox.getChildren().add(connectionsView.getView());

        monitorsPresenter = (MonitorsPresenter) monitorsView.getPresenter();
        ConnectionsPresenter connectionsPresenter = (ConnectionsPresenter) connectionsView.getPresenter();

        startButton.setOnAction(this::startButtonClicked);

        MonitorSetup monitorSetup;
        if (kvm.getConfiguration() != null) {
            monitorSetup = kvm.getConfiguration();
        } else {
            monitorSetup = new MonitorSetup();// auto-loads config from disk
        }

        monitorsPresenter.setConfig(monitorSetup);
        connectionsPresenter.setConfig(monitorSetup);
        connectionsPresenter.addHostMonitor();
        connectionsPresenter.addClients();

        emitter.on("stage.hide", new WeakHandler<>(this.onStageHide));
    }

    private final Handler<Void> onStageHide = (v) -> {
        MonitorSetup config = monitorsPresenter.getConfig();

        try {
            config.save(); // I should also save when you hit the X in the config window or Exit from the menu
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        kvm.setConfiguration(config);

        new GarbageCollectTask(1000).start(); // i want to get all the presenters collected so I can see them remove their listeners from the server
    };

    private void startButtonClicked(ActionEvent event) {
        stage.hide();
    }

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
