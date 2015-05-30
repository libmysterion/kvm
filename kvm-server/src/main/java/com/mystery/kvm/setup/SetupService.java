package com.mystery.kvm.setup;

import com.mystery.kvm.server.model.MonitorSetup;
import com.mystery.libmystery.injection.Inject;
import com.mystery.libmystery.injection.Singleton;

@Singleton
public class SetupService {

    @Inject
    private MonitorSetup monitorSetup;

    
}
