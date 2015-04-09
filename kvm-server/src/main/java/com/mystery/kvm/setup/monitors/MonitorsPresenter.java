package com.mystery.kvm.setup.monitors;

import com.mystery.kvm.server.model.Monitor;
import com.mystery.kvm.server.model.MonitorSetup;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.MioServer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
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

//    @Inject
//    private MouseMover mouseMover;
    @FXML
    private TableView tableView;

    @Inject
    MioServer server;

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
                    GridMonitor gridMonitor = new GridMonitor(monitor.getHostname(), monitor.getSize(), monitor.isHost(), monitor.isConnected());
                    this.tableModel.get(row).setCell(col, gridMonitor);
                }
            }
        }
    }

    public MonitorSetup getConfig() {
        MonitorSetup rv = new MonitorSetup();
        for (int row = 0; row < MONITOR_SETUP_GRID_SIZE; row++) {
            for (int col = 0; col < MONITOR_SETUP_GRID_SIZE; col++) {
                GridMonitor viewModel = this.tableModel.get(row).getCell(col);
                if (viewModel != null) {
                    rv.addMonitor(new Monitor(viewModel.getHostname(), viewModel.getSize(), viewModel.isHost(), viewModel.isHost(), col, row, viewModel.isConnected()));
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
        MonitorTableCell cell = new MonitorTableCell(this);
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

        server.onConnection(this::onConnection);

    }

    private void setClientConnected(String hostname, boolean connected) {
        Platform.runLater(() -> {
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

    private void onConnection(AsynchronousObjectSocketChannel client) {
        client.onDisconnect(this::onDisconnect);

        setClientConnected(client.getHostName(), true);
    }

    private void onDisconnect(AsynchronousObjectSocketChannel client) {
        setClientConnected(client.getHostName(), false);
    }

}
