
package com.mystery.kvm.client;

import com.mystery.libmystery.nio.Callback;
import com.mystery.libmystery.nio.autojoin.TcpPortScanningAutoJoinClient;
import java.net.InetSocketAddress;


public class AutoJoin extends TcpPortScanningAutoJoinClient {

    public AutoJoin(Callback<InetSocketAddress> callback) {
        super(9934, callback);
    }


    
}
