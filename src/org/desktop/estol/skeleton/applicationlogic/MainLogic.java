package org.desktop.estol.skeleton.applicationlogic;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.desktop.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author estol
 */
public enum MainLogic
{
    MainLogic;
    
    private ThreadGroup setupThreads = new ThreadGroup("SetupThreads");
    
    private Thread scannerThread;
    
    
    public void initialize()
    {
            autoMagicConnect();
    }
    
    private boolean autoMagicConnect()
    {
        try
        {
            BroadcastScanner scanner = new BroadcastScanner();
            scannerThread = new Thread(setupThreads, scanner);
            DebugUtilities.addDebugMessage(setupThreads.activeCount() + "");
            if (setupThreads.activeCount() == 0)
            {
                scannerThread.start();
            }
        }
        catch (IOException | NoSuchAlgorithmException ex)
        {
            DebugUtilities.addDebugMessage(ex.getMessage());
        }
        return true;
    }
}
