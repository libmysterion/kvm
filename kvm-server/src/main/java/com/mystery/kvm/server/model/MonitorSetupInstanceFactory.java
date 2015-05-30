package com.mystery.kvm.server.model;

import com.mystery.kvm.server.KVMServer.KVMServer;
import com.mystery.libmystery.injection.Injector;
import com.mystery.libmystery.injection.InjectorFactory;
import java.util.function.Function;

public class MonitorSetupInstanceFactory implements Function<Class<MonitorSetup>, MonitorSetup> {

    @Override
    public MonitorSetup apply(Class<MonitorSetup> a) {
        Injector injector = InjectorFactory.getInstance();
        KVMServer kvmServerSingleton = injector.create(KVMServer.class);
        if (kvmServerSingleton.getConfiguration() != null) {
            return kvmServerSingleton.getConfiguration();
        }
        return new MonitorSetup();// auto-loads config from disk
    }
}
