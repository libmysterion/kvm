package com.mystery.kvm.server.KVMServer;

import com.mystery.kvm.common.messages.KeyPress;
import com.mystery.kvm.common.messages.KeyRelease;
import com.mystery.kvm.common.messages.MousePress;
import com.mystery.kvm.common.messages.MouseMove;
import com.mystery.kvm.common.messages.MouseRelease;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.MioServer;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class MouseMessager {

    @Inject
    private MioServer server;

    private final ArrayList<AsynchronousObjectSocketChannel> clients = new ArrayList<>();

    @PostConstruct
    public void init() {
        server.onConnection(this::onConnection);
    }

    private void onConnection(AsynchronousObjectSocketChannel client) {
        synchronized (clients) {
            this.clients.add(client);
        }

    }

    public void move(String hostname, int x, int y) {
        synchronized (clients) {
            clients.stream()
                    .filter((c) -> c.getHostName().equals(hostname))
                    .forEach((c) -> {
                        c.send(new MouseMove(x, y));
                    });
        }
    }

    public void mousePress(String hostname, int button) {
        synchronized (clients) {
            clients.stream()
                    .filter((c) -> c.getHostName().equals(hostname))
                    .forEach((c) -> {
                        c.send(new MousePress(button));
                    });
        }
    }
    
    
    public void mouseRelease(String hostname, int button) {
        synchronized (clients) {
            clients.stream()
                    .filter((c) -> c.getHostName().equals(hostname))
                    .forEach((c) -> {
                        c.send(new MouseRelease(button));
                    });
        }
    }

    void keyPress(String hostname, int k) {
        synchronized (clients) {
            clients.stream()
                    .filter((c) -> c.getHostName().equals(hostname))
                    .forEach((c) -> {
                        c.send(new KeyPress(k));
                    });
        }
    }

    
    void keyRelease(String hostname, int k) {
          synchronized (clients) {
            clients.stream()
                    .filter((c) -> c.getHostName().equals(hostname))
                    .forEach((c) -> {
                        c.send(new KeyRelease(k));
                    });
        }
    }

    
    

}
