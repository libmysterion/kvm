
package com.mystery.kvm.client;

import com.mystery.libmystery.nio.Callback;
import com.mystery.libmystery.nio.NioClient;
import com.mystery.libmystery.nio.autojoin.TcpPortScanningAutoJoinClient;


public class AutoJoin extends TcpPortScanningAutoJoinClient {

    public AutoJoin(Callback<NioClient> callback) {
        super(9934, callback);
    }

    

}
