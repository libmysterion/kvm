
package com.mystery.kvm.server.KVMServer;

import java.awt.Dimension;
import java.awt.Point;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class DimensionScaleTest {
    
    public DimensionScaleTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of scalePoint method, of class DimensionScale.
     */
    @Test
    public void testScalePoint() {

        Dimension from = new Dimension(100, 100);
        Dimension to = new Dimension(50, 50);
        Point centre = new Point(50, 50);
        
        DimensionScale scale = new DimensionScale(from, to);
       
        Point scaled = scale.scalePoint(centre);
        
        assertEquals(25, scaled.x);
        assertEquals(25, scaled.y);
      
    }
    
    @Test
    public void testScalePointSame() {

        Dimension from = new Dimension(100, 100);
        Dimension to = new Dimension(100, 100);
        Point centre = new Point(50, 50);
        
        DimensionScale scale = new DimensionScale(from, to);
       
        Point scaled = scale.scalePoint(centre);
        
        assertEquals(50, scaled.x);
        assertEquals(50, scaled.y);
      
    }

    
    @Test
    public void testScalePointYOnly() {

        Dimension from = new Dimension(100, 100);
        Dimension to = new Dimension(100, 80);
        Point centre = new Point(50, 50);
        
        DimensionScale scale = new DimensionScale(from, to);
       
        Point scaled = scale.scalePoint(centre);
        
        assertEquals(50, scaled.x);
        assertEquals(40, scaled.y);
      
    }
    
   
}
