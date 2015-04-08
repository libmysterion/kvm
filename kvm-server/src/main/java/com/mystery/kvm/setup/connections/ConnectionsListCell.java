package com.mystery.kvm.setup.connections;

import com.mystery.kvm.setup.monitors.GridMonitor;
import com.mystery.kvm.setup.monitors.MonitorTableCell;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class ConnectionsListCell extends ListCell<GridMonitor> {

    
    private ConnectionsPresenter presenter;

    ConnectionsListCell(ConnectionsPresenter presenter) {
        this.presenter = presenter;
        setOnDragDetected(this::onDragDetected);
        setOnDragDone(this::onDragDone);
    }

    @Override
    protected void updateItem(GridMonitor item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            this.setText(item.getHostname());
        } else {
            this.setText(null);
        }
    }

    private void onDragDetected(MouseEvent event) {
        // a drag event is starting with this as the source
        if (getItem() != null) {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();

            content.put(MonitorTableCell.DATAFORMAT, getItem());
            db.setContent(content);
            event.consume();
        }
    }

    private void onDragDone(DragEvent event) {
        if (event.getAcceptedTransferMode() == TransferMode.MOVE) {
            presenter.remove(getIndex());
        }
    }

}
