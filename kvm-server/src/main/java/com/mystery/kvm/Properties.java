package com.mystery.kvm;


import com.mystery.libmystery.injection.Injector;
import com.mystery.libmystery.injection.InjectorFactory;
import java.util.HashMap;
import java.util.Map;


public class Properties {
    
    public static void initApplicationProperties(){
        
        Injector injector = InjectorFactory.getInstance();
        // todo put all this in a properties file somewhere
        Map<String, Object> customProperties = new HashMap<>();
     
        customProperties.put("MONITOR_SETUP_GRID_SIZE", 5);
        
        
        customProperties.put("newMonitorBalloonHeader", "New Monitor Available");
        customProperties.put("newMonitorBalloonText", " is now available to configure.");
        
        
        customProperties.put("monitorReconnectBalloonHeader", "Monitor Reconnected");
        customProperties.put("monitorReconnectBalloonText", " is now available.");
        
     
        injector.setPropertySource(customProperties::get);
        
        
    }
}
