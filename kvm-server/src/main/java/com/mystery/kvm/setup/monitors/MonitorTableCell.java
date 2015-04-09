package com.mystery.kvm.setup.monitors;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MonitorTableCell extends TableCell<GridRow, GridMonitor> {

    public static DataFormat DATAFORMAT = new DataFormat("com.mystery.kvm.setup.monitors.MonitorTableCell");

    private int SETUP_TABLE_CELL_SIZE = 100;
    private int LABEL_HEIGHT = 20;

    private MonitorsPresenter presenter;

    Background dragTargetBackGround = new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY));
    Background normalBackGround = new Background(new BackgroundFill(Color.WHITESMOKE, CornerRadii.EMPTY, Insets.EMPTY));

    MonitorTableCell(MonitorsPresenter presenter) {
        this.presenter = presenter;

        setBackground(normalBackGround);
        setPrefSize(SETUP_TABLE_CELL_SIZE, SETUP_TABLE_CELL_SIZE);

        setOnDragDetected(this::onDragDetected);

        setOnDragOver(this::onDragOver);
        setOnDragEntered(this::onDragEntered);
        setOnDragDropped(this::onDragDropped);
        setOnDragDone(this::onDragDone);
        setOnDragExited(this::onDragExited);
    }

    private int getRow() {
        return this.getTableRow().getIndex();
    }

    private int getColumn() {
        return getTableView().getColumns().indexOf(getTableColumn());
    }

    private void onDragDetected(MouseEvent event) {
        // a drag event is starting with this as the source
        if (getItem() != null) {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();

            content.put(DATAFORMAT, getItem());
            db.setContent(content);
            event.consume();
        }
    }

    private void onDragOver(DragEvent event) {
        // i will only accept if i am empty
        if (getItem() == null) {
            GridMonitor content = (GridMonitor) event.getDragboard().getContent(DATAFORMAT);
            // if there was dragged content....do i need to do this check???
            if (content != null) {
                // allow the move operation to be performed
                event.acceptTransferModes(TransferMode.MOVE);
            }
        }
    }

    private void onDragEntered(DragEvent event) {
        // i will only accept if i am empty, therfore only do visual cue on same condition
        if (getItem() == null) {
            GridMonitor content = (GridMonitor) event.getDragboard().getContent(DATAFORMAT);
            if (content != null) {
                setBackground(dragTargetBackGround);
            }
        }
    }

    private void onDragDropped(DragEvent event) {
        GridMonitor content = (GridMonitor) event.getDragboard().getContent(DATAFORMAT);
        if (content != null) {
            setBackground(normalBackGround);
            this.presenter.setTableValue(getRow(), getColumn(), content);
            event.setDropCompleted(true);
            event.consume();
        }
    }

    private void onDragDone(DragEvent event) {
        if (event.getAcceptedTransferMode() == TransferMode.MOVE) {
            presenter.setTableValue(getRow(), getColumn(), null);
        }
    }

    private void onDragExited(DragEvent event) {
        setBackground(normalBackGround);
    }

    private void setConnectedGraphic() {
        ImageView icon = createIcon("/server/icons/monitor-icon-256x256.png");
        buildCellNode(icon);
    }

    private void setDisconnectedGraphic() {
        ImageView icon = createIcon("/server/icons/monitor-icon-greyscale-256x256.png");
        buildCellNode(icon);
    }

    private void buildCellNode(ImageView icon) {
        VBox box = new VBox();
        GridMonitor item = this.getItem();
        Label label = new Label(item.getHostname());
        box.setSpacing(0);
        box.getChildren().addAll(icon, label);
        box.setPrefSize(SETUP_TABLE_CELL_SIZE, SETUP_TABLE_CELL_SIZE);
        this.setGraphic(box);
    }

    private void connectedPropertyChanged(ObservableValue<? extends Boolean> observable, boolean wasConnected, boolean isConnected) {
        if (isConnected) {
            setConnectedGraphic();
        } else {
            setDisconnectedGraphic();
        }
    }

    @Override
    protected void updateItem(GridMonitor newItem, boolean empty) {

        GridMonitor oldItem = super.getItem();
        if (oldItem !=null) {
            oldItem.connectedProperty().removeListener(this::connectedPropertyChanged);
        }
        
        super.updateItem(newItem, empty);

        if (newItem != null) {
            newItem.connectedProperty().addListener(this::connectedPropertyChanged);

            if (newItem.isConnected()) {
                setConnectedGraphic();
            } else {
                setDisconnectedGraphic();
            }

        } else {
            this.setGraphic(null);
        }

    }

    private ImageView createIcon(String path) {
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(path)));
        imageView.setFitHeight(SETUP_TABLE_CELL_SIZE - LABEL_HEIGHT);
        imageView.setFitWidth(SETUP_TABLE_CELL_SIZE);
        return imageView;
    }

}