package com.mystery.kvm.setup.connections;

import com.mystery.kvm.setup.monitors.GridMonitor;
import com.mystery.kvm.setup.monitors.MonitorTableCell;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class ConnectionsListCell extends ListCell<GridMonitor> {

     
    private ConnectionsPresenter presenter;
    private final ContextMenu menu = new ContextMenu();

    ConnectionsListCell(ConnectionsPresenter presenter) {
        this.presenter = presenter;
        setOnDragDetected(new WeakEventHandler<>(this.onDragDetected));
        setOnDragDone(new WeakEventHandler<>(this.onDragDone));
            
        MenuItem removeMenuItem = new MenuItem("Remove monitor");
        menu.getItems().add(removeMenuItem);
        removeMenuItem.setOnAction(new WeakEventHandler<>(this.onRemoveClientClicked));
        setContextMenu(menu);
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

    private EventHandler<MouseEvent> onDragDetected = (MouseEvent event) -> {
        // a drag event is starting with this as the source
        if (getItem() != null) {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();

            content.put(MonitorTableCell.DATAFORMAT, getItem());
            db.setContent(content);
            event.consume();
        }
    };

    private EventHandler<DragEvent> onDragDone = (DragEvent event)->{
        if (event.getAcceptedTransferMode() == TransferMode.MOVE) {
            presenter.remove(getIndex());
        }
    };
    
    private EventHandler<ActionEvent> onRemoveClientClicked = (ActionEvent event)-> {
        this.presenter.disconnectClient(this.getItem());
    };

}
