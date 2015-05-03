package com.mystery.kvm.setup.connections;

import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.server.model.MonitorSetup;
import com.mystery.kvm.setup.monitors.GridMonitor;
import com.mystery.kvm.setup.monitors.MonitorTableCell;
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
import javax.inject.Inject;

public class ConnectionsPresenter implements Initializable {

    @FXML
    private ListView listView;

    @Inject
    private MioServer server;

    @Inject
    private EventEmitter emitter;

    private final ObservableList<GridMonitor> availableMonitors = FXCollections.observableArrayList();
    private final Map<AsynchronousObjectSocketChannel, GridMonitor> availableMonitorsMap = new HashMap<>();

    @Inject
    private String newMonitorBalloonHeader;

    @Inject
    private String newMonitorBalloonText;

    private MonitorSetup monitorSetup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        listView.setItems(availableMonitors);
        listView.setCellFactory((e) -> new ConnectionsListCell(this));

        listView.setOnDragOver(new WeakEventHandler<>(this.onDragOver));
        listView.setOnDragDropped(new WeakEventHandler<>(this.onDragDropped));

        server.onConnection(new WeakHandler<>(this.onConnection));  // weak handler will allow GC and remove itself

        emitter.on("stage.hide", new WeakHandler<>(this.onStageHide));
    }

    private final Handler<Void> onStageHide = (v) -> {
        listView.setCellFactory(null); // prevents leak
    };

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

    private ClientMessageHandler<MonitorInfo> onMonitorInfo = (client, m) -> {
        System.out.println("onMonitorInfo");
        Platform.runLater(() -> {

            // if hes not in the setup/table already
            if (!this.monitorSetup.hasHost(client.getHostName())) {
                // then add him to the listview
                GridMonitor gridMonitor = new GridMonitor(client.getHostName(), new Dimension(m.getWidth(), m.getHeight()), false, true);
                availableMonitorsMap.put(client, gridMonitor);
                availableMonitors.add(gridMonitor);
                emitter.emit(TrayMessage.class, new TrayMessage(newMonitorBalloonHeader, newMonitorBalloonText, TrayIcon.MessageType.INFO));
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
            remove(client);
        });
    };

    void add(GridMonitor monitor) {
        availableMonitors.add(monitor);
    }

    void remove(int index) {
        GridMonitor get = availableMonitors.get(index);
        if (get != null) {
            AsynchronousObjectSocketChannel client = keyFromValue(availableMonitorsMap, get);
            availableMonitorsMap.remove(client);
            availableMonitors.remove(index);
        }
    }

    private <K, V> K keyFromValue(Map<K, V> map, V value) {
        for (Entry<K, V> e : map.entrySet()) {
            if (e.getValue() == value) {
                return e.getKey();
            }
        }
        return null;
    }

    void remove(AsynchronousObjectSocketChannel client) {
        GridMonitor get = availableMonitorsMap.get(client);
        if (get != null) {
            availableMonitors.remove(get);
            availableMonitorsMap.remove(client);
            System.out.println("=======remover it all");
        }
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
                .map((c) -> new GridMonitor(c.getHostName(), this.monitorSetup.getSize(c.getHostName()), false, true))
                .forEach((gm) -> this.availableMonitors.add(gm));
    }

    void disconnectClient(GridMonitor item) {
        try {
            Optional<AsynchronousObjectSocketChannel> optional = server.getClients()
                    .filter((AsynchronousObjectSocketChannel c) -> c.getHostName().equals(item.getHostname()))
                    .findFirst();
            if (optional.isPresent()) {
                server.disconnectClient(optional.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
