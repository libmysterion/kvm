package com.mystery.kvm;

import com.mystery.kvm.common.messages.MouseMove;
import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import com.mystery.libmystery.nio.MioServer;
import java.util.ArrayList;

public class MouseMover {

    private MioServer server;
    private ArrayList<AsynchronousObjectSocketChannel> clients = new ArrayList<>();

    public MouseMover(MioServer server) {
        this.server = server;

        server.onConnection((AsynchronousObjectSocketChannel client) -> {
            clients.add(client);
        });
    }

    public void move(String hostname, int x, int y) {
        clients.stream()
                .filter((c) -> c.getHostName().equals(hostname))
                .forEach((c) -> {
                       c.send(new MouseMove(x, y));
                });
    }

}
