package com.mystery.kvm.setup.connections;

import com.mystery.kvm.common.messages.MonitorInfo;
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

}
