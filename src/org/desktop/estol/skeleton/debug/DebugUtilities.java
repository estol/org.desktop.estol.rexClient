package org.desktop.estol.skeleton.debug;

//import org.desktop.estol.skeleton.commons.NotificationIcon;
import org.desktop.estol.skeleton.commons.NumericUtilities;
import org.desktop.estol.skeleton.commons.ThreadedUtility;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
//import java.awt.TrayIcon;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import org.desktop.estol.skeleton.system.exceptions.InternalErrorException;

/**
 * Class name says it all. 
 * 
 * @author estol
 */
public class DebugUtilities
{
    private static memory m  = null;
    private static threads t = null;
    private static DebugConsole d = null;
    private static Thread dConsoleThread = null;
    private static Thread locationWatchThread = null;
    private static LocationWatch lw = null;

    private static class memory implements Runnable, ThreadedUtility
    {
        Runtime rt;
        float totalmem, freemem, maxmem, usedmem;
        long interval = 1500;
        static float peakmem = 0;
        byte unit;
        boolean runFlag = true;
        String t, f, m, u, p;
        JLabel total, free, max, used, peak;
        
        final static int RELATION = 1024;
        final static String K = "KB", M = "MB", G = "GB";
        final static String TOTAL = "Total: ",
                FREE = "Free: ",
                MAX  = "Max: ",
                USED = "Used: ",
                PEAK = "Peak: ";
        
        
        
        memory(JLabel total, JLabel free, JLabel max, JLabel used, JLabel peak, byte unit) {
            rt = Runtime.getRuntime();
            runFlag = true;
            this.total = total;
            this.free  = free;
            this.max   = max;
            this.used  = used;
            this.unit  = unit;
            this.peak  = peak;
            display();
        }
        
        void getMemoryUsage() {
            totalmem = (float)rt.totalMemory();
            freemem  = (float)rt.freeMemory();
            maxmem   = (float)rt.maxMemory();
            usedmem  = (float)totalmem - freemem;
            peakmem  = (usedmem > peakmem) ? (float)usedmem : (float)peakmem;
            
            switch (unit) {
                case 0:
                    totalmem /= RELATION;
                    t = TOTAL + Float.toString(NumericUtilities.roundFloat(totalmem, 2)) + K;
                    freemem  /= RELATION;
                    f = FREE  + Float.toString(NumericUtilities.roundFloat(freemem, 2))  + K;
                    maxmem   /= RELATION;
                    m = MAX   + Float.toString(NumericUtilities.roundFloat(maxmem, 2))   + K;
                    usedmem  /= RELATION;
                    u = USED  + Float.toString(NumericUtilities.roundFloat(usedmem, 2))  + K;
                    peakmem  /= RELATION;
                    p = PEAK  + Float.toString(NumericUtilities.roundFloat(peakmem, 2))  + K;
                    break;
                case 1:
                    totalmem /= (RELATION * RELATION);
                    t = TOTAL + Float.toString(NumericUtilities.roundFloat(totalmem, 2)) + M;
                    freemem  /= (RELATION * RELATION);
                    f = FREE  + Float.toString(NumericUtilities.roundFloat(freemem, 2))  + M;
                    maxmem   /= (RELATION * RELATION);
                    m = MAX   + Float.toString(NumericUtilities.roundFloat(maxmem, 2))   + M;
                    usedmem  /= (RELATION * RELATION);
                    u = USED  + Float.toString(NumericUtilities.roundFloat(usedmem, 2))  + M;
                    peakmem  /= (RELATION * RELATION);
                    p = PEAK  + Float.toString(NumericUtilities.roundFloat(peakmem, 2))  + M;
                    break;
                case 2:
                    totalmem /= (RELATION * RELATION * RELATION);
                    t = TOTAL + Float.toString(NumericUtilities.roundFloat(totalmem, 2)) + G;
                    freemem  /= (RELATION * RELATION * RELATION);
                    f = FREE  + Float.toString(NumericUtilities.roundFloat(freemem, 2))  + G;
                    maxmem   /= (RELATION * RELATION * RELATION);
                    m = MAX   + Float.toString(NumericUtilities.roundFloat(maxmem, 2))   + G;
                    usedmem  /= (RELATION * RELATION * RELATION);
                    u = USED  + Float.toString(NumericUtilities.roundFloat(usedmem, 2))  + G;
                    peakmem  /= (RELATION * RELATION * RELATION);
                    p = PEAK  + Float.toString(NumericUtilities.roundFloat(peakmem, 2))  + G;
                    break;
                default:
                    totalmem /= (RELATION * RELATION);
                    t = TOTAL + Float.toString(NumericUtilities.roundFloat(totalmem, 2)) + M;
                    freemem  /= (RELATION * RELATION);
                    f = FREE  + Float.toString(NumericUtilities.roundFloat(freemem, 2))  + M;
                    maxmem   /= (RELATION * RELATION);
                    m = MAX   + Float.toString(NumericUtilities.roundFloat(maxmem, 2))   + M;
                    usedmem  /= (RELATION * RELATION);
                    u = USED  + Float.toString(NumericUtilities.roundFloat(usedmem, 2))  + M;
                    peakmem  /= (RELATION * RELATION);
                    p = PEAK  + Float.toString(NumericUtilities.roundFloat(peakmem, 2))  + M;
                    break;            }
        }

        void showMemoryUsage() {
            total.setText(t);
             free.setText(f);
              max.setText(m);
             used.setText(u);
             peak.setText(p);
        }

        @Override
        public final void display() {
            getMemoryUsage();
            showMemoryUsage();
        }

        void changeUnit(byte unit) {
            this.unit = unit;
        }

        void changeUpdateInterval(long interval) {
            this.interval = interval;
        }
        
        
        @Override
        public void shutdown() {
            runFlag = false;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Memory watch thread");
            while (runFlag) {
                try {
                    Thread.sleep(interval);
                    display();
                } catch (InterruptedException ex) {
                    DebugUtilities.addDebugMessage(ex.getMessage());
                    continue; // supressing the error... BURN THE HERETIC! KILL THE MUTANT! PURGE THE UNCLEAN!
                }
            }
        }
        
        @Override
        public boolean isRunning() {
            return runFlag;
        }
    }
    
    private static class threads implements Runnable, ThreadedUtility
    {
        ThreadMXBean ThreadBean = ManagementFactory.getThreadMXBean();
        int currentThreadCount, peakThreadCount;
        JLabel cThreadCount, pThreadCount;
        boolean runFlag = true;
        long interval = 1500;
        
        
        threads(JLabel cThreadCount, JLabel pThreadCount) {
            this.cThreadCount  = cThreadCount;
            this.pThreadCount  = pThreadCount;
            display();
            runFlag = true;
        }

        void getCounts() {
            currentThreadCount = ThreadBean.getThreadCount();
            peakThreadCount    = ThreadBean.getPeakThreadCount();
        }
        
        void showCounts() {
            cThreadCount.setText("Current: " + currentThreadCount);
            pThreadCount.setText("Peak:    " + peakThreadCount);
        }

        @Override
        public final void display() {
            getCounts();
            showCounts();
        }

        @Override
        public void shutdown() {
            runFlag = false;
        }
        
        @Override
        public boolean isRunning() {
            return runFlag;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Thread monitor thread");
            while (runFlag) {
                try {
                    Thread.sleep(interval);
                    display();
                } catch (InterruptedException ex) {
                    DebugUtilities.addDebugMessage(ex.getMessage());
                    continue;
                }
            }
        }
    }
    
    private static class DebugConsole implements Runnable, ThreadedUtility
    {
        boolean runFlag = true;
        static boolean Running = false;
        JTextPane pane = null;
        long interval = 125;
        static volatile HashMap<String, String> messages = new HashMap();
        static final DateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");

        DebugConsole() {
            addMessage("Debug console started.");
        }

        boolean isPaneSet() {
            return (pane == null) ? false : true;
        }

        @Override
        public boolean isRunning() {
            return Running;
        }

        void setPane(JTextPane p) {
            pane = p;
            display();
        }
        
        public void unSetPane() {
            pane = null;
        }

        DebugConsole(JTextPane p) {
            pane = p;
            addMessage("Debug console started.");
            display();
        }

        final synchronized void addMessage(String msg) {
            messages.put(df.format(new Date()), msg);
        }

        @Override
        public final synchronized void display() {
            Iterator iterator = messages.entrySet().iterator();
            boolean color = false;
            while (iterator.hasNext()) {
                Map.Entry pairs = (Map.Entry)iterator.next();
                StringBuilder sb = new StringBuilder();
                if (!"".equals(pane.getText())) {
                    sb.append(pane.getText());
                }
                sb.append(pairs.getKey());
                sb.append(" - ");
                sb.append(pairs.getValue());
                sb.append("\n");
                pane.setText(sb.toString());
                iterator.remove();
                color = !color;
            }
        }

        @Override
        public void shutdown() {
            runFlag = false;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("DebugConsole thread");
            Running = true;
            while (runFlag) {
                if (isPaneSet()) {
                    try {
                        Thread.sleep(interval);
                        display();
                    } catch (InterruptedException ex) {
                        addMessage(ex.getMessage());
                    }
                } else {
                    try {
                        Thread.sleep(interval * 10);
                        continue;
                    } catch (InterruptedException ex) {
                        addMessage(ex.getMessage());
                    }
                }
            }
            // should not see this...
            addMessage("Debug console stoped.");
            Running = false;
        }
    }
    
    private static class LocationWatch implements Runnable, ThreadedUtility
    {
        boolean runFlag = false;
        
        private JLabel x = null;
        private JLabel y = null;
        
        LocationWatch(JLabel xLabel, JLabel yLabel)
        {
            init(xLabel, yLabel);
        }
        
        LocationWatch() {}
        
        final void init (JLabel xLabel, JLabel yLabel)
        {
            x = xLabel;
            y = yLabel;
            
            x.setText(null);
            y.setText(null);            
        }
        
        public void setLabels(JLabel xLabel, JLabel yLabel)
        {
            init(xLabel, yLabel);
        }
        
        public boolean isLabelsSet()
        {
            return (x == null && y == null) ? false : true;
        }
        
        public void logMouseLocation()
        {
            Point p = MouseInfo.getPointerInfo().getLocation();
            StringBuilder sb = new StringBuilder();
            sb.append("X: ");
            sb.append(p.getX());
            sb.append(" Y: ");
            sb.append(p.getY());
            DebugUtilities.addDebugMessage(sb.toString());
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            Thread.currentThread().setName("Pointer location watcher");
            runFlag = !runFlag;
            while(runFlag)
            {
                try
                {
                    Thread.sleep(0, 5);
                    Point p = MouseInfo.getPointerInfo().getLocation();
                    x.setText("X: " + Double.toString(p.getX()));
                    y.setText("y: " + Double.toString(p.getY()));
                }
                catch (Throwable t)
                {
                    DebugUtilities.addDebugMessage(t.getMessage());
                    continue;
                }
            }
        }

        @Override
        public void display() {
            // TODO: review ThreadedUtility interface, and classes using it
        }

        @Override
        public void shutdown() {
            runFlag = false;
        }

        @Override
        public boolean isRunning() {
            return runFlag;
        }
    }
    
    public static void logMouseLocation()
    {
        if (lw == null)
        {
            lw = new LocationWatch();
        }
        lw.logMouseLocation();
    }
    
    public static void startLocationWatch(JLabel xLabel, JLabel yLabel)
    {
        if (lw == null)
        {
            lw = new LocationWatch(xLabel, yLabel);
        }
        else
        {
            if (!lw.isLabelsSet())
            {
                lw.setLabels(xLabel, yLabel);
            }
        }
        
        if (!lw.isRunning())
        {
            locationWatchThread = new Thread(lw);
            locationWatchThread.start();
        }
    }
    
    public static void locationWatchShutdown()
    {
        lw.shutdown();
    }
    
    public static void startDebugConsoleThread(JTextPane p) {
        if (d == null) {
            d = new DebugConsole(p);
        }
        if (!d.isPaneSet()) {
            d.setPane(p);
        }
        if (dConsoleThread == null || !dConsoleThread.isAlive()) {
            dConsoleThread = new Thread(d);
            dConsoleThread.start();
        }
    }
    
    public static void headlessDebugConsoleThread() {
        if (d == null) {
            d = new DebugConsole();
        }
        if (dConsoleThread == null || !dConsoleThread.isAlive()) {
            dConsoleThread = new Thread(d);
            dConsoleThread.start();
        }
    }
    
    public static void unsetDebugConsolePane() {
        d.unSetPane();
    }

    public static void addDebugMessage(String msg) {
        if (d == null) {
            headlessDebugConsoleThread();
        }
        if (!d.isPaneSet())
        {
            //NotificationIcon.displayMessage("Debug message added!", msg, TrayIcon.MessageType.INFO);
        }
        d.addMessage(msg);
    }

    public static void DebugConsoleshutdown() {
        d.shutdown();
    }
    
    public static void startThreadmonitorThread(JLabel c, JLabel p) {
        t = new threads(c, p);
        new Thread(t).start();
    }
    
    public static void threadmonitorShutdown() {
        t.shutdown();
    }
    
    public static void startMemoryThread(JLabel total, JLabel free, JLabel max, JLabel used, JLabel peak, byte unit) {
        m = new memory(total, free, max, used, peak, unit);
        new Thread(m).start();
    }
    
    public static void memoryShutdown() {
        m.shutdown();
    }
    
    public static void memoryChangeUnit(byte unit) {
        m.changeUnit(unit);
    }
    
    public static void changeUpdateInterval(long interval) {
        m.changeUpdateInterval(interval);
    }
    
    public static String getCallerMethodName() {
        final StackTraceElement[] ste = new Throwable().getStackTrace();
        return ste[1].getMethodName();
    }

    static String getCallerMethod() {
        final StackTraceElement[] ste = new Throwable().getStackTrace();
        return ste[2].getMethodName();
    }
    
    public static String getCallerClassName() {
        final StackTraceElement[] ste = new Throwable().getStackTrace();
        return ste[1].getClassName();
    }
    
    static String getCallerClass() {
        final StackTraceElement[] ste = new Throwable().getStackTrace();
        return ste[2].getClassName();
    }
    
    public static void invalidParameter() {
        DebugUtilities.addDebugMessage("Invalid parameter passed to method " + getCallerMethod() + " in class\n" + getCallerClass());
    }
    
    public static int detectParentDisplay(java.awt.Window w) throws
            IllegalArgumentException, InternalErrorException {
        if (w == null) {
            invalidParameter();
            throw new IllegalArgumentException("Cannot detect display for null");
        }
        GraphicsConfiguration config = w.getGraphicsConfiguration();
        GraphicsDevice myScreen = config.getDevice();
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] allScreens = env.getScreenDevices();
        int myScreenIndex = -1;
        for (GraphicsDevice display : allScreens) {
            myScreenIndex++;
            if (display.equals(myScreen)) {
                break;
            }
        }
        if (myScreenIndex == -1) {
            throw new InternalErrorException("Window does not appear to be on any of the screens");
        }
        return myScreenIndex;
    }

    public static int showVideoMemory(java.awt.Window w) {
        GraphicsConfiguration config = w.getGraphicsConfiguration();
        GraphicsDevice myDevice = config.getDevice();
        return myDevice.getAvailableAcceleratedMemory();
    }
    
    public static void showDisplayModes(java.awt.Window w) {
        GraphicsConfiguration config = w.getGraphicsConfiguration();
        GraphicsDevice myDevice = config.getDevice();
        DisplayMode[] displayModes = myDevice.getDisplayModes();
        for (DisplayMode displayMode : displayModes) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(displayMode.getWidth());
                sb.append("*");
                sb.append(displayMode.getHeight());
                sb.append("@");
                sb.append(displayMode.getRefreshRate());
                sb.append("Hz ");
                sb.append(displayMode.getBitDepth());
                sb.append("bpp");
                DebugUtilities.addDebugMessage(sb.toString());
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                DebugUtilities.addDebugMessage(ex.getMessage());
                continue;
            }
        }
    }
}
