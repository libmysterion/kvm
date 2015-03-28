
package com.mystery.kvm.server.KVMServer;

import java.awt.Dimension;
import java.awt.Point;


class DimensionScale {

    private final double x;
    private final double y;
    
    DimensionScale(Dimension from, Dimension to) {
        x = (double)to.width / from.width;
        y = (double)to.height/ from.height;
    }

    Point scalePoint(Point p) {
        return new Point((int)Math.floor(p.x * x), (int)Math.floor(p.y * y));
    }
    
    
    
}
