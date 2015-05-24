package com.mystery.kvm.setup.connections;

import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.server.model.MonitorSetup;
import com.mystery.kvm.setup.monitors.GridMonitor;
import com.mystery.kvm.setup.monitors.MonitorTableCell;
import com.mystery.kvm.setup.monitors.MonitorTableCell.RemoveFromGridEvent;
import com.mystery.kvm.tray.TrayMessage;
import com.mystery.libmystery.event.EventEmitter;
import com.mystery.libmystery.event.Handler;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.ClientMessageHandler;
import com.mystery.libmystery.nio.ConnectionHandler;
import com.mystery.libmystery.nio.DisconnectHandler;
import com.mystery.libmystery.nio.MioServer;
import com.mystery.libmystery.event.WeakDualHandler;
import com.mystery.libmystery.event.WeakHandler;
import com.mystery.libmystery.injection.Inject;
import com.mystery.libmystery.injection.PostConstruct;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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


public class ConnectionsPresenter implements Initializable{

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

    private MonitorSetup monitorSetup;

    @Override
    public void initialize(URL url, ResourceBundle bundle) {

        listView.setItems(availableMonitors);
        listView.setCellFactory((e) -> new ConnectionsListCell(availableMonitors, server));

        listView.setOnDragOver(new WeakEventHandler<>(this.onDragOver));
        listView.setOnDragDropped(new WeakEventHandler<>(this.onDragDropped));

        server.onConnection(new WeakHandler<>(this.onConnection));

        emitter.on("stage.hide", new WeakHandler<>(this.onStageHide));
        emitter.on(RemoveFromGridEvent.class, new WeakHandler<>(this.onRemovedFromGrid));
        
    }

    private final Handler<Void> onStageHide = (v) -> {
        listView.setCellFactory(null); // prevents leak
    };

    private final Handler<RemoveFromGridEvent> onRemovedFromGrid = (RemoveFromGridEvent v) -> {
        Platform.runLater(()->{
            availableMonitors.add(v.getMonitor());
        });  
    };

    public void addHostMonitor() {
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

    private ClientMessageHandler<MonitorInfo> onMonitorInfo = (client, m) -> {
        System.out.println("onMonitorInfo");
        Platform.runLater(() -> {

            // if hes not in the setup/table already
            if (!this.monitorSetup.hasHost(client.getHostName())) {
                // then add him to the listview
                GridMonitor gridMonitor = new GridMonitor(client.getHostName(), new Dimension(m.getWidth(), m.getHeight()), false, true , m.getHostName());
               availableMonitors.add(gridMonitor);
                emitter.emit(TrayMessage.class, new TrayMessage(newMonitorBalloonHeader, m.getHostName() + newMonitorBalloonText, TrayIcon.MessageType.INFO));
            }
        });

    };
    
    private final ConnectionHandler onConnection = (AsynchronousObjectSocketChannel client) -> {
        client.onMessage(MonitorInfo.class, new WeakDualHandler<>(onMonitorInfo));
        client.onDisconnect(new WeakHandler<>(this.onDisconnect));
    };

    private final DisconnectHandler onDisconnect = (AsynchronousObjectSocketChannel client) -> {
        System.out.println("++++onDisconnect======");
        Platform.runLater(() -> {
            availableMonitors.removeIf((m) -> m.getHostname().equals(client.getHostName()));
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

    public void setConfig(MonitorSetup monitorSetup) {
        this.monitorSetup = monitorSetup;
    }

    public void addClients() {
        this.server.getClients()
                .filter((c) -> this.monitorSetup.hasHost(c.getHostName()))
                .map((c) -> new GridMonitor(c.getHostName(), this.monitorSetup.getSize(c.getHostName()), false, true, this.monitorSetup.getAlias(c.getHostName())))
                .forEach((gm) -> this.availableMonitors.add(gm));
    }

    

}
