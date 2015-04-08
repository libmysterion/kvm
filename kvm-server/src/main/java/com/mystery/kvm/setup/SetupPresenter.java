package com.mystery.kvm.setup;

import com.mystery.kvm.server.KVMServer.KVMServer;
import com.mystery.kvm.server.model.MonitorSetup;
import com.mystery.kvm.setup.connections.ConnectionsPresenter;
import com.mystery.kvm.setup.connections.ConnectionsView;
import com.mystery.kvm.setup.monitors.MonitorsPresenter;
import com.mystery.kvm.setup.monitors.MonitorsView;
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
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        
        MonitorsView monitorsView = new MonitorsView();
        ConnectionsView connectionsView = new ConnectionsView();
        
        hBox.getChildren().add(monitorsView.getView());
        hBox.getChildren().add(connectionsView.getView());
               
        monitorsPresenter = (MonitorsPresenter)monitorsView.getPresenter();
        ConnectionsPresenter connectionsPresenter = (ConnectionsPresenter)connectionsView.getPresenter();
        
        startButton.setOnAction(this:: startButtonClicked);

        MonitorSetup monitorSetup;
        if(kvm.getConfiguration() != null){
              monitorSetup = kvm.getConfiguration();
        } else {
            monitorSetup = new MonitorSetup();// auto-loads config from disk
        }
        
        monitorsPresenter.setConfig(monitorSetup);
        connectionsPresenter.setConfig(monitorSetup);
        connectionsPresenter.addHostMonitor();
        connectionsPresenter.addClients();
        
    }
    
    private void startButtonClicked(ActionEvent event){
        MonitorSetup config = monitorsPresenter.getConfig();
        kvm.setConfiguration(config);
        stage.hide();
    }

    
}
