package com.mystery.kvm.setup.monitors;

import com.mystery.libmystery.event.EventEmitter;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class MonitorTableCellFactory implements Callback<TableColumn<GridRow, GridMonitor>, TableCell<GridRow, GridMonitor>>{
  
    private MonitorsPresenter controller;
    private EventEmitter emitter;
    
    public MonitorTableCellFactory(MonitorsPresenter controller, EventEmitter emitter) {
        this.controller = controller;
        this.emitter = emitter;
    }
    
    @Override
    public MonitorTableCell call(TableColumn<GridRow, GridMonitor> param) {
       MonitorTableCell cell = new MonitorTableCell(controller, emitter);
        return cell;
    }
}
