package com.mystery.kvm;

import com.mystery.libmystery.nio.MioServer;
import com.mystery.libmystery.nio.autojoin.AutoJoinerServer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("---LAUNCH SERVER---");
    
        this.primaryStage = stage;
        this.primaryStage.setTitle("KVM server");
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/fxml/Scene.fxml"));
        Parent root = loader.load();
        MonitorSetupController ctrl = loader.getController();
        
        Scene scene = new Scene(root);
       
        scene.getStylesheets().add("/server/styles/Styles.css");
        
        stage.setScene(scene);
        
        
        
        int PORT = 9934;
        
        MioServer server = new MioServer();
        AutoJoinerServer auto = new AutoJoinerServer("synergy", PORT);
        ctrl.setServer(server);
        ctrl.setStage(stage);
        
        server.listen(PORT);
        
        
        stage.show();
        auto.start();
        
    }

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
