package com.mystery.kvm.client;

import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.common.messages.MouseMove;
import com.mystery.libmystery.nio.NioClient;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        AutoJoin j = new AutoJoin((address) -> {
            NioClient nioClient = new NioClient();
            nioClient.connect(address)
                    .onSucess(() -> {
                        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
                        nioClient.send(new MonitorInfo(size.width, size.height));
                    }).onError((err) -> {
                        err.printStackTrace();
                    });
            
            nioClient.onMessage(MouseMove.class, (msg) -> {

                try {
                    Robot r = new Robot();
                    r.mouseMove(msg.getX(), msg.getY());
                } catch (AWTException ex) {
                    ex.printStackTrace();
                }
            });

        });

// you run the client and he gives you a url to connect to a server
        // wait surely the clients outght to be the ones running a server
        // then all i do is spin up a client
        // and on a "server" he actually just creates a bunch of clients
        // although that makes no freakin sense
        // what i real want is a thing for my use-case
        // it will be free
        // so
        // its going to use UDP multicast to check if theres anyone listening on the multicast
        // not sure if that would work in an enterprise
        // basically i want to be able to see on the host a list of available monitors(machinenames)
        // and then i drag it from a list into some kind of grid
        // that grid becomes my monitorSetup
        // so the client just now needs to be able to join the group when he starts up
        // the server sits there and broadcasts messages on the UDP port with the IP and socket to join on
        // that sounds very insecure...but safe on a home network
        // so when the client pick p a message he knows to connect the TCP client
        // the TCP client connects to the host
        // and we take it from there
        // since the server wont actually respond to anything but java messages
        // and in this case the server wont actually have any message handlers anywya
        // so even if some rouge did connect he couldt accomplish anything
//        new Thread(() -> {
//            try {
//                MulticastSocket socket = new MulticastSocket(4446);
//                InetAddress group = InetAddress.getByName("228.5.6.7");
//                socket.joinGroup(group);
//
//                byte[] buf = new byte[256];
//                DatagramPacket packet = new DatagramPacket(buf, buf.length);
//                socket.receive(packet);
//                String received = new String(packet.getData());
//
//                System.out.println(received);
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//
//        }).start();
//
//        new Thread(() -> {
//            try {
//
//                MulticastSocket socket = new MulticastSocket(4446);
//
//                for (int i = 0; i < 10; i++) {
//                    Thread.sleep(1000);
//                    byte[] buf = new byte[256];
//                    buf = "Hello world".getBytes();
//                    InetAddress group = InetAddress.getByName("228.5.6.7");
//                    DatagramPacket packet;
//                    packet = new DatagramPacket(buf, buf.length, group, 4446);
//
//                    socket.send(packet);
//                }
//
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//            }
//        }).start();

//        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));
//
//        Scene scene = new Scene(root);
//        scene.getStylesheets().add("/styles/Styles.css");
//
//        stage.setTitle("JavaFX and Maven");
//        stage.setScene(scene);
//        stage.show();
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
