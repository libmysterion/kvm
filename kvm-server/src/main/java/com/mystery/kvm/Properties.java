package com.mystery.kvm;

import com.airhacks.afterburner.injection.Injector;
import java.util.HashMap;
import java.util.Map;


public class Properties {
    
    public static void initApplicationProperties(){
        
        Map<Object, Object> customProperties = new HashMap<>();
     
        customProperties.put("MONITOR_SETUP_GRID_SIZE", 5);
        
        
        customProperties.put("newMonitorBalloonHeader", "New Monitor Available");
        customProperties.put("newMonitorBalloonText", "somebody joined");
        
        
        customProperties.put("monitorReconnectBalloonHeader", "Monitor Reconnected");
        customProperties.put("monitorReconnectBalloonText", "somebody joined in the config");
        
     
        Injector.setConfigurationSource(customProperties::get);
        
        
    }
}
