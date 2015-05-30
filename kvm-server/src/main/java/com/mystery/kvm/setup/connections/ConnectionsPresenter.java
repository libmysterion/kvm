package com.mystery.kvm.setup.connections;

import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.server.model.MonitorSetup;
import com.mystery.kvm.setup.monitors.GridMonitor;
import com.mystery.kvm.setup.monitors.MonitorTableCell;
import com.mystery.kvm.setup.monitors.MonitorTableCell.RemoveFromGridEvent;
import com.mystery.libmystery.event.DualHandler;
import com.mystery.libmystery.event.EventEmitter;
import com.mystery.libmystery.event.Handler;
import com.mystery.libmystery.event.WeakDualHandler;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.ConnectionHandler;
import com.mystery.libmystery.nio.DisconnectHandler;
import com.mystery.libmystery.nio.MioServer;
import com.mystery.libmystery.event.WeakHandler;
import com.mystery.libmystery.injection.Inject;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;

public class ConnectionsPresenter implements Initializable {

    @FXML
    private ListView listView;

    @Inject
    private MioServer server;

    @Inject
    private EventEmitter emitter;

    private final ObservableList<GridMonitor> availableMonitors = FXCollections.observableArrayList();

    @Inject
    private String newMonitorBalloonHeader;

    @Inject
    private String newMonitorBalloonText;

    @Inject
    private ConnectionsService connectionsService;

    @Inject
    private MonitorSetup monitorSetup;

    @Override
    public void initialize(URL url, ResourceBundle bundle) {

        listView.setItems(availableMonitors);
        listView.setCellFactory((e) -> new ConnectionsListCell(availableMonitors, connectionsService));

        listView.setOnDragOver(new WeakEventHandler<>(this.onDragOver));
        listView.setOnDragDropped(new WeakEventHandler<>(this.onDragDropped));

        emitter.on("stage.hide", new WeakHandler<>(this.onStageHide));

        emitter.on("client.connect", new WeakDualHandler<AsynchronousObjectSocketChannel, MonitorInfo>(this.clientConnected));
        emitter.on("client.disconnect", new WeakDualHandler<AsynchronousObjectSocketChannel, MonitorInfo>(this.clientDisconnected));

        emitter.on(RemoveFromGridEvent.class, new WeakHandler<>(this.onRemovedFromGrid));

        addHostMonitor();
        addClients();

    }

    private final Handler<Void> onStageHide = (v) -> {
        listView.setCellFactory(null); // prevents leak
    };

    private final Handler<RemoveFromGridEvent> onRemovedFromGrid = (RemoveFromGridEvent v) -> {
        Platform.runLater(() -> {
            availableMonitors.add(v.getMonitor());
        });
    };

    private void addHostMonitor() {
        try {
            if (this.monitorSetup.findHost() == null) { // if host monitor not added to table
                // then add to list
                String hostHostName = InetAddress.getLocalHost().getHostName();
                availableMonitors.add(new GridMonitor(hostHostName, Toolkit.getDefaultToolkit().getScreenSize(), true, true, hostHostName));
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }

    private final DualHandler<AsynchronousObjectSocketChannel, MonitorInfo> clientConnected = (client, monitorInfo) -> {
        if (!monitorSetup.hasMonitor(monitorInfo)) {
            GridMonitor gridMonitor = new GridMonitor(monitorInfo.getHostName(), new Dimension(monitorInfo.getWidth(), monitorInfo.getHeight()), false, true, monitorInfo.getHostName());
            Platform.runLater(() -> {
                availableMonitors.add(gridMonitor);
            });
        }
    };

    private final DualHandler<AsynchronousObjectSocketChannel, MonitorInfo> clientDisconnected = (c, monitor) -> {
        Platform.runLater(() -> {
            availableMonitors.removeIf((m) -> m.getHostname().equals(monitor.getHostName()));
        });
    };

    void add(GridMonitor monitor) {
        availableMonitors.add(monitor);
    }

    void remove(int index) {
        availableMonitors.remove(index);
    }

    private EventHandler<DragEvent> onDragOver = (DragEvent event) -> {
        GridMonitor content = (GridMonitor) event.getDragboard().getContent(MonitorTableCell.DATAFORMAT);
        // if there was dragged content....do i need to do this check???
        if (content != null) {
            // allow the move operation to be performed
            event.acceptTransferModes(TransferMode.MOVE);
        }
    };

    private EventHandler<DragEvent> onDragDropped = (DragEvent event) -> {
        GridMonitor content = (GridMonitor) event.getDragboard().getContent(MonitorTableCell.DATAFORMAT);
        if (content != null) {
            if (!content.isConnected()) {
                this.monitorSetup.remove(content.getHostname());
            } else {
                add(content);
            }
            event.setDropCompleted(true);
            event.consume();
        }
    };

    private void addClients() {
        Map<AsynchronousObjectSocketChannel, MonitorInfo> clientMap = connectionsService.getClientMap();

        for (Entry<AsynchronousObjectSocketChannel, MonitorInfo> e : clientMap.entrySet()) {
            AsynchronousObjectSocketChannel client = e.getKey();
            MonitorInfo monitorInfo = e.getValue();
            if (!monitorSetup.hasMonitor(monitorInfo)) {
                GridMonitor gridMonitor = new GridMonitor(monitorInfo.getHostName(), new Dimension(monitorInfo.getWidth(), monitorInfo.getHeight()), false, true, monitorInfo.getHostName());
                availableMonitors.add(gridMonitor);
            }

        }
    }

}
