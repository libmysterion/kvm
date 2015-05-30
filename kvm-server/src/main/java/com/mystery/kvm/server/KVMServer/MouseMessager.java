package com.mystery.kvm.server.KVMServer;

import com.mystery.kvm.common.messages.ControlTransition;
import com.mystery.kvm.common.messages.KeyPress;
import com.mystery.kvm.common.messages.KeyRelease;
import com.mystery.kvm.common.messages.MonitorInfo;
import com.mystery.kvm.common.messages.MousePress;
import com.mystery.kvm.common.messages.MouseMove;
import com.mystery.kvm.common.messages.MouseRelease;
import com.mystery.kvm.common.messages.MouseWheel;
import com.mystery.kvm.setup.connections.ConnectionsService;
import com.mystery.libmystery.injection.Inject;
import com.mystery.libmystery.injection.Singleton;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import java.io.Serializable;
import java.util.Map.Entry;

@Singleton
public class MouseMessager {

    @Inject
    private ConnectionsService connectionsService;

    private AsynchronousObjectSocketChannel getClient(String hostname) {

        for (Entry<AsynchronousObjectSocketChannel, MonitorInfo> entry : this.connectionsService.getClientMap().entrySet()) {
            AsynchronousObjectSocketChannel client = entry.getKey();
            MonitorInfo monitorInfo = entry.getValue();

            if (monitorInfo.getHostName().equals(hostname)) {
                return client;
            }
        }

        return null;
    }

    private void send(String hostname, Serializable message) {
        AsynchronousObjectSocketChannel client = getClient(hostname);
        if (client != null) {
            client.send(message);
        }
    }

    public void move(String hostname, int x, int y) {
        send(hostname, new MouseMove(x, y));
    }

    public void mousePress(String hostname, int button) {
        send(hostname, new MousePress(button));
    }

    public void mouseRelease(String hostname, int button) {
        send(hostname, new MouseRelease(button));
    }

    void keyPress(String hostname, int k) {
        send(hostname, new KeyPress(k));
    }

    void keyRelease(String hostname, int k) {
        send(hostname, new KeyRelease(k));
    }

    void activate(String hostname) {
        send(hostname, new ControlTransition(true));
    }

    void deactivate(String hostname) {
        send(hostname, new ControlTransition(false));
    }

    void mouseWheel(String hostname, int notches) {
        send(hostname, new MouseWheel(notches));
    }

}
