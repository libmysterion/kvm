package com.mystery.kvm.setup.connections;

import com.mystery.kvm.common.messages.ConnectClient;
import com.mystery.kvm.common.messages.DisconnectClient;
import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.tray.TrayService;
import com.mystery.libmystery.event.EventEmitter;
import com.mystery.libmystery.injection.Inject;
import com.mystery.libmystery.injection.PostConstruct;
import com.mystery.libmystery.injection.Singleton;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.MioServer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ConnectionsService {

    @Inject
    private EventEmitter eventEmitter;

    @Inject
    private TrayService trayService;

    @Inject
    private String newMonitorBalloonText;

    @Inject
    private String newMonitorBalloonHeader;

    @Inject
    private MioServer mioServer;

    // todo - this is not thread safe
    private Map<AsynchronousObjectSocketChannel, ConnectedMonitor> clientMap = new HashMap<>();

    @PostConstruct
    private void initialise() {
        mioServer.onConnection((client) -> {

            try {
                client.send(new MonitorInfo(0, 0, InetAddress.getLocalHost().getHostName()));
            } catch (UnknownHostException ex) {
                //if i connected to something i dont think this can really happpen....
                ex.printStackTrace();
            }
            client.onMessage(MonitorInfo.class, (monitorInfo) -> {
                clientMap.put(client, new ConnectedMonitor(false, monitorInfo));
            });

            client.onMessage(ConnectClient.class, (msg) -> {
                clientMap.get(client).setConnected(true);
                this.eventEmitter.emit("client.connect", client, clientMap.get(client).getMonitorInfo());
                // todo put the popups back
                //trayService.showMessage(new TrayMessage(newMonitorBalloonHeader, monitorInfo.getHostName() + newMonitorBalloonText, TrayIcon.MessageType.INFO));
            });

            client.onMessage(DisconnectClient.class, (msg) -> {
                clientMap.get(client).setConnected(false);
                this.eventEmitter.emit("client.disconnect", client, clientMap.get(client).getMonitorInfo());
                //trayService.showMessage(new TrayMessage(newMonitorBalloonHeader, monitorInfo.getHostName() + newMonitorBalloonText, TrayIcon.MessageType.INFO));
            });

            client.onDisconnect((c) -> {
                this.eventEmitter.emit("client.disconnect", client, clientMap.get(client).getMonitorInfo());
                clientMap.remove(client);
                // todo dc tray mesages
                //trayService.showMessage(new TrayMessage(newMonitorBalloonHeader, monitorInfo.getHostName() + newMonitorBalloonText, TrayIcon.MessageType.INFO));
            });

        });
    }

    public Map<AsynchronousObjectSocketChannel, MonitorInfo> getClientMap() {
        Map<AsynchronousObjectSocketChannel, MonitorInfo> map = new HashMap<>();
        clientMap.forEach((k, v) -> map.put(k, v.getMonitorInfo()));
        return map;
    }

    // todo - thee client will simplt reconnect
    // so we need to send a mesage to the client saying i am disconnecting you
    // and the client needs to keep in memory that he should not reconnect to the server
    // there should then be a menu option listing the available servers and you can connect manually
    // sound like a feature
    // so auto connect becomes a feature
    // the client has a menu item with a checkbox "autoconnect"
    // if that is checked then
    //...ok step back
    // we will need to make sure that the client can connect to many servers
    // and server will ned to understand that a client may be connected but inactive
    // that allows us to show the list of servers we are connected to
    // then a disconnect wil not actually disconnect from the server
    // but it deactivates the client - he is still connected but does not show in the connections list
    // the client can only now manually reconnect by clicking the menu item for the server address
    // so if autoconnect is checked...
    // lets not have an autoconnect as an top level option 
    // it needs to be an sub-option on a server
    // so we have the servers listed
    // // and i can hover over one and i get the autoconnect option in sub-menu
    // then i can check that and it is persisted
    // if that is checked then the app will send some message saying i want to be activated once a connection is made with that server
    // i could maybe just avoid sending the monitor info
    // that could be the trigger to show him on teh srrver
    // nooo...do not do that lets have a new message type to make it explicit!
    public void disconnectClient(String hostname) throws Exception {
        for (Map.Entry<AsynchronousObjectSocketChannel, ConnectedMonitor> entry : clientMap.entrySet()) {
            AsynchronousObjectSocketChannel client = entry.getKey();
            MonitorInfo value = entry.getValue().getMonitorInfo();
            if (value.getHostName().equals(hostname)) {
                client.send(new DisconnectClient());
                this.eventEmitter.emit("client.disconnect", client, clientMap.get(client).getMonitorInfo());
                return;
            }
        }
    }
}
