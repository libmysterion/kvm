
package com.mystery.kvm.setup.monitors;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class GridRow {

    private int size;

    private final ObservableList<SimpleObjectProperty<GridMonitor>> columns = FXCollections.observableArrayList();

    public GridRow(int size) {
        this.size = size;               
        for (int i = 0; i < size; i++) {
            columns.add(new SimpleObjectProperty<>());
        }
    }

    SimpleObjectProperty<GridMonitor> getCellProperty(int column) {
        return columns.get(column);
    }

    void setCell(int column, GridMonitor monitor) {
        getCellProperty(column).set(monitor);
    }

    GridMonitor getCell(int column) {
        return getCellProperty(column).get();
    }
    
    ObservableList<SimpleObjectProperty<GridMonitor>> getColumns(){
        return columns;
    }

}
