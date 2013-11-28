package org.desktop.estol.skeleton.system.windowloader;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.desktop.estol.skeleton.debug.DebugUtilities;
import org.desktop.estol.skeleton.system.exceptions.InternalErrorException;

/**
 * A revised version of the LoadWindow class.
 * 
 * Provides centralized methods to access frames in Java, by their titles.
 * 
 * @author estol
 */
public enum LoadWindow
{
    LoadWindow;

    // this is the old way of thinking
    // private ArrayList<JFrame> windows = new ArrayList();

    // this is the new way
    private volatile HashMap<String, dataWrapper> aliveWindows    = new HashMap();
    private volatile HashMap<String, dataWrapper> disposedWindows = new HashMap();
    
    /**
     * A very simple class, that is only here because of it's data
     * structure.
     * 
     * The constructor stores a JFrame, and a Thread, then there is
     * a getter for the Frame and the Thread.
     * 
     */
    private static class dataWrapper
    {
        private final JFrame frame;
        private final Thread thread;
        
        /**
         * Creates a dataWrapepr object, to encapsulate a frame, and the
         * thread that originally loaded the frame.
         * 
         * @param fr
         * @param th 
         */
        dataWrapper(JFrame fr, Thread th)
        {
            frame = fr;
            thread = th;
        }
        
        /**
         * returns the stored frame
         * @return 
         */
        JFrame getFrame()
        {
            return frame;
        }

        /**
         * returns the frame's loader thread
         * @return 
         */
        Thread getThread()
        {
            return thread;
        }
    }
    
    /**
     * Positions the window to the center of the screen.
     * The screen parameter allows to select the display device.
     * screen 0 is the main screen by default.
     * 
     * Tested positive on:
     *   - Windows Vista, 7, 8, 8.1, Server 2008, Server 2008 R2, Server 2012
     *   - Linux KDE 4.11 with latest nightly Wayland and XOrg 1.14, Gnome 3.10, same as for KDE
     *   - Mac OSX Cheetah, Puma, Jaguar, Panther, Tiger, Leopard, Snow Leopard, Lion, Mountain Lion, Mavericks
     * 
     * Tested negative on:
     *   - Linux awesome, apparently the setup I use on my workstation doesn't allow the detection of
     *     GraphicsEnvironment.
     * 
     * @param frame
     * @param screen 
     */
    private void setWindowPosition(String frame, int screen)
    {
        JFrame window = aliveWindows.get(frame).getFrame();
        
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] allDevices = env.getScreenDevices();
        int topLeftX, topLeftY, screenX, screenY, windowPosX, windowPosY;

        if (screen < allDevices.length && screen > -1)
        {
            topLeftX = allDevices[screen].getDefaultConfiguration().getBounds().x;
            topLeftY = allDevices[screen].getDefaultConfiguration().getBounds().y;

            screenX  = allDevices[screen].getDefaultConfiguration().getBounds().width;
            screenY  = allDevices[screen].getDefaultConfiguration().getBounds().height;
        }
        else
        {
            DebugUtilities.addDebugMessage("Screen probably doesn't exists, fallback to default main.");
            topLeftX = allDevices[0].getDefaultConfiguration().getBounds().x;
            topLeftY = allDevices[0].getDefaultConfiguration().getBounds().y;

            screenX  = allDevices[0].getDefaultConfiguration().getBounds().width;
            screenY  = allDevices[0].getDefaultConfiguration().getBounds().height;
        }

        windowPosX = ((screenX - window.getWidth())  / 2) + topLeftX;
        windowPosY = ((screenY - window.getHeight()) / 2) + topLeftY;
        
        window.setLocation(windowPosX, windowPosY);
        //window.setLocation((displayMode.getWidth() - window.getWidth()) / 2, (displayMode.getHeight() - window.getHeight()) / 2);
    }

    /**
     * Loads the JFrame type passed in the first argument, on the screen
     * specified in parameter screen
     * 
     * @param frame
     * @param screen 
     */
    public void Load(JFrame frame, int screen)
    {
        // If the window already exists, but is not visible, or not in the 
        // front, makes it visible, and brings it to the front.
        // Uses the last known position of the window.
        if (aliveWindows.containsKey(frame.getTitle()))
        {
            JFrame currentFrame = aliveWindows.get(frame.getTitle()).getFrame();
            if (!currentFrame.isVisible() || !currentFrame.isFocused())
            {
                currentFrame.setVisible(true);
                currentFrame.toFront();
                currentFrame.repaint();
            }
        }
        // If the window existed before,
        // reuses that instance of the frame
        // uses the last known position of the window.
        else if (disposedWindows.containsKey(frame.getTitle()))
        {
            JFrame currentFrame = disposedWindows.get(frame.getTitle()).getFrame();
            aliveWindows.put(currentFrame.getTitle(), disposedWindows.get(currentFrame.getTitle()));
            currentFrame.setVisible(true);
            currentFrame.toFront();
            currentFrame.repaint();
        }
        // If none of the above is true, cretes a new frame, with a new thread
        // and stores it in the aliveWindows hashmap
        else
        {
            final JFrame window = frame;
            String frameName = window.getTitle();
            DebugUtilities.addDebugMessage(frameName);
            Thread frameThread = new Thread
            (
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            window.setVisible(true);
                        }
                    }
            );
            frameThread.setName("Window thread for " + frameName);
            aliveWindows.put(frameName, new dataWrapper(window, frameThread));
            setWindowPosition(frameName, screen);
            frameThread.start();
        }
    }
    
    /**
     * Loads the frame on the default main display.
     * 
     * @param frame 
     */
    public void Load(JFrame frame)
    {
        Load(frame, 1);
    }
    
    /**
     * Flags the frame for being destroyed.
     * The instance of the Frame is being retained, so it could be reused later.
     * 
     * @param frame 
     */
    public synchronized void Destroyed(JFrame frame)
    {
        if (!"Terminate".equals(new Throwable().getStackTrace()[2].getMethodName()))
        {
            disposedWindows.put(frame.getTitle(), aliveWindows.get(frame.getTitle()));
            aliveWindows.remove(frame.getTitle());
        }
    }
    
    /**
     * Returns all known keys from the hashmap,
     * revealing the frames to chose from.
     * 
     * @return
     * @deprecated was used only for debug purposes
     */
    @Deprecated
    public String[] getFrameKeys()
    {
        String[] keys;
        
        Iterator iterator = aliveWindows.keySet().iterator();
        keys = new String[aliveWindows.keySet().size()];
        int i = 0;
        while (iterator.hasNext())
        {
            keys[i] = (String) iterator.next();
        }
        
        return keys;
    }
    
    /**
     * Calls the dispose method of the JFrame defined in frame,
     * or generates a debug message, if there is no such JFrame defined.
     * @param name 
     */
    public static void disposeFrame(String name)
    {
        if (LoadWindow.aliveWindows.containsKey(name))
        {
            LoadWindow.aliveWindows.get(name).getFrame().dispose();
        }
        else
        {
            DebugUtilities.addDebugMessage("Can't dispose frame: " + name);
        }
    }
    
    public static JFrame getFrame(String name) throws InternalErrorException
    {
        if (LoadWindow.aliveWindows.containsKey(name))
        {
            return LoadWindow.aliveWindows.get(name).getFrame();
        }
        else
        {
            throw new InternalErrorException("No frame with the passed name (" + name + ") exists.");
        }
    }
    
    /**
     * Terminates the application, by calling dispose on all currently alive
     * frame. The dispose call is essential, so the individual frames may ask
     * the user if the frame's state should be saved
     * (in layman's term: save? yes - no and since the process is irreversible,
     * there is no cancel.)
     */
    public synchronized void Terminate()
    {
        
        Iterator iterator = aliveWindows.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<String, dataWrapper> pairs = (Map.Entry) iterator.next();
            pairs.getValue().getFrame().dispose();
        }
        /*
        iterator = disposedWindows.entrySet().iterator();
        while (iterator.hasNext())
        {
            iterator.remove();
        }*/
        System.exit(0);
    }
    
    /**
     * Initializes the underlying debug system atm, but
     * in the future may take care of certain housekeeping required to set up the
     * environment of the program.
     */
    public static void initSystem()
    {
        try
        {
            DebugUtilities.headlessDebugConsoleThread();
            UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
        }
        catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            DebugUtilities.addDebugMessage("Exception occured while settings LAF: " + ex.getMessage());
        }
    }
}
