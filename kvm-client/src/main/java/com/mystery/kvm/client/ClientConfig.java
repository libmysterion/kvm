
package com.mystery.kvm.client;

import com.mystery.libmystery.persistence.PersistantObject;

public class ClientConfig extends PersistantObject{

    private String autoConnectHostName;
    
    public ClientConfig() {
        super("./config");
    }

    public String getAutoConnectHostName() {
        return autoConnectHostName;
    }

    public void setAutoConnectHostName(String autoConnectHostName) {
        this.autoConnectHostName = autoConnectHostName;
    }
    
}
