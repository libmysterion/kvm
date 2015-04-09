package com.mystery.kvm.server.model;

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MonitorSetupTest {
    
    public MonitorSetupTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        MonitorSetup.path = "./testMonitorfile";
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
    
    @Test
    public void testLoadingFromDisk() throws IOException {
        
          MonitorSetup s = new MonitorSetup();
          int x = 4;
          int y = 5;
          boolean connected = true;
          boolean active = true;
          boolean isHost = true;
          
          s.addMonitor(new Monitor("hello", null,active, isHost,x, y, connected));
          
          s.save();
          
          
          MonitorSetup t = new MonitorSetup();
          assertNotNull(t.getMonitor("hello"));
          
          assertEquals(t.getMonitor("hello").getGridX(), x);
          assertEquals(t.getMonitor("hello").getGridY(), y);
          
        
    }

//    /**
//     * Test of connectClient method, of class MonitorSetup.
//     */
//    @Test
//    public void testConnectClient() {
//        System.out.println("connectClient");
//        String hostname = "";
//        MonitorSetup instance = new MonitorSetup();
//        instance.connectClient(hostname);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of disconnectClient method, of class MonitorSetup.
//     */
//    @Test
//    public void testDisconnectClient() {
//        System.out.println("disconnectClient");
//        String hostname = "";
//        MonitorSetup instance = new MonitorSetup();
//        instance.disconnectClient(hostname);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findActive method, of class MonitorSetup.
//     */
//    @Test
//    public void testFindActive() {
//        System.out.println("findActive");
//        MonitorSetup instance = new MonitorSetup();
//        Monitor expResult = null;
//        Monitor result = instance.findActive();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findHost method, of class MonitorSetup.
//     */
//    @Test
//    public void testFindHost() {
//        System.out.println("findHost");
//        MonitorSetup instance = new MonitorSetup();
//        Monitor expResult = null;
//        Monitor result = instance.findHost();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addMonitor method, of class MonitorSetup.
//     */
//    @Test
//    public void testAddMonitor() {
//        System.out.println("addMonitor");
//        Monitor monitor = null;
//        MonitorSetup instance = new MonitorSetup();
//        instance.addMonitor(monitor);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isTileOccupied method, of class MonitorSetup.
//     */
//    @Test
//    public void testIsTileOccupied() {
//        System.out.println("isTileOccupied");
//        int tile_x = 0;
//        int tile_y = 0;
//        MonitorSetup instance = new MonitorSetup();
//        boolean expResult = false;
//        boolean result = instance.isTileOccupied(tile_x, tile_y);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getMonitor method, of class MonitorSetup.
//     */
//    @Test
//    public void testGetMonitor_int_int() {
//        System.out.println("getMonitor");
//        int tile_x = 0;
//        int tile_y = 0;
//        MonitorSetup instance = new MonitorSetup();
//        Monitor expResult = null;
//        Monitor result = instance.getMonitor(tile_x, tile_y);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findFromCurrent method, of class MonitorSetup.
//     */
//    @Test
//    public void testFindFromCurrent() {
//        System.out.println("findFromCurrent");
//        int offsetX = 0;
//        int offsetY = 0;
//        MonitorSetup instance = new MonitorSetup();
//        Monitor expResult = null;
//        Monitor result = instance.findFromCurrent(offsetX, offsetY);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setSize method, of class MonitorSetup.
//     */
//    @Test
//    public void testSetSize() {
//        System.out.println("setSize");
//        String hostName = "";
//        MonitorInfo mi = null;
//        MonitorSetup instance = new MonitorSetup();
//        instance.setSize(hostName, mi);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hasHost method, of class MonitorSetup.
//     */
//    @Test
//    public void testHasHost() {
//        System.out.println("hasHost");
//        String hostname = "";
//        MonitorSetup instance = new MonitorSetup();
//        boolean expResult = false;
//        boolean result = instance.hasHost(hostname);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSize method, of class MonitorSetup.
//     */
//    @Test
//    public void testGetSize() {
//        System.out.println("getSize");
//        String hostname = "";
//        MonitorSetup instance = new MonitorSetup();
//        Dimension expResult = null;
//        Dimension result = instance.getSize(hostname);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of remove method, of class MonitorSetup.
//     */
//    @Test
//    public void testRemove() {
//        System.out.println("remove");
//        String hostname = "";
//        MonitorSetup instance = new MonitorSetup();
//        instance.remove(hostname);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getMonitor method, of class MonitorSetup.
//     */
//    @Test
//    public void testGetMonitor_String() {
//        System.out.println("getMonitor");
//        String hostname = "";
//        MonitorSetup instance = new MonitorSetup();
//        Monitor expResult = null;
//        Monitor result = instance.getMonitor(hostname);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    
}
