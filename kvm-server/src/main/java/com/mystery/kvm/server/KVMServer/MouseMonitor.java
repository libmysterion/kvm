package com.mystery.kvm.server.KVMServer;

import com.mystery.libmystery.nio.Callback;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class MouseMonitor {
    
    private ExecutorService exc = Executors.newSingleThreadExecutor((Runnable r) -> {
        Thread t = new Thread(()->{
            try {
                r.run();
            }catch(Exception e){
                e.printStackTrace();
            }
        });
       // t.setDaemon(true);
        return t;
    });
    
    private List<Callback<Point>> callbacks = new ArrayList<>();

    public MouseMonitor() {
        
    }
        
    
    public void onTick(Callback<Point> cb){
        synchronized(callbacks){
            callbacks.add(cb);
        }
    }
    
    private void setMouseLocation (Point p) {
        synchronized(callbacks){
            callbacks.forEach((cb) -> cb.onSuccess(p));
        }
    }
    
    private void updateMouseLocation () {
        PointerInfo inf = MouseInfo.getPointerInfo();
        Point p = inf.getLocation();
        setMouseLocation(p);
    }
     
    private boolean stop = false;
    
    private Runnable mon = ()-> {
        updateMouseLocation();
        if (!stop) {
            exc.submit(this.mon);
        }
    };
    
    public void start() {
        exc.submit(mon);
    }
    
    public void stop(){
        this.stop = true;
    }
    
}