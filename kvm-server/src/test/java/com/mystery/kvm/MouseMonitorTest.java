
package com.mystery.kvm;

import com.mystery.kvm.server.KVMServer.MouseMonitor;
import java.awt.Point;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class MouseMonitorTest {

    public MouseMonitorTest() {
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
     * Test of main method, of class MouseMonitor.
     */
    @Test
    public void testMain() throws Exception {
        MouseMonitor m = new MouseMonitor();
        m.onTick((p) -> {
            assertNotNull(p);
            assertEquals(Point.class, p.getClass());
            synchronized (m) {
                m.notify();
            }
        });
        m.start();
        synchronized (m) {
            m.wait();
        }
    }

}
