package com.mystery.kvm.setup.connections;

import com.mystery.kvm.setup.monitors.GridMonitor;
import com.mystery.kvm.setup.monitors.MonitorTableCell;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.MioServer;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class ConnectionsListCell extends ListCell<GridMonitor> {

    private ObservableList<GridMonitor> availableMonitors; 
    private final ContextMenu menu = new ContextMenu();
    private final MioServer mioServer; 
    
    ConnectionsListCell( ObservableList<GridMonitor> availableMonitors, MioServer mioServer) {
        this.mioServer = mioServer;
        this.availableMonitors = availableMonitors;
        setOnDragDetected(new WeakEventHandler<>(this.onDragDetected));
        setOnDragDone(new WeakEventHandler<>(this.onDragDone));

        MenuItem removeMenuItem = new MenuItem("Remove monitor");
        MenuItem aliasMenuItem = new MenuItem("Rename");

        menu.getItems().addAll(removeMenuItem, aliasMenuItem);
        removeMenuItem.setOnAction(new WeakEventHandler<>(this.onRemoveClientClicked));
        aliasMenuItem.setOnAction(new WeakEventHandler<>(this.onRenameClientClicked));

        setContextMenu(menu);
    }

    @Override
    protected void updateItem(GridMonitor item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            this.setText(item.getAlias());
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

    private EventHandler<DragEvent> onDragDone = (DragEvent event) -> {
        if (event.getAcceptedTransferMode() == TransferMode.MOVE) {
            availableMonitors.remove(getIndex());
        }
    };

    private EventHandler<ActionEvent> onRemoveClientClicked = (ActionEvent event) -> {
        this.disconnectClient(this.getItem());
    };

    private final EventHandler<ActionEvent> onRenameClientClicked = (ActionEvent event) -> {

        TextInputDialog dialog = new TextInputDialog(getItem().getAlias());
        
        // todo set the monitor graphic
        //dialog.setGraphic(this);
        
        
        dialog.setTitle("Rename Monitor");
        dialog.setContentText("Monitor Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            this.getItem().setAlias(name);
            this.setText(getItem().getAlias());
        });
    };
    
    // todo - bug heere ---we need to use the nonitor info to compare hostnames
    private void disconnectClient(GridMonitor item) {
        try {
            Optional<AsynchronousObjectSocketChannel> optional = mioServer.getClients()
                    .filter((AsynchronousObjectSocketChannel c) -> c.getHostName().equals(item.getHostname()))
                    .findFirst();
            if (optional.isPresent()) {
                mioServer.disconnectClient(optional.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
