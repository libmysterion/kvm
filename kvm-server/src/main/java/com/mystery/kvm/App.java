package com.mystery.kvm;

import com.airhacks.afterburner.injection.Injector;
import com.mystery.kvm.setup.SetupView;
import com.mystery.libmystery.event.EventEmitter;
import com.mystery.libmystery.nio.MioServer;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class App extends Application {

    // host app will have an icon in bottom right always
    // it has a menu item for "configure" always
    // if its already showing setup screen then clicking it does nothing
    // so host starts up for first time and confi screen is showing
    // user sets up stuff and clicks on go
    // the monitor config is saved
    // the tray icon menu now shows an option for "configure" which showsthe setup screen
    // user can go to configure find a newly connected moitor add it and continue working
    // user quites the program
    // user opens for non-initial launch
    // the setup from previous session is picked up and loaded
    // as additional clients join they slot into their alloted position in the setup
    
    // ----------todo---when a click joins a bubble message will come from the tray to inform host user
    // ----------todo---if the client joining is not configured in the setup then a diferent message should show informing the host user
    // ----------todo---there will be ability to right click on a connection in the connections list to get a menu
    // ----------todo---menu option will be "remove" which will disconnect the client
    // 
    // stability fixes
    // prevent the stage window from showing in the taskbar
    // be able to send alt+tab
    // make the ui pretty -- fx-bootstrap?
    // close hook to remove listeners from server from presenters
    // close hook to save the monitor setup config when window closed(and apply it to the server) 
    // ability to configure alias for monitor
     // menu item - alias
     // little input dialog thing to get the single field
    // client to send hostname with the monitor info (since only host shows monitor name right, if machines swap ip's then alias would get messed up)
    
    
    // client to hide mouse when not active
    // client to use a trayicon
    // exit menu item
    // client to perform an eternal portscan....scan should continue on disconnect, and should loop if no server found
    
    // server threads bug...the app never dies on its own
    // client might also exhibit
    // manage thread pools from app not automanaged
    // make server use a configured channel pool thing so i can use a cached thread pool with it
    // 
    // stability fixes
    //
    // exe wrapper to include a private jre in the dist
    // ---admin mode launcher----
    // installer should have option to start client on machine startup (we need that since same installer used for host)
    // if the user declined then it makes sense to have that function available somewhere(not sure how to do that since don know what it is yet)
    // so i wont be able to do from java;thats for sure
    // since it should be an exe on its own(or .bat or whatever) maybe the java can run it with Runtime.getRuntime.exec
    // failing that (maybe) a start menu item for[Run client on startup]
    // would need to come with corresponding [remove client run on startup]
 
    // the goal is to have the UAC popop fire once on installation
    // then on machine startup i want to start the client in admin mode without the UAC popup
        // this should create a task like in these articles...
    // see also - https://www.raymond.cc/blog/task-scheduler-bypass-uac-prompt/
    // see also 2 - http://www.howtogeek.com/howto/windows-vista/create-administrator-mode-shortcuts-without-uac-prompts-in-windows-vista/
    
    
// make a release branch
    
    // version 2
    // client-side mouse "smoothing" whenever you get told to mouseTo somewhere do it in stages to prevent cursor jump
    // adding ability to have dual monitor setup
    // this should allow each monitor to be treated independently still
    // so i could setup like | dual-1 | guest | dual-2 |
    // and i would need to know when the user is going between monitors and have all the screen sizes
    // but should be sort of the same as what we got 
    
    
    
    private MioServer server;

    private Stage primaryStage;
    private EventEmitter emitter;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Platform.setImplicitExit(false);

        this.primaryStage = primaryStage;

        Properties.initApplicationProperties();

        server = createServer();
        
        Injector.setModelOrService(MioServer.class, server);
        Injector.setModelOrService(Stage.class, primaryStage);

        emitter = new EventEmitter();
        Injector.setModelOrService(EventEmitter.class, emitter);
        
        SwingUtilities.invokeLater(this::addAppToTray);

        primaryStage.setOnHidden((c)-> {
            System.out.println("onHidden");
            emitter.emit("stage.hide", null);
        });
        
        showSetupView();

        startApplicationServer();
    }

    private void showSetupView() {
        
        if (!primaryStage.isShowing()) {   // if i add any more screens i guess i just need to add a check for that
            SetupView setupView = new SetupView();
            Scene scene = new Scene(setupView.getView());
            //stage.setTitle("followme.fx");
            final String uri = getClass().getResource("app.css").toExternalForm();
            scene.getStylesheets().add(uri);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

    }

    private MioServer createServer() {
        MioServer r = new MioServer();
        return r;
    }

    private void startApplicationServer() {
        try {
            System.out.println("starting...");
            server.listen(7777);
            System.out.println("started");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        Injector.forgetAll();
    }

    private void trayOnAction(java.awt.event.ActionEvent e) {
        Platform.runLater(() -> {
            showSetupView();
        });
    }

    // this how to display a baloon mesage
//     javax.swing.SwingUtilities.invokeLater(() ->
//                                trayIcon.displayMessage(
//                                        "hello",
//                                        "The time is now " + timeFormat.format(new Date()),
//                                        java.awt.TrayIcon.MessageType.INFO
//                                )
//                            );
    //    /**
//     * Sets up a system tray icon for the application.
//     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }

            // set up a system tray icon.
            SystemTray tray = SystemTray.getSystemTray();

            TrayIcon trayIcon = new TrayIcon(ImageIO.read(this.getClass().getResourceAsStream("/server/icons/monitor-icon-16x16.png")));

            // if the user double-clicks on the tray icon, show the setup screen
            trayIcon.addActionListener(this::trayOnAction);
            java.awt.MenuItem openItem = new java.awt.MenuItem("Configure");
            openItem.addActionListener(this::trayOnAction);

            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
                Platform.exit();
                tray.remove(trayIcon);
                try {
                    server.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                
            });

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // add the application tray icon to the system tray.
            tray.add(trayIcon);
            
            
        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }

//    @Override
//    public void start(Stage stage) throws Exception {
//        System.out.println("---LAUNCH SERVER---");
//    
//       // this.primaryStage = stage;
//       // this.primaryStage.setTitle("KVM server");
//        
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/fxml/Scene.fxml"));
//        Parent root = loader.load();
//        MonitorSetupController ctrl = loader.getController();
//        
//        Scene scene = new Scene(root);
//       
//        scene.getStylesheets().add("/server/styles/Styles.css");
//        
//        Stage window = new Stage();
//        window.setScene(scene);
//        
//        
//        
//        int PORT = 9934;
//        
//        MioServer server = new MioServer();
//        ctrl.setServer(server);
//        ctrl.setStage(window);
//        
//        server.listen(PORT);
//        
//       
//        window.show();
//      
//        Platform.setImplicitExit(false);
//   //     stage.initStyle(StageStyle.TRANSPARENT);
////        stage.setX(0);
////        stage.setY(0);
////        stage.setWidth(0);
////        stage.setHeight(0);
//     //   stage.setOpacity(0.000d);
//
//       // stage.
//        
//        //stage.show();
//        
//    }
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch();
    }

}
//
//
//import javafx.application.*;
//import javafx.geometry.Pos;
//import javafx.scene.*;
//import javafx.scene.control.Label;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.stage.*;
// 
//import javax.imageio.ImageIO;
//import java.io.IOException;
//import java.net.URL;
//import java.text.*;
//import java.util.*;
// 
//// Java 8 code
//public class MainApp extends Application {
// 
//    // one icon location is shared between the application tray icon and task bar icon.
//    // you could also use multiple icons to allow for clean display of tray icons on hi-dpi devices.
//    private static final String iconImageLoc =
//            "http://icons.iconarchive.com/icons/scafer31000/bubble-circle-3/16/GameCenter-icon.png";
// 
//    // application stage is stored so that it can be shown and hidden based on system tray icon operations.
//    private Stage stage;
// 
//    // a timer allowing the tray icon to provide a periodic notification event.
//    private Timer notificationTimer = new Timer();
// 
//    // format used to display the current time in a tray icon notification.
//    private DateFormat timeFormat = SimpleDateFormat.getTimeInstance();
// 
//    // sets up the javafx application.
//    // a tray icon is setup for the icon, but the main stage remains invisible until the user
//    // interacts with the tray icon.
//    @Override public void start(final Stage stage) {
//        // stores a reference to the stage.
//        this.stage = stage;
// 
//        // instructs the javafx system not to exit implicitly when the last application window is shut.
//        Platform.setImplicitExit(false);
// 
//        // sets up the tray icon (using awt code run on the swing thread).
//        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);
// 
//        // out stage will be translucent, so give it a transparent style.
//        stage.initStyle(StageStyle.TRANSPARENT);
// 
//        // create the layout for the javafx stage.
//        StackPane layout = new StackPane(createContent());
//        layout.setStyle(
//                "-fx-background-color: rgba(255, 255, 255, 0.5);"
//        );
//        layout.setPrefSize(300, 200);
// 
//        // this dummy app just hides itself when the app screen is clicked.
//        // a real app might have some interactive UI and a separate icon which hides the app window.
//        layout.setOnMouseClicked(event -> stage.hide());
// 
//        // a scene with a transparent fill is necessary to implement the translucent app window.
//        Scene scene = new Scene(layout);
//        scene.setFill(Color.TRANSPARENT);
// 
//        stage.setScene(scene);
//    }
// 
//    /**
//     * For this dummy app, the (JavaFX scenegraph) content, just says "hello, world".
//     * A real app, might load an FXML or something like that.
//     *
//     * @return the main window application content.
//     */
//    private Node createContent() {
//        Label hello = new Label("hello, world");
//        hello.setStyle("-fx-font-size: 40px; -fx-text-fill: forestgreen;");
//        Label instructions = new Label("(click to hide)");
//        instructions.setStyle("-fx-font-size: 12px; -fx-text-fill: orange;");
// 
//        VBox content = new VBox(10, hello, instructions);
//        content.setAlignment(Pos.CENTER);
// 
//        return content;
//    }
// 

// 
//    /**
//     * Shows the application stage and ensures that it is brought ot the front of all stages.
//     */
//    private void showStage() {
//        if (stage != null) {
//            stage.show();
//            stage.toFront();
//        }
//    }
// 
//    public static void main(String[] args) throws IOException, java.awt.AWTException {
//        // Just launches the JavaFX application.
//        // Due to way the application is coded, the application will remain running
//        // until the user selects the Exit menu option from the tray icon.
//        launch(args);
//    }
//}
