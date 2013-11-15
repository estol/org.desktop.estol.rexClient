package org.desktop.estol.skeleton.applicationlogic;

import java.io.IOException;
import org.desktop.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author estol
 */
public enum MainLogic
{
    MainLogic;
    
    public void initialize()
    {
        autoMagicConnect();
    }
    
    private boolean autoMagicConnect()
    {
        try
        {
            BroadcastScanner scanner = new BroadcastScanner();
            new Thread(scanner).start();
        }
        catch (IOException ex)
        {
            DebugUtilities.addDebugMessage(ex.getMessage());
        }
        return true;
    }
}
