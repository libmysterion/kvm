package com.mystery.kvm.setup.monitors;

import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.server.KVMServer.KVMServer;
import com.mystery.kvm.server.model.Monitor;
import com.mystery.kvm.server.model.MonitorSetup;
import com.mystery.kvm.setup.connections.ConnectionsService;
import com.mystery.kvm.tray.TrayMessage;
import com.mystery.libmystery.event.DualHandler;
import com.mystery.libmystery.event.EventEmitter;
import com.mystery.libmystery.event.Handler;
import com.mystery.libmystery.event.WeakDualHandler;
import com.mystery.libmystery.event.WeakHandler;
import com.mystery.libmystery.injection.Inject;
import com.mystery.libmystery.injection.Property;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.ClientMessageHandler;
import com.mystery.libmystery.nio.ConnectionHandler;
import com.mystery.libmystery.nio.DisconnectHandler;
import com.mystery.libmystery.nio.MioServer;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;

public class MonitorsPresenter implements Initializable {

    @Property
    private int MONITOR_SETUP_GRID_SIZE;

    @FXML
    private TableView tableView;

    @Inject
    private EventEmitter emitter;

    @Inject
    private MonitorSetup monitorSetup;

    @Property
    private String monitorReconnectBalloonHeader;

    @Property
    private String monitorReconnectBalloonText;

    @Inject
    private KVMServer kvm;
    
    @Inject
    private ConnectionsService connectionsService;

    private final ObservableList<GridRow> tableModel = FXCollections.observableArrayList();

    void setTableValue(int row, int column, GridMonitor value) {
        tableModel.get(row).setCell(column, value);
    }

    private ObservableValue<GridMonitor> getCellValue(TableColumn.CellDataFeatures<GridRow, GridMonitor> param) {
        int columnIndex = param.getTableView().getColumns().indexOf(param.getTableColumn());
        return param.getValue().getCellProperty(columnIndex);
    }

    private void setConfig() {
        for (int row = 0; row < MONITOR_SETUP_GRID_SIZE; row++) {
            for (int col = 0; col < MONITOR_SETUP_GRID_SIZE; col++) {
                Monitor monitor = this.monitorSetup.getMonitor(col, row);
                if (monitor != null) {
                    boolean isConnected = monitor.isHost();
                    if (!isConnected) {
 
                        for(MonitorInfo mon : connectionsService.getClientMap().values()){
                            if(mon.getHostName().equals(monitor.getHostname())){
                                isConnected = true;
                                break;
                            }
                        }
                        
                    }
                    // todo needs to put the alias in th saved config probably
                    GridMonitor gridMonitor = new GridMonitor(monitor.getHostname(), monitor.getSize(), monitor.isHost(), isConnected, monitor.getAlias());
                    this.tableModel.get(row).setCell(col, gridMonitor);
                }
            }
        }
    }

    public MonitorSetup getConfig() {
        MonitorSetup rv = new MonitorSetup(false);

        for (int row = 0; row < MONITOR_SETUP_GRID_SIZE; row++) {
            for (int col = 0; col < MONITOR_SETUP_GRID_SIZE; col++) {
                GridMonitor viewModel = this.tableModel.get(row).getCell(col);
                if (viewModel != null) {
                    rv.addMonitor(new Monitor(viewModel.getHostname(), viewModel.getSize(), viewModel.isHost(), viewModel.isHost(), col, row, viewModel.isConnected(), viewModel.getAlias()));
                }
            }
        }
        return rv;
    }

//    private int CELLSIZE = 9;
    private TableColumn<GridRow, GridMonitor> createColumn() {
        TableColumn<GridRow, GridMonitor> column = new TableColumn();
        //      column.setPrefWidth(CELLSIZE);

        column.setCellValueFactory((param) -> {
            int columnIndex = param.getTableView().getColumns().indexOf(param.getTableColumn());
            return param.getValue().getCellProperty(columnIndex);
        });
        column.setCellFactory(new MonitorTableCellFactory(this, emitter));
        return column;
    }

    private final Handler<Void> onStageHide = (v) -> {
        MonitorSetup config = getConfig();
        try {
            config.save();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        kvm.setConfiguration(config);

        tableView.getColumns().forEach(new Consumer<TableColumn<GridRow, GridMonitor>>() {

            public void accept(TableColumn<GridRow, GridMonitor> c) {
                c.setCellFactory((e) -> null);
            }
        });
        tableView.getColumns().removeAll(tableView.getColumns());
    };

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        tableView.setItems(tableModel);
        for (int i = 0; i < MONITOR_SETUP_GRID_SIZE; i++) {
            TableColumn<GridRow, GridMonitor> column = createColumn();
            tableModel.add(new GridRow(MONITOR_SETUP_GRID_SIZE));
            tableView.getColumns().add(column);
        }

        emitter.on("stage.hide", new WeakHandler(onStageHide));

        tableView.widthProperty().addListener(new TableColumnHiderListener(tableView));

        tableView.setSelectionModel(new NullTableSelectionModel(tableView));

        emitter.on("client.connect", new WeakDualHandler<AsynchronousObjectSocketChannel, MonitorInfo>(this.clientConnected));
        emitter.on("client.disconnect", new WeakDualHandler<AsynchronousObjectSocketChannel, MonitorInfo>(this.clientDisconnected));

        this.setConfig();
    }

    private void setClientConnected(String hostname, boolean connected) {
        Platform.runLater(() -> {
            System.out.println("setClientConnected:" + hostname + ":" + connected);

            for (GridRow r : tableModel) {
                for (int column = 0; column < MONITOR_SETUP_GRID_SIZE; column++) {

                    GridMonitor cellValue = r.getCell(column);

                    if (cellValue != null && cellValue.getHostname().equals(hostname)) {
                        cellValue.setConnected(connected);
                        return;
                    }
                }
            }
        });
    }

    private final DualHandler<AsynchronousObjectSocketChannel, MonitorInfo> clientConnected = (client, monitor) -> {
        setClientConnected(monitor.getHostName(), true);
    };

    private final DualHandler<AsynchronousObjectSocketChannel, MonitorInfo> clientDisconnected = (client, monitor) -> {
        setClientConnected(monitor.getHostName(), false);
    };

    // todo - show reconnected popup balloon - but only implement it in one fucking place
//    private final ClientMessageHandler<MonitorInfo> onMonitorInfo = (client, m) -> {
//        System.out.println("onMonitorInfo - monitorsPresenter");
//        Platform.runLater(() -> {
//            
//            MonitorInfo get = this.connectionsService.getClientMap().get(client);
//            
//            if (this.getConfig().hasHost(client.getHostName())) {
//                emitter.emit(TrayMessage.class, new TrayMessage(monitorReconnectBalloonHeader, m.getHostName() + monitorReconnectBalloonText, TrayIcon.MessageType.INFO));
//            }
//        });
//    };
}

class TableColumnHiderListener implements InvalidationListener {

    private TableView tableView;

    public TableColumnHiderListener(TableView tableView) {
        this.tableView = tableView;
    }

    public void invalidated(Observable n) {
        Pane header = (Pane) tableView.lookup("TableHeaderRow");
        header.setMaxHeight(0);
        header.setMinHeight(0);
        header.setPrefHeight(0);
        header.setVisible(false);
        tableView.setLayoutY(header.getHeight() * -1);
        tableView.autosize();
    }

}
