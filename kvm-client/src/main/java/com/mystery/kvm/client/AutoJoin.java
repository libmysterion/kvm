
package com.mystery.kvm.client;

import com.mystery.libmystery.nio.Callback;
import com.mystery.libmystery.nio.autojoin.AutoJoinClient;
import java.net.InetSocketAddress;


public class AutoJoin extends AutoJoinClient {

    public AutoJoin(Callback<InetSocketAddress> callback) {
        super("synergy", callback);
    }


    
}
