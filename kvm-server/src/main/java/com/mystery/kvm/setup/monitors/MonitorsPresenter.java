package com.mystery.kvm.setup.monitors;

import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.server.model.Monitor;
import com.mystery.kvm.server.model.MonitorSetup;
import com.mystery.kvm.tray.TrayMessage;
import com.mystery.libmystery.event.EventEmitter;
import com.mystery.libmystery.event.WeakDualHandler;
import com.mystery.libmystery.event.WeakHandler;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.ClientMessageHandler;
import com.mystery.libmystery.nio.ConnectionHandler;
import com.mystery.libmystery.nio.DisconnectHandler;
import com.mystery.libmystery.nio.MioServer;
import java.awt.Dimension;
import java.awt.TrayIcon;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javax.inject.Inject;

public class MonitorsPresenter implements Initializable {

    @Inject
    private int MONITOR_SETUP_GRID_SIZE;

    @FXML
    private TableView tableView;

    @Inject
    private MioServer server;
    
    @Inject 
    private EventEmitter emitter;
    
    @Inject
    private String monitorReconnectBalloonHeader;
    
    @Inject
    private String monitorReconnectBalloonText;

    private List<String> clients = new ArrayList<>();

    private final ObservableList<GridRow> tableModel = FXCollections.observableArrayList();

    void setTableValue(int row, int column, GridMonitor value) {
        tableModel.get(row).setCell(column, value);
    }

    private ObservableValue<GridMonitor> getCellValue(TableColumn.CellDataFeatures<GridRow, GridMonitor> param) {
        int columnIndex = param.getTableView().getColumns().indexOf(param.getTableColumn());
        return param.getValue().getCellProperty(columnIndex);
    }

    public void setConfig(MonitorSetup config) {

        for (int row = 0; row < MONITOR_SETUP_GRID_SIZE; row++) {
            for (int col = 0; col < MONITOR_SETUP_GRID_SIZE; col++) {
                Monitor monitor = config.getMonitor(col, row);
                if (monitor != null) {
                    boolean isConnected = monitor.isHost();
                    if (!isConnected) {
                        List<AsynchronousObjectSocketChannel> collect = server.getClients()
                                .filter((c) -> c.getHostName().equals(monitor.getHostname()))
                                .collect(Collectors.toList());
                        if (!collect.isEmpty()) {
                            isConnected = true;
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

        column.setCellValueFactory(this::getCellValue);
        column.setCellFactory(this::getCell);
        return column;
    }

    private TableCell<GridRow, GridMonitor> getCell(TableColumn<GridRow, GridMonitor> in) {
        MonitorTableCell cell = new MonitorTableCell(this, emitter);

        return cell;
    }

    @Override
    public void initialize(URL location, ResourceBundle rb) {
        tableView.setItems(tableModel);
        for (int i = 0; i < MONITOR_SETUP_GRID_SIZE; i++) {
            TableColumn<GridRow, GridMonitor> column = createColumn();
            tableModel.add(new GridRow(MONITOR_SETUP_GRID_SIZE));
            tableView.getColumns().add(column);
        }

        tableView.widthProperty().addListener((n) -> {
            Pane header = (Pane) tableView.lookup("TableHeaderRow");
            header.setMaxHeight(0);
            header.setMinHeight(0);
            header.setPrefHeight(0);
            header.setVisible(false);
            tableView.setLayoutY(header.getHeight() * -1);
            tableView.autosize();
        });

        
        tableView.setSelectionModel(new TableView.TableViewSelectionModel(tableView) {
            
            @Override
            public ObservableList getSelectedCells() {
                 return FXCollections.emptyObservableList();
            }
            
            @Override
            public boolean isSelected(int row, TableColumn column) {
                return false;
            }
            
            @Override
            public void select(int row, TableColumn column) {
            }
            
            @Override
            public void clearAndSelect(int row, TableColumn column) {
            }
            
            @Override
            public void clearSelection(int row, TableColumn column) {
            }
            
            @Override
            public void selectLeftCell() {
            }
            
            @Override
            public void selectRightCell() {
            }
            
            @Override
            public void selectAboveCell() {
            }
            
            @Override
            public void selectBelowCell() {
            }
        });
        
        System.out.println("adding onConnection here++++");
        server.onConnection(new WeakHandler<>(this.onConnection));

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

    private final ConnectionHandler onConnection = (AsynchronousObjectSocketChannel client) -> {
        client.onDisconnect(new WeakHandler<>(this.onDisconnect));
        setClientConnected(client.getHostName(), true);
        client.onMessage(MonitorInfo.class, new WeakDualHandler<>(this.onMonitorInfo));
    };
    
    private final ClientMessageHandler<MonitorInfo> onMonitorInfo = (client, m) -> {
        System.out.println("onMonitorInfo - monitorsPresenter");
        Platform.runLater(() -> {
            if (this.getConfig().hasHost(client.getHostName())) {
                emitter.emit(TrayMessage.class, new TrayMessage(monitorReconnectBalloonHeader,m.getHostName() +  monitorReconnectBalloonText, TrayIcon.MessageType.INFO));
            }
        });
    };
    
    private final DisconnectHandler onDisconnect = (AsynchronousObjectSocketChannel client) -> {
        setClientConnected(client.getHostName(), false);
    };

}
