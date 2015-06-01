package com.mystery.kvm.client;

import com.mystery.libmystery.nio.Callback;
import com.mystery.libmystery.nio.NioClient;
import java.io.IOException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Tray tray;
    private AutoJoin autojoin;   
    
    private Callback<NioClient> attachClient = (nioClient) -> new KVMClient(nioClient, tray);
    

    private void startAutoJoin() {
        if (autojoin == null) {
            autojoin = new AutoJoin(attachClient);
            tray.setAutoJoin(autojoin);
        }

        try {
            autojoin.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {

     
        ClientConfig config = new ClientConfig();   // auto loads if existing
        
        tray = new Tray(config);
        
        tray.addAppToTray();

        startAutoJoin();
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
        launch(args);
    }

}
