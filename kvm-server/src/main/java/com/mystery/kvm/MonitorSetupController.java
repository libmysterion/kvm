package com.mystery.kvm;

import static com.mystery.kvm.MonitorSetupController.TABLESIZE;
import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.model.Monitor;
import com.mystery.kvm.model.MonitorSetup;
import com.mystery.kvm.server.KVMServer.MouseLogicManagerThing;
import com.mystery.libmystery.nio.MioServer;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MonitorSetupController implements Initializable {

    private final ObservableList<GridMonitor> connectedClients = FXCollections.observableArrayList();

    private final ObservableList<GridMonitor> monitorSetup = FXCollections.observableArrayList();

    private final ObservableList<GridRow> tableRows = FXCollections.observableArrayList();

    private DataFormat listDataFormat = new DataFormat("list.dragndrop");
    private DataFormat tableDataFormat = new DataFormat("table.dragndrop");

    private MioServer server;

    @FXML
    private Label label;

    @FXML
    private ListView<GridMonitor> availableMonitorsListBox;
    
    @FXML
    private Button startButton;

    @FXML
    private TableView<GridRow> monitorsTable;
    private MouseMover mover;

    public MonitorSetup getConfig() {
        MonitorSetup m = new MonitorSetup(TABLESIZE);
        for (int row = 0; row < TABLESIZE; row++) {
            for (int col = 0; col < TABLESIZE; col++) {
                GridMonitor viewModel = this.tableRows.get(row).get(col).get();
                if (viewModel != null) {
                     Monitor model = new Monitor(viewModel.getHostname(),viewModel.getSize(), viewModel.isHost(), viewModel.isHost());
                     m.addMonitor(col, row, model);
                }
            }
        }
        return m;
    }

    static final double CELLSIZE = 100;
    final static int TABLESIZE = 5;

    private Stage stage;
    
    private void startMouseMonitor(){
        
        MouseLogicManagerThing thing = new MouseLogicManagerThing(this.getConfig(), mover);
        stage.hide();
        
    }
    
    public void setStage(Stage stage){
        this.stage = stage;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        startButton.setOnAction((a)->{
            this.startMouseMonitor();
        });
        
                
        try {
            GridMonitor gridMonitor = new GridMonitor(InetAddress.getLocalHost().getHostName(), Toolkit.getDefaultToolkit().getScreenSize(), true);
            connectedClients.add(gridMonitor);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }

                        
        availableMonitorsListBox.setItems(connectedClients);
        monitorsTable.setItems(tableRows);

        for (int i = 0; i < TABLESIZE; i++) {
            TableColumn<GridRow, GridMonitor> col1 = new TableColumn();
            col1.setPrefWidth(CELLSIZE);

            tableRows.add(new GridRow());
            final int columnIndex = i;
            col1.setCellValueFactory((e) -> {
                return e.getValue().get(columnIndex);
            });
            col1.setCellFactory((column) -> {
                TableCell<GridRow, GridMonitor> cell = new TableCell<GridRow, GridMonitor>() {

                    @Override
                    protected void updateItem(GridMonitor item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            HBox box = new HBox();
                            box.setSpacing(2);
                            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/server/icons/monitor-icon.png")));
                            imageView.setFitHeight(CELLSIZE);
                            imageView.setFitWidth(CELLSIZE);
                            box.getChildren().add(imageView);
                            box.setPrefSize(CELLSIZE, CELLSIZE);
                            this.setGraphic(box);
                        } else {
                            this.setGraphic(null);
                        }
                    }

                };
                Background dragTargetBackGround = new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY));
                Background normalBackGround = new Background(new BackgroundFill(Color.WHITESMOKE, CornerRadii.EMPTY, Insets.EMPTY));
                cell.setBackground(normalBackGround);

                cell.setPrefSize(CELLSIZE, CELLSIZE);

                cell.setOnDragDetected((e) -> {
                    if (cell.getItem() == null) {
                        return;
                    }

                    Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
                    ClipboardContent content = new ClipboardContent();

                    content.put(listDataFormat, cell.getItem());
                    content.put(tableDataFormat, new TableDrag(cell.getTableRow().getIndex(), columnIndex));

                    db.setContent(content);
                    e.consume();
                });

                cell.setOnDragOver((e) -> {
                    if (cell.getItem() == null) {
                        GridMonitor content = (GridMonitor) e.getDragboard().getContent(listDataFormat);
                        if (content != null) {
                            e.acceptTransferModes(TransferMode.ANY);
                        }
                    }

                });

                cell.setOnDragEntered((e) -> {
                    if (cell.getItem() == null) {
                        GridMonitor content = (GridMonitor) e.getDragboard().getContent(listDataFormat);
                        if (content != null) {
                            cell.setBackground(dragTargetBackGround);
                        }
                    }
                });

                cell.setOnDragExited((e) -> {
                    cell.setBackground(normalBackGround);
                });

                cell.setOnDragDropped((e) -> {
                    GridMonitor content = (GridMonitor) e.getDragboard().getContent(listDataFormat);
                    if (content != null) {
                        cell.setBackground(normalBackGround);

                        TableDrag d = (TableDrag) e.getDragboard().getContent(tableDataFormat);
                        if (d != null) {
                            this.tableRows.get(d.getRow()).set(d.getColumn(), null);
                        }

                        TableRow row = cell.getTableRow();
                        this.tableRows.get(row.getIndex()).set(columnIndex, content);

                        this.connectedClients.remove(content);
                    }
                });

                return cell;
            });
            monitorsTable.getColumns().add(col1);
        }

        availableMonitorsListBox.setOnDragOver((e) -> {

            TableDrag content = (TableDrag) e.getDragboard().getContent(tableDataFormat);
            if (content != null) {
                e.acceptTransferModes(TransferMode.ANY);
            }

        });

        availableMonitorsListBox.setOnDragDropped((e) -> {

            TableDrag d = (TableDrag) e.getDragboard().getContent(tableDataFormat);

            if (d != null) {
                this.tableRows.get(d.getRow()).set(d.getColumn(), null);
            }

            GridMonitor content = (GridMonitor) e.getDragboard().getContent(listDataFormat);
            if (content != null) {
                connectedClients.add(content);
            }
        });

        availableMonitorsListBox.setCellFactory((listView) -> {
            ListCell<GridMonitor> c = new ListCell<GridMonitor>() {
                @Override
                protected void updateItem(GridMonitor item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        this.setText(item.getHostname());
                    } else {
                        this.setText(null);
                    }
                }

            };

            c.setOnDragDetected((e) -> {
                if (c.getItem() == null) {
                    return;
                }

                Dragboard db = availableMonitorsListBox.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.put(listDataFormat, c.getItem());
                db.setContent(content);
                e.consume();
            });

            return c;
        });

    }

    private void setCursors() {
        this.availableMonitorsListBox.setCursor(new ImageCursor(new Image(getClass().getResourceAsStream("/icons/monitor-icon.png"))));
    }

    private void unsetCursors() {
        this.availableMonitorsListBox.setCursor(Cursor.DEFAULT);
    }

    public void setServer(MioServer server) {
        this.server = server;
        
        this.mover = new MouseMover(server);
        
        server.onConnection((c) -> {

            c.onMessage(MonitorInfo.class, (mi) -> {
                
                GridMonitor gridMonitor = new GridMonitor(c.getHostName(), new Dimension(mi.getWidth(), mi.getHeight()), false);

                Platform.runLater(()-> connectedClients.add(gridMonitor));
     
                c.onDisconnect(() -> {
                    connectedClients.remove(gridMonitor);
                    for (GridRow r : tableRows) {
                        for (int i = 0; i < TABLESIZE; i++) {
                            GridMonitor get = r.get(i).get();
                            if (get != null && get.equals(gridMonitor)) {
                                r.set(i, null);
                                return;
                            }
                        }
                    }
                });

            });

        });
    }

}

class GridRow {

    private final ObservableList<SimpleObjectProperty<GridMonitor>> monitorsInRow = FXCollections.observableArrayList();

    {
        for (int i = 0; i < TABLESIZE; i++) {
            monitorsInRow.add(new SimpleObjectProperty<>());
        }
    }

    void set(int i, GridMonitor m) {
        monitorsInRow.get(i).set(m);
    }

    ObservableObjectValue<GridMonitor> get(int i) {
        return monitorsInRow.get(i);
    }
}

class TableDrag implements Serializable {

    private int row;
    private int column;

    public TableDrag(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

}

class GridMonitor implements Serializable {

    private final String hostname;
    private final Dimension size;
    private final boolean isHost;

    GridMonitor(String hostname, Dimension dims, boolean isHost) {
        this.hostname = hostname;
        this.size = dims;
        this.isHost = isHost;
    }

    public Dimension getSize() {
        return size;
    }

    public String getHostname() {
        return hostname;
    }

    @Override
    public String toString() {
        return this.hostname;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.hostname);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GridMonitor other = (GridMonitor) obj;
        if (!Objects.equals(this.hostname, other.hostname)) {
            return false;
        }
        return true;
    }

    boolean isHost() {
        return this.isHost;
    }

}
