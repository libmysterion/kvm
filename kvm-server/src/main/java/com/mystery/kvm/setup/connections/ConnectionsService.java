package com.mystery.kvm.setup.connections;

import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.setup.monitors.GridMonitor;
import com.mystery.kvm.tray.TrayMessage;
import com.mystery.kvm.tray.TrayService;
import com.mystery.libmystery.event.EventEmitter;
import com.mystery.libmystery.injection.Inject;
import com.mystery.libmystery.injection.PostConstruct;
import com.mystery.libmystery.injection.Singleton;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.MioServer;
import java.awt.TrayIcon;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import jdk.nashorn.internal.codegen.Emitter;

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
    private Map<AsynchronousObjectSocketChannel, MonitorInfo> clientMap = new HashMap<>();
   
    @PostConstruct
    private void initialise() {
        mioServer.onConnection((client) -> {

            client.onMessage(MonitorInfo.class, (monitorInfo) -> {
                clientMap.put(client, monitorInfo);
                trayService.showMessage(new TrayMessage(newMonitorBalloonHeader, monitorInfo.getHostName() + newMonitorBalloonText, TrayIcon.MessageType.INFO));
                this.eventEmitter.emit("client.connect", client, monitorInfo);
            });

            client.onDisconnect((c) -> {
                this.eventEmitter.emit("client.disconnect", client, clientMap.get(client));
                clientMap.remove(client);
            });

        });
    }

    public Map<AsynchronousObjectSocketChannel, MonitorInfo> getClientMap() {
        return clientMap;
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
    public void disconnectClient(String hostname) throws Exception {
        for(Map.Entry<AsynchronousObjectSocketChannel, MonitorInfo> entry : clientMap.entrySet()){
            AsynchronousObjectSocketChannel key = entry.getKey();
            MonitorInfo value = entry.getValue();
            if(value.getHostName().equals(hostname)){
                mioServer.disconnectClient(key);
                return;
            }
        }
    }
}
