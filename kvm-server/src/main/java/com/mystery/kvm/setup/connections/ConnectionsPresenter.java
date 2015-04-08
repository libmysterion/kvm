package com.mystery.kvm.setup.connections;

import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.server.model.MonitorSetup;
import com.mystery.kvm.setup.monitors.GridMonitor;
import com.mystery.kvm.setup.monitors.MonitorTableCell;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.MioServer;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javax.inject.Inject;

public class ConnectionsPresenter implements Initializable {

    @FXML
    private ListView listView;

    @Inject
    private MioServer server;

    private final ObservableList<GridMonitor> availableMonitors = FXCollections.observableArrayList();

    private MonitorSetup monitorSetup;
        
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        System.out.println("initialize");

        listView.setItems(availableMonitors);
        listView.setCellFactory((e) -> new ConnectionsListCell(this));

        listView.setOnDragOver(this::onDragOver);
        listView.setOnDragDropped(this::onDragDropped);

        server.onConnection(this::onConnection);

    }

    public void addHostMonitor() {
        try {
            if (this.monitorSetup.findHost() == null) { // if host monitor not added to table
                // then add to list
                availableMonitors.add(new GridMonitor(InetAddress.getLocalHost().getHostName(), Toolkit.getDefaultToolkit().getScreenSize(), true, true));
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }

    private void onConnection(AsynchronousObjectSocketChannel client) {
        client.onMessage(MonitorInfo.class, (m) -> this.onMonitorInfo(client, m));
        client.onDisconnect(() -> this.onDisconnect(client));
    }

    private void onDisconnect(AsynchronousObjectSocketChannel client) {
        Platform.runLater(() -> {
            availableMonitors.remove(new GridMonitor(client.getHostName(), null, false, false));
        });
    }

    private void onMonitorInfo(AsynchronousObjectSocketChannel client, MonitorInfo m) {
        Platform.runLater(() -> {
    
            // if hes not in the setup/table already
            if(!this.monitorSetup.hasHost(client.getHostName())){
                // then add hime to the listview
                availableMonitors.add(new GridMonitor(client.getHostName(), new Dimension(m.getWidth(), m.getHeight()), false, true));
            }
        });
    }

    void add(GridMonitor monitor) {
        availableMonitors.add(monitor);
    }

    void remove(int index) {
        availableMonitors.remove(index);
    }

    private void onDragOver(DragEvent event) {
        GridMonitor content = (GridMonitor) event.getDragboard().getContent(MonitorTableCell.DATAFORMAT);
        // if there was dragged content....do i need to do this check???
        if (content != null) {
            // allow the move operation to be performed
            event.acceptTransferModes(TransferMode.MOVE);
        }
    }

    private void onDragDropped(DragEvent event) {
        GridMonitor content = (GridMonitor) event.getDragboard().getContent(MonitorTableCell.DATAFORMAT);
        if (content != null) {
            if(!content.isConnected()){
                this.monitorSetup.remove(content.getHostname());
            } else {
                add(content);
            }
            event.setDropCompleted(true);
            event.consume();
        }
    }

    public void setConfig(MonitorSetup monitorSetup) {
        this.monitorSetup = monitorSetup;
    }

    public void addClients() {
        this.server.getClients()
                .filter((c) -> this.monitorSetup.hasHost(c.getHostName()))
                .map((c) -> new GridMonitor(c.getHostName(), this.monitorSetup.getSize(c.getHostName()), false, true))
                .forEach((gm) -> this.availableMonitors.add(gm));
    }

}
